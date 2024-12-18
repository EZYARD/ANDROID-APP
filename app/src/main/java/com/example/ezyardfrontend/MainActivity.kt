package com.example.ezyardfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory

data class User(
    val uid: String,
    val token: String
)

private lateinit var auth: FirebaseAuth

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
    auth = Firebase.auth

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
    ) {
        NavHost(navController = navController, startDestination = "SplashScreen") {
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
        containerColor = Color.LightGray,
        modifier = Modifier
            .height(96.dp)
            .shadow(8.dp, RoundedCornerShape(0.dp))
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