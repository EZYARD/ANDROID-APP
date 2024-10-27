package com.example.sprint0backend

import OwnerListingScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth


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
    var currentRoute by remember { mutableStateOf("ListingsScreen") }

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentRoute = destination.route ?: "ListingsScreen"
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute == "ListingsScreen" ||
                currentRoute == "SearchScreen" ||
                currentRoute == "ProfileScreen") {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "ListingsScreen", modifier = Modifier.padding(innerPadding)) {
            composable("ListingsScreen") {
                ListingsScreen(navController = navController)
            }
            composable("SearchScreen") {
                SearchScreen(navController = navController)
            }
            composable("ProfileScreen") {
                ProfileScreen(navController = navController)
            }
            composable("LoginScreen") {
                LoginScreen(navController = navController)
            }
            composable("CreateAccount") {  // Add this line for CreateAccount
                CreateAccount(navController = navController)
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
}

/**
 * This will add a navigation bar at the bottom of the screen
 *
 * For now, 'ListingsScreen' has the 'home' icon
 * */
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val currentDestination = navController.currentDestination
    val auth = FirebaseAuth.getInstance() // Initialize FirebaseAuth
    val user = auth.currentUser // Get the current user

    NavigationBar(
        containerColor = Color.LightGray
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Listings") },
            label = { Text("Listings") },
            selected = currentDestination != null && currentDestination.route == "ListingsScreen",
            onClick = {
                navController.navigate("ListingsScreen") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Search") },
            selected = currentDestination != null && currentDestination.route == "SearchScreen",
            onClick = {
                navController.navigate("SearchScreen") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentDestination != null && currentDestination.route == "ProfileScreen",
            onClick = {
                if (user != null) {
                    // If user is signed in, navigate to ProfileScreen
                    navController.navigate("ProfileScreen") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                } else {
                    // If user is not signed in, navigate to LoginScreen
                    navController.navigate("LoginScreen") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            }
        )
    }
}