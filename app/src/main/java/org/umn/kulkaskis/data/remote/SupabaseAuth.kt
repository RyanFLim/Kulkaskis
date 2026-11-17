package org.umn.kulkaskis.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val email: String
)

sealed class AuthResult {
    data class Success(val session: AuthSession) : AuthResult()
    data object NeedsConfirmation : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class SupabaseAuth {
    private val client = OkHttpClient()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    suspend fun signUp(email: String, password: String): AuthResult =
        post(
            "/auth/v1/signup",
            JSONObject().put("email", email).put("password", password)
        )

    suspend fun signIn(email: String, password: String): AuthResult =
        post(
            "/auth/v1/token?grant_type=password",
            JSONObject().put("email", email).put("password", password)
        )

    suspend fun refresh(refreshToken: String): AuthResult =
        post(
            "/auth/v1/token?grant_type=refresh_token",
            JSONObject().put("refresh_token", refreshToken)
        )

    private suspend fun post(path: String, json: JSONObject): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(SupabaseConfig.URL + path)
                    .addHeader("apikey", SupabaseConfig.KEY)
                    .post(json.toString().toRequestBody(jsonType))
                    .build()
                client.newCall(request).execute().use { response ->
                    val text = response.body?.string().orEmpty()
                    val obj = try {
                        JSONObject(text)
                    } catch (e: Exception) {
                        null
                    }
                    parse(response.code, obj)
                }
            } catch (e: IOException) {
                AuthResult.Error("No internet connection")
            }
        }

    private fun parse(code: Int, obj: JSONObject?): AuthResult {
        if (obj == null) return AuthResult.Error("Unexpected response from server")
        if (code !in 200..299) {
            val message = obj.optString("msg")
                .ifBlank { obj.optString("error_description") }
                .ifBlank { obj.optString("message") }
                .ifBlank { "Something went wrong" }
            return AuthResult.Error(message)
        }
        val token = obj.optString("access_token")
        if (token.isBlank()) return AuthResult.NeedsConfirmation
        val user = obj.optJSONObject("user")
        return AuthResult.Success(
            AuthSession(
                accessToken = token,
                refreshToken = obj.optString("refresh_token"),
                userId = user?.optString("id").orEmpty(),
                email = user?.optString("email").orEmpty()
            )
        )
    }
}