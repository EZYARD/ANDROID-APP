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
        composable("OwnerListingScreen/{owner}") { backStackEntry ->
            var owner = backStackEntry.arguments?.getString("owner")
            if (owner == null) {
                owner = "Unknown"
            }
            OwnerListingScreen(owner = owner, navController = navController)
        }
    }
}
