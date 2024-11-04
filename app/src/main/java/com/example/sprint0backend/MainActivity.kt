package com.example.sprint0backend

import OwnerListingScreen
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun MainApp() {
    val navController = rememberNavController()
    var listings by rememberSaveable { mutableStateOf<List<ListingComponent>>(emptyList()) }
    var currentRoute by remember { mutableStateOf("ListingsScreen") }
    val user = Firebase.auth.currentUser
    var uid by remember { mutableStateOf<String?>(null) }

    // Initialize Retrofit and BackendSchema using the backend URL from Constants
    val retrofit = Retrofit.Builder()
        .baseUrl(Constants().BACKEND_URL) // Use the BACKEND_URL from Constants
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val backendService = retrofit.create(BackendSchema::class.java)

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentRoute = destination.route ?: "ListingsScreen"
        }
    }

    LaunchedEffect(user) {
        if (user != null) {
            uid = user.uid
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
            composable("ProfileScreen") {
                ProfileScreen(navController = navController)
            }
            composable("LoginScreen") {
                LoginScreen(navController = navController)
            }
            composable("CreateAccount") {  // Add this line for CreateAccount
                CreateAccount(navController = navController)
            }
            composable("CreateListingScreen") {  // Add this line for CreateAccount
                CreateListingScreen(navController = navController)
            }
            composable("EditListingScreen/{listingId}") { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId")?.toIntOrNull() ?: -1
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
                    EditListingScreen(
                        listing = selectedListing,
                        navController = navController,
                        backendService = backendService
                    )
                }
            }
            composable("OwnerListingScreen/{listingId}") { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId")?.toIntOrNull() ?: -1
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
                    selectedListing.let {
                        val isOwner = uid == selectedListing.uid
                        OwnerListingScreen(listing = it, navController = navController, isOwner = isOwner)
                    }

                    //OwnerListingScreen(listing = selectedListing, navController = navController, isOwner = true)
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