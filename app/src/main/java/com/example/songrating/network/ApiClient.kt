package com.example.songrating.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object ApiClient {
    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(7, TimeUnit.SECONDS)
        .writeTimeout(7, TimeUnit.SECONDS)
        .readTimeout(7, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
}
