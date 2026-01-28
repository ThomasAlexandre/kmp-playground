package se.ac.kmp_playground.data

import io.ktor.client.call.body
import io.ktor.client.request.get

class ShoppingListRepository(
    private val baseUrl: String = "https://thomasalexandre.unison-services.cloud/s/shopping-lists-api"
) {
    private val client = createHttpClient()

    suspend fun getShoppingListById(listId: String): Result<ShoppingList> {
        return try {
            val list: ShoppingList = client.get("$baseUrl/lists/$listId").body()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getShoppingListsByIds(listIds: List<String>): Result<List<ShoppingList>> {
        return try {
            val lists = listIds.mapNotNull { listId ->
                try {
                    client.get("$baseUrl/lists/$listId").body<ShoppingList>()
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(lists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEnrichedShoppingList(listId: String): Result<EnrichedShoppingList> {
        return try {
            val list: EnrichedShoppingList = client.get("$baseUrl/lists/$listId/enriched").body()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}