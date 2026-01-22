package se.ac.kmp_playground.data

import kotlinx.serialization.Serializable

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