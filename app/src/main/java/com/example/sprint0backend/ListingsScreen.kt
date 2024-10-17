package com.example.sprint0backend

import Listings
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

//@Preview
@Composable
fun ListingsScreen(navController: NavHostController) {
    var listings by rememberSaveable { mutableStateOf<List<ListingComponent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        BackendWrapper.getListings(
            onSuccess = { backendListings ->
                listings = backendListings
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }


    // Display loading indicator, error message, or listings based on the state
    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
    } else if (errorMessage != null) {
        Text(text = errorMessage!!, modifier = Modifier.fillMaxSize())
    } else {
        // Display the listings (hardcoded or backend) in a LazyColumn
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(listings, key = { it.id }) { listing ->
                Listings(listing = listing, navController = navController)
            }
        }
    }
}