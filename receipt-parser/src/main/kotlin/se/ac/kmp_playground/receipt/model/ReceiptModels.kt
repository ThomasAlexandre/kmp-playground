package se.ac.kmp_playground.receipt.model

import kotlinx.serialization.Serializable

/**
 * Represents a parsed receipt from a grocery store
 */
@Serializable
data class ParsedReceipt(
    val storeName: String,
    val storeAddress: String,
    val orgNumber: String,
    val date: String,          // ISO 8601 format: 2026-01-30T12:49:00Z
    val receiptNumber: String,
    val items: List<ReceiptItem>,
    val totalAmount: String,
    val currency: String = "SEK"
)

/**
 * Represents a single item on the receipt
 */
@Serializable
data class ReceiptItem(
    val name: String,
    val articleNumber: String,  // Barcode/EAN or internal store code
    val unitPrice: String,
    val quantity: Double,
    val unit: String,           // "st" (pieces), "kg", etc.
    val totalPrice: String
)

/**
 * Format for posting to the price-api
 */
@Serializable
data class PriceApiRequest(
    val product: ProductIdentifier,
    val storeId: String,
    val price: PriceRecord,
    val recordedAt: String
)

@Serializable
data class ProductIdentifier(
    val type: String,           // "barcode" or "store_specific"
    val code: String,
    val storeId: String? = null // Only for store_specific
)

@Serializable
data class PriceRecord(
    val price: String,
    val currency: String,
    val unit: String            // "per_item", "per_kg", "per_100g", "per_liter"
)

/**
 * Store information from stores-api
 */
@Serializable
data class Store(
    val id: Int,
    val key: String,
    val name: String,
    val lon: Double,
    val lat: Double,
    val address: String,
    val postalCode: String,
    val city: String,
    val imageUrl: String
)

/**
 * Product mapping from product-mapping-api
 * Maps store-specific article numbers to universal barcodes
 */
@Serializable
data class ProductMapping(
    val storeId: Int,
    val storeReference: String,
    val universalBarcode: String
)