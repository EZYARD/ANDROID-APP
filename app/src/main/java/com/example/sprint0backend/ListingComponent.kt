package com.example.sprint0backend

// Data class that represents the structure of a single listing item
data class ListingComponent(
    val id: Int,
    val name: String,
    val description: String,
    val city: String,
    val state: String,
    val zipcode: Int,
    val streetNumber: String,
    val streetName: String,
    val tags: String,
    val priceRange: Int,
    val rating: Float,
    val startTime: String,
    val endTime: String,
    val reviews: String
)
