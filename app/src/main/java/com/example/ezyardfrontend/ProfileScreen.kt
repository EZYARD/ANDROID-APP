package com.example.ezyardfrontend

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ezyardfrontend.BackendWrapper.Companion.getBookmarkedListings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
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
    val bookmarks = remember { mutableStateListOf<Int>() }
    val user = Firebase.auth.currentUser
    val coroutineScope = rememberCoroutineScope()

    // Fetch user info and their listings
    LaunchedEffect(user) {
        if (user != null) {
            userToken = user.getIdToken(true).await().token
            uid = user.uid

            BackendWrapper.getListings(
                onSuccess = { listings ->
                    // Filter the listings to only include the ones owned by the user
                    userListings = listings.filter { it.uid == uid }
                    isLoadingListings = false

                    getBookmarkedListings(userToken!!, onSuccess = { bookmarkedIds ->
                        bookmarks.clear()
                        bookmarks.addAll(bookmarkedIds)

                        // Add all bookmarked listings that aren't already in userListings
                        val bookmarkedListings = listings.filter { it.id in bookmarkedIds && it !in userListings }
                        userListings = userListings + bookmarkedListings // Add them below user listings

                    }, onError = {
                        println("Error getting bookmarked listings")
                    })
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
    Box(modifier = Modifier.fillMaxSize()) {
        // Sign Out button at the top-right corner
        Button(
            onClick = {
                Firebase.auth.signOut()
                sharedPreferences.edit().remove("userToken").apply()
                navController.navigate("LoginScreen")
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(text = "Sign Out")
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
        Spacer(modifier = Modifier.height(150.dp))

        // Bottom Section - Buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { navController.navigate("YourListingsScreen") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Your Listings")
            }
            Button(
                onClick = { navController.navigate("BookmarkedListingsScreen") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Your Bookmarked Listings")
            }
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



