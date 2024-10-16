package com.example.sprint0backend

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerListingScreen(listing: ListingComponent, navController: NavHostController) {
    // Scaffold to hold the back button in the top bar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listing Details", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        // Display detailed information about the selected listing
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) // Adds padding around the details
        ) {
            // Display listing image if available
//            Image(
//                painter = rememberAsyncImagePainter(model = listing.picture), // Use image URL
//                contentDescription = null,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(250.dp),
//                contentScale = ContentScale.Crop
//            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display the listing details with larger font
            Text(
                text = "Name: ${listing.name}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Owner: ${listing.name}", // If owner is available in your data
                fontSize = 20.sp,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Address: ${listing.streetNumber} ${listing.streetName}, ${listing.city}, ${listing.state} ${listing.zipcode}",
                fontSize = 20.sp,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Date: ${listing.startTime} - ${listing.endTime}", // Assuming startTime and endTime are relevant for this field
                fontSize = 20.sp,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Price Range: ${listing.priceRange}",
                fontSize = 20.sp,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Rating: ${listing.rating}",
                fontSize = 20.sp,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Display tags if available
            if (listing.tags.isNotEmpty()) {
                Text(
                    text = "Categories:",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                listing.tags.forEach { tag ->
                    Text(text = "- $tag", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display the listing description
            Text(
                text = "Description: ${listing.description}",
                fontSize = 18.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
