package se.ac.kmp_playground.data

import io.ktor.client.call.body
import io.ktor.client.request.get

class UserRepository(
    private val baseUrl: String = "https://thomasalexandre.unison-services.cloud/s/users-api"
) {
    private val client = createHttpClient()

    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val user: User = client.get("$baseUrl/users/$userId").body()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}