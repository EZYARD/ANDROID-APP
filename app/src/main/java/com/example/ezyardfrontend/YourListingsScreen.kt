package com.example.ezyardfrontend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourListingsScreen(navController: NavHostController) {
    val user = FirebaseAuth.getInstance().currentUser
    var userListings by remember { mutableStateOf<List<ListingComponent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user) {
        if (user != null) {
            BackendWrapper.getListings(
                onSuccess = { listings ->
                    userListings = listings.filter { it.uid == user.uid }
                    isLoading = false
                },
                onError = { error ->
                    println("Error getting listings: $error")
                    isLoading = false
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp) // Add some padding around the screen
    ) {
        TopAppBar(
            title = { Text("Your Listings") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                userListings.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "You have no listings yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate("CreateListingScreen") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Create a Listing")
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        userListings.forEach { listing ->
                            Listings(
                                listing = listing,
                                navController = navController,
                                distance = null,
                                isAccountPage = true
                            )
                        }
                    }
                }
            }
        }
    }
}