# Receipt Parser

A Kotlin JVM CLI tool for parsing Swedish grocery store receipts (PDF) and uploading price data to the prices-api.

## Features

- Parses PDF receipts from ICA stores using Tabula table extraction
- Extracts product names, article numbers, prices, quantities, and units
- Converts parsed data to price-api format for upload
- Supports both weight-based (kg) and count-based (st) items

## Building

```bash
./gradlew :receipt-parser:build
```

## Usage

### Parse Receipts to JSON

```bash
# Parse single receipt
./gradlew :receipt-parser:run --args="parse receipt.pdf"

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

# Upload to default API
./gradlew :receipt-parser:run --args="upload receipt.pdf"

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
    "storeName": "ICA Supermarket Brommaplan",
    "storeAddress": "...",
    "orgNumber": "SE559175008701",
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

## Supported Stores

| Store Name | Store ID |
|------------|----------|
| ICA Supermarket Brommaplan | 1 |
| Maxi ICA Stormarknad Bromma | 2 |

## Dependencies

- **Tabula** - PDF table extraction
- **PDFBox** - PDF text extraction (used by Tabula)
- **Ktor Client** - HTTP client for API uploads
- **Clikt** - Command-line argument parsing
- **Kotlinx Serialization** - JSON serialization