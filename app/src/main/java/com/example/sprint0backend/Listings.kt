package com.example.sprint0backend

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter

/*
* Creates a card for each listing made and displays it on main screen
*/
@Composable
fun Listings(listing: ListingComponent) {
    Card(
        modifier = Modifier
            .fillMaxWidth() // Makes the card fill the width of the screen
            .padding(8.dp) // Adds padding around the card
            .clickable { /* Placeholder clicking ability */ },
        elevation = CardDefaults.cardElevation(4.dp) // Adds elevation for shadow effect
    ) {
        // Column to stack listing content vertically
        Column(modifier = Modifier.padding(8.dp)) {
            Image(
                painter = rememberAsyncImagePainter(model = listing.picture), // Loads image from url
                // painter = painterResource(id = listing.picture),         // Alternative way to use local image
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth() // Image fills the width of the card
                    .height(250.dp), // Sets fixed height for the image
                contentScale = ContentScale.Crop // Crops the image to fill the dimensions while preserving aspect ratio
            )

            // Adds vertical space between the image and text (no overlapping)
            Spacer(modifier = Modifier.height(8.dp))

            // The Following were used for hardcoded listings

            // Display listing details as text below the image
//            Text(text = "Owner: ${listing.owner}", style = MaterialTheme.typography.bodyMedium)
//            Text(text = "Address: ${listing.address}")
//            Text(text = "Date: ${listing.date}")
//            Text(text = "Price Range: ${listing.priceRange}")
//            Text(text = "Rating: ${listing.rating}")
        }
    }
}
