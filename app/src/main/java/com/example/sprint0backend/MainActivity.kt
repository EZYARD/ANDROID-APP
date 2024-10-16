package com.example.sprint0backend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.*
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

    NavHost(navController = navController, startDestination = "ListingsScreen"){
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
            if (listingIdString.isNotEmpty()) {
                try {
                    listingId = listingIdString.toInt()  // Convert the string to an integer
                } catch (e: NumberFormatException) {
                    listingId = -1  // Set default value if conversion fails
                }
            } else {
                listingId = -1  // return if no listings exist
            }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            // Variables for listing and loading/error states
//            var listings by remember { mutableStateOf<List<ListingComponent>>(emptyList()) }
//
//
//            // Uncomment for backend data:
//            LaunchedEffect(Unit) {
//                getBackendListings(
//                    onSuccess = { backendListings ->
//                        listings = backendListings
//                    },
//                    onError = { error ->
//                        errorMessage = error
//                    }
//                )
//            }

            // Uncomment this for hardcoded data:
            var listings = getHardcodedListings()

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