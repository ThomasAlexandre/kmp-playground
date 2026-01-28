package se.ac.kmp_playground.data

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingList(
    val id: String,
    val name: String,
    val createdAt: String,
    val updatedAt: String,
    val isArchived: Boolean
)