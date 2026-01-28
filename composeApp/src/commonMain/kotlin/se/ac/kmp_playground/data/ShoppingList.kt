package se.ac.kmp_playground.data

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingList(
    val id: String,
    val name: String,
    val createdAt: String,
    val updatedAt: String,
    val isArchived: Boolean,
    val items: List<ShoppingItem> = emptyList()
)

@Serializable
data class ShoppingItem(
    val code: String,
    val quantity: Int,
    val isChecked: Boolean = false,
    val notes: String? = null
)

@Serializable
data class EnrichedShoppingItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val unit: String,
    val category: String,
    val isChecked: Boolean,
    val notes: String? = null,
    val productCode: String? = null,
    val productBrands: String? = null,
    val productImageUrl: String? = null
)

@Serializable
data class EnrichedShoppingList(
    val id: String,
    val name: String,
    val createdAt: String,
    val updatedAt: String,
    val isArchived: Boolean,
    val items: List<EnrichedShoppingItem>
)