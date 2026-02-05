package se.ac.kmp_playground.receipt.converter

import se.ac.kmp_playground.receipt.model.*

/**
 * Converts parsed receipts into price-api request format
 */
class PriceApiConverter(
    private val storeIdMapping: Map<String, String> = DEFAULT_STORE_MAPPING
) {

    companion object {
        // Mapping from store names/org numbers to store IDs in your system
        val DEFAULT_STORE_MAPPING = mapOf(
            "SE559175008701" to "1",      // ICA Supermarket Brommaplan
            "ICA Supermarket Brommaplan" to "1",
            "SE556182563001" to "2",      // Maxi ICA Stormarknad Bromma
            "Maxi ICA Stormarknad Bromma" to "2",
        )

        // Minimum length for EAN/UPC barcodes (shorter codes are store-specific)
        private const val MIN_BARCODE_LENGTH = 8
    }

    /**
     * Convert a parsed receipt to a list of price-api requests
     */
    fun convert(receipt: ParsedReceipt): List<PriceApiRequest> {
        val storeId = resolveStoreId(receipt)

        return receipt.items.map { item ->
            PriceApiRequest(
                product = createProductIdentifier(item, storeId),
                storeId = storeId,
                price = PriceRecord(
                    price = item.unitPrice,
                    currency = receipt.currency,
                    unit = mapUnit(item.unit)
                ),
                recordedAt = receipt.date
            )
        }
    }

    /**
     * Resolve store ID from receipt metadata
     */
    private fun resolveStoreId(receipt: ParsedReceipt): String {
        // Try org number first
        storeIdMapping[receipt.orgNumber]?.let { return it }

        // Try store name
        storeIdMapping[receipt.storeName]?.let { return it }

        // Try partial match on store name
        storeIdMapping.entries.find { (key, _) ->
            receipt.storeName.contains(key, ignoreCase = true) ||
                    key.contains(receipt.storeName, ignoreCase = true)
        }?.let { return it.value }

        // Fallback: generate from org number
        return receipt.orgNumber.filter { it.isDigit() }.takeLast(6).ifEmpty { "0" }
    }

    /**
     * Create product identifier - barcode for standard products, store-specific for internal codes
     */
    private fun createProductIdentifier(item: ReceiptItem, storeId: String): ProductIdentifier {
        val code = item.articleNumber

        // Determine if this is a standard barcode or store-specific code
        return if (isStandardBarcode(code)) {
            ProductIdentifier(
                type = "barcode",
                code = code
            )
        } else {
            ProductIdentifier(
                type = "store_specific",
                code = code,
                storeId = storeId
            )
        }
    }

    /**
     * Check if the code is a standard barcode (EAN-8, EAN-13, UPC-A, etc.)
     */
    private fun isStandardBarcode(code: String): Boolean {
        // Standard barcodes are typically 8, 12, or 13 digits
        // Store-specific codes are usually shorter (4-6 digits)
        if (!code.all { it.isDigit() }) return false

        return when (code.length) {
            8 -> true   // EAN-8
            12 -> true  // UPC-A
            13 -> true  // EAN-13
            14 -> true  // GTIN-14
            else -> code.length >= MIN_BARCODE_LENGTH && validateCheckDigit(code)
        }
    }

    /**
     * Validate EAN/UPC check digit (simple validation)
     */
    private fun validateCheckDigit(code: String): Boolean {
        if (code.length < 8) return false

        // For codes that look like valid barcodes (10+ digits), assume they're valid
        // Full check digit validation could be added if needed
        return code.length >= 10
    }

    /**
     * Map receipt unit to price-api unit format
     */
    private fun mapUnit(unit: String): String {
        return when (unit.lowercase()) {
            "st", "styck", "pcs" -> "per_item"
            "kg", "kilo" -> "per_kg"
            "g", "gram" -> "per_100g"
            "l", "liter" -> "per_liter"
            "ml" -> "per_liter"
            else -> "per_item"
        }
    }
}

/**
 * Extension function to convert receipts easily
 */
fun ParsedReceipt.toPriceApiRequests(
    storeIdMapping: Map<String, String> = PriceApiConverter.DEFAULT_STORE_MAPPING
): List<PriceApiRequest> {
    return PriceApiConverter(storeIdMapping).convert(this)
}