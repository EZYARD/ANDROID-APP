package com.example.sprint0backend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                MainApp()
            }
        }
    }
}

/**
 * Allows the functionality to go to the main ListingsScreen
 * and the OwnerListingScreen (if a listing is clicked)
 * */
@Preview
@Composable
fun MainApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "ListingsScreen"){
        composable("ListingsScreen") {
            ListingsScreen(navController = navController)
        }
        composable("OwnerListingScreen/{listingId}") { backStackEntry ->
            // Get the listingId passed from the previous screen
            val listingId = backStackEntry.arguments?.getString("listingId")?.toIntOrNull() ?: -1
            val listings = getHardcodedListings() // Use your method or replace with dynamic data

            // Find the listing with the matching ID
            val selectedListing = listings.find { it.id == listingId }

            // If the listing exists, pass it to OwnerListingScreen
            selectedListing?.let {
                OwnerListingScreen(listing = it, navController = navController)
            }
        }
    }
}