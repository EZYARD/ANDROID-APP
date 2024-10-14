package com.example.sprint0backend

// Data class that represents the structure of a single listing item
data class ListingComponent(
    val id: Int,     // Unique identifier for each listing
    val owner: String,
    val address: String,
    val picture: List<String>,   /*List<Int>,*/     // Multiple pictures uses List
    val date: String,
    val tags: String,
    val priceRange: String,
    val rating: Float
)
