package com.example.sprint0backend

import OwnerListingScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier


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
    var listings by rememberSaveable { mutableStateOf<List<ListingComponent>>(emptyList()) }

    NavHost(navController = navController, startDestination = "ListingsScreen") {
        composable("ListingsScreen") {
            ListingsScreen(navController = navController)
        }
        composable("OwnerListingScreen/{listingId}") { backStackEntry ->
            // default values
            var listingIdString: String = ""
            var listingId: Int = -1

            // Check if backStackEntry.arguments exists
            if (backStackEntry.arguments != null) {
                // Check if "listingId" is present in the arguments and retrieve it
                val tempId = backStackEntry.arguments!!.getString("listingId")

                // Ensure tempId is not null or empty
                if (!tempId.isNullOrEmpty()) {
                    listingIdString = tempId
                }
            }

            // Try to convert listingIdString to an integer
            listingId = if (listingIdString.isNotEmpty()) {
                try {
                    listingIdString.toInt()  // Convert the string to an integer
                } catch (e: NumberFormatException) {
                    -1  // Set default value if conversion fails
                }
            } else {
                -1  // return if no listings exist
            }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            BackendWrapper.getListings(
                onSuccess = { backendListings ->
                    listings = backendListings
                },
                onError = { error ->
                    errorMessage = error
                }
            )

            // Check if listings have been loaded and if the selected listing exists
            val selectedListing = listings.find { it.id == listingId }

            if (selectedListing != null) {
                // Pass the selected listing to the OwnerListingScreen

                OwnerListingScreen(listing = selectedListing, navController = navController)
            } else {
                // Show error message or fallback
                Text(text = errorMessage ?: "Loading...", modifier = Modifier.fillMaxSize())
            }
        }
    }
}