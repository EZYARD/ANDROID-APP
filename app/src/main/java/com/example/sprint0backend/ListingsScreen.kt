package com.example.sprint0backend

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun ListingsScreen(listings: List<ListingComponent>) {
    // LazyColumn is used for scrolling
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(listings) { listing ->
            ListingCard(listing = listing)
        }
    }
}

// Composable function to display a single listing as a card
@Composable
fun ListingCard(listing: ListingComponent) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        // Column arranges the listing information vertically
        Column(modifier = Modifier.padding(16.dp)) {
            // Load and display the image using Coil, an image loading library for Compose
            Image(
                // `rememberAsyncImagePainter` loads the image using the URL from `listing.picture`
                painter = rememberAsyncImagePainter(model = listing.picture),
                contentDescription = "Listing Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp) // Sets a fixed height for the image
            )
            Spacer(modifier = Modifier.height(8.dp)) // Adds vertical space between image and text

            // Display listing details as text below the image
            Text(text = "Owner: ${listing.owner}")
            Text(text = "Address: ${listing.address}")
            Text(text = "Date: ${listing.date}")
            Text(text = "Price Range: ${listing.priceRange}")
            Text(text = "Rating: ${listing.rating}")
        }
    }
}

//////////////////HARDCODED STUFF/////////////////////


//
//@Composable
//fun MainScreen() {
//    val tempListings = getListings()
//    ListingsScreen(listings = tempListings)
//}
//
//fun getListings(): List <ListingComponent> {
//    return listOf(
//        // the following is hard coded info for testing purposes
//        ListingComponent(
//            id = 1,
//            owner = "Max Holloway",
//            address = "123 plae ground stret",
//            picture = R.drawable.image4090,
//            date = "Sept 30, 9 AM - 5 PM",
//            tags = listOf("Electronics", "Books", "Clothes", "Sports"),
//            priceRange = "$1 - $2,499",
//            rating = 4.3f
//        ),
//        ListingComponent(
//            id = 2,
//            owner = "Alex Volk",
//            address = "Kangaroo Australia",
//            picture = R.drawable.alienwarex14r22, // Use the same image or a different one
//            date = "Oct 10, 10 AM - 4 PM",
//            tags = listOf("Furniture", "Electronics"),
//            priceRange = "$10 - $700",
//            rating = 4.6f
//        ),
//        ListingComponent(
//            id = 3,
//            owner = "Joe Biden",
//            address = "123 White House Blvd",
//            picture = R.drawable.ps5pro, // Use the same image or another
//            date = "Oct 22, 8 AM - 1 PM",
//            tags = listOf("Toys", "Books", "Games"),
//            priceRange = "$5 - $1000",
//            rating = 4.9f
//        // end of testing info
//        )
//    )
//}
//@Preview
//@Composable
//fun PreviewListingsScreen() {
//    MainScreen()
//}