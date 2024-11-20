package com.example.sprint0backend

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.sprint0backend.BackendWrapper.Companion.testAuth
import kotlinx.coroutines.tasks.await

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val sharedPreferences = LocalContext.current.getSharedPreferences("auth", Context.MODE_PRIVATE)
    var userToken by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var uid by remember { mutableStateOf<String?>(null) }
    var userListings by remember { mutableStateOf<List<ListingComponent>>(emptyList()) }
    var isLoadingListings by remember { mutableStateOf(true) }

    val user = Firebase.auth.currentUser
    val coroutineScope = rememberCoroutineScope()

    // Fetch user info and their listings
    LaunchedEffect(user) {
        if (user != null) {
            userToken = user.getIdToken(true).await().token
            uid = user.uid

            BackendWrapper.getListings(
                onSuccess = { listings ->
                    userListings = listings.filter { it.uid == uid }
                    isLoadingListings = false
                },
                onError = { error ->
                    errorMessage = error
                    isLoadingListings = false
                }
            )
        } else {
            userToken = sharedPreferences.getString("userToken", null)
            if (userToken == null) {
                errorMessage = "No token found. Please log in again."
            }
        }
    }

    // Make the entire screen scrollable
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 128.dp), // Add padding for navigation bar
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp)) // Lower the logo and welcome section

        // Top Section with centered content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.wrapContentHeight()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ezyard),
                contentDescription = "Profile image",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 16.dp)
            )
            if (user != null || userToken != null) {
                Text(
                    text = "Welcome, ${user?.email ?: "User"}!",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "User ID: ${uid ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "You are not signed in.",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Listings Section
        Text(
            text = "Your Listings",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            when {
                isLoadingListings -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                userListings.isEmpty() -> {
                    Text(
                        text = "Your listings will be displayed here when created.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        userListings.forEach { listing ->
                            Listings(listing = listing, navController = navController, distance = null)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Space between listings and buttons

        // Bottom Section - Buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    Firebase.auth.signOut()
                    sharedPreferences.edit().remove("userToken").apply()
                    navController.navigate("LoginScreen")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Sign Out")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { navController.navigate("CreateListingScreen") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Create a Listing")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { showDeleteConfirmation = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Delete Account", color = Color.White)
            }
        }
    }

    // Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(text = "Confirm Deletion") },
            text = { Text(text = "Are you sure you want to delete your account? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        try {
                            Firebase.auth.currentUser!!.delete().await()
                            sharedPreferences.edit().remove("userToken").apply()
                            navController.navigate("LoginScreen")
                        } catch (e: Exception) {
                            errorMessage = e.localizedMessage
                        }
                    }
                    showDeleteConfirmation = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("No")
                }
            }
        )
    }
}



