package se.ac.kmp_playground.data

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val preferredLanguage: String,
    val currency: String,
    val notificationsEnabled: Boolean,
    val darkModeEnabled: Boolean
)

@Serializable
data class SelectedStore(
    val storeId: Int,
    val addedAt: String,
    val isPreferred: Boolean
)

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String,
    val createdAt: String,
    val profile: UserProfile,
    val selectedStores: List<SelectedStore>,
    val shoppingListIds: List<String>
)