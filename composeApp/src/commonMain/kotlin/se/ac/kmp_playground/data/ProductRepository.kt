package se.ac.kmp_playground.data

import io.ktor.client.call.body
import io.ktor.client.request.get

class ProductRepository(
    private val baseUrl: String = "https://thomasalexandre.unison-services.cloud/s/products-api"
) {
    private val client = createHttpClient()

    suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            val products: List<Product> = client.get("$baseUrl/products").body()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
