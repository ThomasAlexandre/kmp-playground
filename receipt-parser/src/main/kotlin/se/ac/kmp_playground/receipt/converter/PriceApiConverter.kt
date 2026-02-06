package se.ac.kmp_playground.receipt.converter

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import se.ac.kmp_playground.receipt.model.*

/**
 * Converts parsed receipts into price-api request format
 */
class PriceApiConverter(
    private val storesApiUrl: String = DEFAULT_STORES_API_URL
) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // Cache for store lookups by key
    private val storeCache = mutableMapOf<String, Store?>()

    companion object {
        const val DEFAULT_STORES_API_URL = "https://thomasalexandre.unison-services.cloud/s/stores-api"

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
     * Resolve store ID from receipt metadata by querying the stores-api
     */
    private fun resolveStoreId(receipt: ParsedReceipt): String {
        // Try org number first (this is the store key in the backend)
        val store = getStoreByKey(receipt.orgNumber)
        if (store != null) {
            return store.id.toString()
        }

        // Fallback: generate from org number digits
        return receipt.orgNumber.filter { it.isDigit() }.takeLast(6).ifEmpty { "0" }
    }

    /**
     * Query the stores-api to get store by key
     */
    private fun getStoreByKey(key: String): Store? {
        // Check cache first
        if (storeCache.containsKey(key)) {
            return storeCache[key]
        }

        // Query the API
        val store = runBlocking {
            try {
                val response = client.get("$storesApiUrl/stores/key/$key")
                if (response.status == HttpStatusCode.OK) {
                    response.body<Store>()
                } else {
                    null
                }
            } catch (e: Exception) {
                System.err.println("Warning: Failed to query stores-api for key $key: ${e.message}")
                null
            }
        }

        // Cache the result (including null for not found)
        storeCache[key] = store
        return store
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

    /**
     * Close the HTTP client
     */
    fun close() {
        client.close()
    }
}

/**
 * Extension function to convert receipts easily
 */
fun ParsedReceipt.toPriceApiRequests(
    storesApiUrl: String = PriceApiConverter.DEFAULT_STORES_API_URL
): List<PriceApiRequest> {
    return PriceApiConverter(storesApiUrl).convert(this)
}