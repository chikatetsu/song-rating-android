package com.example.songrating.network

import com.example.songrating.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object ApiService {
    private val JSON = "application/json; charset=utf-8".toMediaType()

    fun sendVote(betterSong: String, worseSong: String): Result<String> {
        return try {
            val payload = JSONObject().apply {
                put("better_song", betterSong)
                put("worse_song", worseSong)
            }

            val body = payload.toString().toRequestBody(JSON)
            val request = Request.Builder()
                .url("${BuildConfig.API_URL}/rate")
                .addHeader("Authorization", "Bearer ${BuildConfig.AUTH_TOKEN}")
                .post(body)
                .build()

            val response = ApiClient.client.newCall(request).execute()

            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val json = JSONObject(response.body.string())
            val message = json.optString("response", "(no message)")

            Result.success(message)
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }
}
