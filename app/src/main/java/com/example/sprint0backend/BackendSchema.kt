package com.example.sprint0backend

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

data class ImageResponse(
    val id: Int,
    val download_url: String
)

// Interface to define API endpoints for Retrofit
interface BackendSchema {

    // Define a GET request to the "listings" endpoint
    @GET("listings")
    fun getListings(): Call<List<ListingComponent>>

    // Define a GET request to fetch image URLs for a specific listing by its ID
    @GET("images/{id}")
    fun getListingImages(@Path("id") listingId: Int): Call<List<ImageResponse>>
}
