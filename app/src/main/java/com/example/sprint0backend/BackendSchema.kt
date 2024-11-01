package com.example.sprint0backend

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class ImageResponse(
    val id: Int,
    val download_url: String
)

data class ListingComponent(
    val id: Int,
    val name: String,
    val description: String,
    val city: String,
    val state: String,
    val zipcode: Int,
    val streetNumber: Int,  // changed to Int to match backend model
    val streetName: String,
    val tags: String,
    val priceRange: String?,  // matching backend's nullable String
    val rating: String?,      // matching backend's nullable String
    val startTime: String,
    val endTime: String,
    val reviews: String?
)


//data class ListingComponent(
//    val id: Int,
//    val name: String,
//    val description: String,
//    val city: String,
//    val state: String,
//    val zipcode: Int,
//    val streetNumber: String,
//    val streetName: String,
//    val tags: String,
//    val priceRange: Int,
//    val rating: Float,
//    val startTime: String,
//    val endTime: String,
//    val reviews: String
//)

// Interface to define API endpoints for Retrofit
interface BackendSchema {

    // Define a GET request to the "listings" endpoint
    @GET("listings")
    fun getListings(): Call<List<ListingComponent>>

    // Define a GET request to fetch image URLs for a specific listing by its ID
    @GET("images/{id}")
    fun getListingImages(@Path("id") listingId: Int): Call<List<ImageResponse>>

    @PUT("listings/update/{listing_id}")
    fun updateListing(
        @Path("listing_id") listingId: Int,
        @Query("name") name: String,
        @Query("streetNumber") streetNumber: Int,
        @Query("streetName") streetName: String,
        @Query("city") city: String,
        @Query("state") state: String,
        @Query("zipcode") zipcode: Int,
        @Query("description") description: String,
        @Query("tags") tags: String?,
        @Query("priceRange") priceRange: String?,
        @Query("rating") rating: String?,
        @Query("reviews") reviews: String?
    ): Call<Void>

}