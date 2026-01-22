package se.ac.kmp_playground.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    @SerialName("code") val barcode: String,
    @SerialName("product_name") val productName: String,
    val brands: String,
    @SerialName("image_url") val productImageUrl: String,
    @SerialName("stores") val productStores: String? = null
)