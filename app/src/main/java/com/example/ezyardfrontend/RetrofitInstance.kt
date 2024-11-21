package com.example.ezyardfrontend

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Retrofit is a type-safe HTTP client for Android for us to call our backend API
object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants().BACKEND_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: BackendSchema by lazy {
        retrofit.create(BackendSchema::class.java)
    }
}
