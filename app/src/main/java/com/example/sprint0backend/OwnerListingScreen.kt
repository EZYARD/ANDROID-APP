package com.example.sprint0backend

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
fun OwnerListingScreen(owner: String,  navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "$owner's Listing") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        // Only showing the owner's listing text
        Text(
            text = "$owner's Listing",
            modifier = Modifier.padding(16.dp)
        )
//    }
//    Column(modifier = Modifier.padding(8.dp)) {
//        Image(
//            painter = rememberAsyncImagePainter(model = listing.picture),
//            contentDescription = null,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(250.dp),
//            contentScale = ContentScale.Crop
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(text = "Owner: ${listing.owner}", style = MaterialTheme.typography.bodyMedium)
//        Text(text = "Address: ${listing.address}")
//        Text(text = "Date: ${listing.date}")
//        Text(text = "Price Range: ${listing.priceRange}")
//        Text(text = "Rating: ${listing.rating}")
//    }

    }
}