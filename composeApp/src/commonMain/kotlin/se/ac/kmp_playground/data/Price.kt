package se.ac.kmp_playground.data

import kotlinx.serialization.Serializable

@Serializable
data class PriceRecord(
    val price: String,
    val currency: String,
    val unit: String
)

@Serializable
data class StorePriceComparison(
    val storeId: String,
    val recordedAt: String,
    val price: PriceRecord
)

/**
 * Represents price comparison for a single product across multiple stores
 */
data class ProductPriceComparison(
    val barcode: String,
    val productName: String,
    val storePrices: List<StorePriceComparison>
)

/**
 * Represents total price comparison for a shopping list across stores
 */
data class ShoppingListPriceComparison(
    val shoppingListId: String,
    val shoppingListName: String,
    val itemComparisons: List<ProductPriceComparison>,
    val storeTotals: Map<Int, Double> // storeId -> total price
)