package com.example.sprint0backend

import retrofit2.Call
import retrofit2.http.GET

// Interface to define API endpoints for Retrofit
interface ListingBackend {

    // Define a GET request to the "listings" endpoint
    @GET("listings")
    fun getListings(): Call<List<ListingComponent>>
}
