package com.example.sprint0backend

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerListingScreen(listing: ListingComponent, navController: NavHostController) {
    // Display detailed information about the selected listing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // Adds padding around the details
    ) {
        // Display the listing details
        Text(text = "Owner: ${listing.owner}", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Address: ${listing.address}")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Date: ${listing.date}")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Price Range: ${listing.priceRange}")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Rating: ${listing.rating}")

        // Display tags if available
        Spacer(modifier = Modifier.height(16.dp))
        if (listing.tags.isNotEmpty()) {
            Text(text = "Categories:")
            listing.tags.forEach { tag ->
                Text(text = "- $tag")
            }
        }
    }
}