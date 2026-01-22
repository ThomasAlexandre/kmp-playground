package se.ac.kmp_playground.data

import io.ktor.client.call.body
import io.ktor.client.request.get

class StoreRepository(
    private val baseUrl: String = "https://thomasalexandre.unison-services.cloud/s/stores-api"
) {
    private val client = createHttpClient()

    suspend fun getAllStores(): Result<List<Store>> {
        return try {
            val stores: List<Store> = client.get("$baseUrl/stores").body()
            Result.success(stores)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}