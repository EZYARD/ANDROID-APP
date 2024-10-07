package com.example.sprint0backend

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton object for creating and managing Retrofit instance
// Helps connect to FastAPI data
object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000")

            // conversion between Kotlin objects and the API responses
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Lazy initialization of the API interface
    // This is where the Retrofit instance is used to create the implementation of the API endpoints
    val api: ListingBackend by lazy {
        retrofit.create(ListingBackend::class.java)
    }
}
