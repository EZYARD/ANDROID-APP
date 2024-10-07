package com.example.sprint0backend

// Data class that represents the structure of a single listing item
data class ListingComponent(
    val id: Int,     // Unique identifier for each listing
    val owner: String,
    val address: String,
    val picture: String,
    val date: String,
    val tags: List<String>,
    val priceRange: String,
    val rating: Float
)
