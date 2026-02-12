# Receipt Parser

A Kotlin JVM CLI tool for parsing Swedish grocery store receipts (PDF) and uploading price data to the prices-api.

## Features

- **Multiple parser support** - ICA and Hemköp/Axfood receipt formats
- Extracts product names, article numbers, prices, quantities, and units
- Converts parsed data to price-api format for upload
- Supports both weight-based (kg) and count-based (st) items
- **Dynamically resolves store IDs** by querying the stores-api using the receipt's org number

## Supported Stores

| Parser | Stores | Article Numbers |
|--------|--------|-----------------|
| `ica` (default) | ICA Supermarket, Maxi ICA, ICA Kvantum | EAN barcodes (8-13 digits) |
| `hemkop` | Hemköp, Willys, Tempo, Handlar'n (Axfood) | Generated from product name (HKxxxxxxxx) |

## Building

```bash
./gradlew :receipt-parser:build
```

## Usage

### Parse Receipts to JSON

```bash
# Parse ICA receipt (default parser)
./gradlew :receipt-parser:run --args="parse receipt.pdf"

# Parse Hemköp receipt
./gradlew :receipt-parser:run --args="parse --parser hemkop receipt.pdf"

# Parse multiple receipts
./gradlew :receipt-parser:run --args="parse receipt1.pdf receipt2.pdf"

# Save output to directory
./gradlew :receipt-parser:run --args="parse -o ./output receipt.pdf"

# Output in price-api format
./gradlew :receipt-parser:run --args="parse --price-api receipt.pdf"
```

### Upload to Price API

```bash
# Dry run (preview without uploading)
./gradlew :receipt-parser:run --args="upload --dry-run receipt.pdf"

# Upload ICA receipt to default API
./gradlew :receipt-parser:run --args="upload receipt.pdf"

# Upload Hemköp receipt
./gradlew :receipt-parser:run --args="upload --parser hemkop receipt.pdf"

# Upload to custom API URL
./gradlew :receipt-parser:run --args="upload --api-url https://example.com/api receipt.pdf"

# Override store ID
./gradlew :receipt-parser:run --args="upload --store-id 42 receipt.pdf"
```

### List Store Mappings

```bash
./gradlew :receipt-parser:run --args="list-stores"
```

## Output Formats

### Parsed Receipt (default)

```json
{
    "storeName": "Supermarket xxxx",
    "storeAddress": "...",
    "orgNumber": "SE55xxxxxxxxxx",
    "date": "2026-02-02T11:43:00Z",
    "receiptNumber": "7588",
    "items": [
        {
            "name": "Poke Bowl Lax",
            "articleNumber": "2095998800000",
            "unitPrice": "139.00",
            "quantity": 1.0,
            "unit": "st",
            "totalPrice": "139.00"
        }
    ],
    "totalAmount": "227.85",
    "currency": "SEK"
}
```

### Price API Format (--price-api)

```json
[
    {
        "product": {
            "type": "barcode",
            "code": "2095998800000",
            "storeId": null
        },
        "storeId": "1",
        "price": {
            "price": "139.00",
            "currency": "SEK",
            "unit": "per_item"
        },
        "recordedAt": "2026-02-02T11:43:00Z"
    }
]
```

## Store Resolution

Store IDs are resolved dynamically by querying the stores-api using the receipt's organization number (org number).
The stores-api endpoint `GET /stores/key/{orgNumber}` is used to look up the store.

Use the `list-stores` command to see all available stores:

```bash
./gradlew :receipt-parser:run --args="list-stores"
```

Example output:
```
Known stores:
  SE55xxxxxxxxx1 -> Store ID: 1 (ICA ...)
  SE55xxxxxxxxx2 -> Store ID: 2 (Maxi ICA ...)
```

## Parser Details

### ICA Parser (`--parser ica`)

The default parser for ICA stores (ICA Supermarket, Maxi ICA, ICA Kvantum).

- Uses **Tabula** for table extraction from PDF
- Extracts **EAN barcodes** (article numbers) from receipts
- Supports weight-based pricing (kr/kg)
- Product identifiers are standard barcodes usable across stores

Example receipt format:
```
Beskrivning          Artikelnummer  Pris    Antal  Summa
Äppeljuice Cloudy    7318690173885  29,95   1 st   29,95
```

### Hemköp Parser (`--parser hemkop`)

Parser for Hemköp and other Axfood stores (Willys, Tempo, Handlar'n).

- Uses **PDFBox** text extraction (no table structure)
- **No barcodes** - generates store-specific codes from product names
- Generated codes use format `HKxxxxxxxx` (hash-based)
- Handles Swedish special characters (Ä, Ö, Å, É, Ô, etc.)
- Automatically skips discount lines (Klubbpris)

Example receipt format:
```
ENTRECÔTE
    0,401kg*445,00kr/kg     178,45
SVARTPEPPAR HEL70G           32,95
```

Supported item formats:
- Simple: `PRODUCT NAME    XX,XX`
- Weighted: Product name on one line, `X,XXXkg*YYY,YYkr/kg ZZ,ZZ` on next
- Quantity: `PRODUCT NAME Xst*YY,YY ZZ,ZZ`

## Dependencies

- **Tabula** - PDF table extraction (ICA parser)
- **PDFBox** - PDF text extraction
- **Ktor Client** - HTTP client for API uploads
- **Clikt** - Command-line argument parsing
- **Kotlinx Serialization** - JSON serialization
