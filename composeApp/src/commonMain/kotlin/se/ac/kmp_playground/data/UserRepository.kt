package se.ac.kmp_playground.data

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

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

    suspend fun updateUser(user: User): Result<User> {
        return try {
            val updatedUser: User = client.put("$baseUrl/users/${user.id}") {
                contentType(ContentType.Application.Json)
                setBody(user)
            }.body()
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}