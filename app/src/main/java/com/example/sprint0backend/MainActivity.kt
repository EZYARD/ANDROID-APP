package com.example.sprint0backend

import MapScreen
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class User(
    val uid: String,
    val token: String
)

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                MainApp()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun MainApp() {
    val navController = rememberNavController()
    var listings by rememberSaveable { mutableStateOf<List<ListingComponent>>(emptyList()) }
    var currentRoute by remember { mutableStateOf("ListingsScreen") }
    var user by rememberSaveable { mutableStateOf<User?>(null) }

    // Initialize Retrofit and BackendSchema using the backend URL from Constants
    val retrofit = Retrofit.Builder()
        .baseUrl(Constants().BACKEND_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val backendService = retrofit.create(BackendSchema::class.java)

    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                firebaseUser.getIdToken(true).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user = User(uid = firebaseUser.uid, token = task.result?.token ?: "")
                    }
                }
            } else {
                user = null
            }
        }
    }
    navController.addOnDestinationChangedListener { _, destination, _ ->
        currentRoute = destination.route ?: "ListingsScreen"
    }

    Scaffold(
        bottomBar = {
            // Only show the BottomNavigationBar when not on the SplashScreen
            if (currentRoute != "SplashScreen" &&
                (currentRoute == "ListingsScreen" ||
                        currentRoute == "SearchScreen" ||
                        currentRoute == "MapScreen" ||
                        currentRoute == "ProfileScreen")
            ) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "SplashScreen", modifier = Modifier.padding(innerPadding)) {
            composable("SplashScreen") {
                SplashScreen(navController = navController)
            }
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
            composable("ForgotPasswordScreen") {
                ForgotPasswordScreen(navController = navController)
            }
            composable("MapScreen") {
                MapScreen()
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

                val selectedListing = listings.find { it.id == listingId }
                if (selectedListing != null) {
                    // Check if the logged-in user is the owner
                    val isOwner = user?.uid == selectedListing.uid
                    OwnerListingScreen(listing = selectedListing, navController = navController, isOwner = isOwner)
                } else {
                    Text(text = errorMessage ?: "Loading...", modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val currentDestination = navController.currentDestination
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

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
                    navController.navigate("ProfileScreen") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                } else {
                    navController.navigate("LoginScreen") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.LocationOn, contentDescription = "Map") },
            label = { Text("Map") },
            selected = currentDestination != null && currentDestination.route == "MapScreen",
            onClick = {
                navController.navigate("MapScreen") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )
    }
}