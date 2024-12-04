package com.example.ezyardfrontend

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ezyardfrontend.BackendWrapper.Companion.getBookmarkedListings
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkedListingsScreen(navController: NavHostController) {
    val sharedPreferences = LocalContext.current.getSharedPreferences("auth", Context.MODE_PRIVATE)
    var userToken by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userListings by remember { mutableStateOf<List<ListingComponent>>(emptyList()) }
    var isLoadingListings by remember { mutableStateOf(true) }
    val bookmarks = remember { mutableStateListOf<Int>() }
    val user = Firebase.auth.currentUser
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(user) {
        if (user != null) {
            userToken = user.getIdToken(true).await().token
            val uid = user.uid

            BackendWrapper.getListings(
                onSuccess = { listings ->
                    isLoadingListings = false

                    getBookmarkedListings(userToken!!, onSuccess = { bookmarkedIds ->
                        bookmarks.clear()
                        bookmarks.addAll(bookmarkedIds)
                        userListings = listings.filter { it.id in bookmarkedIds }
                    }, onError = {
                        errorMessage = "Error getting bookmarked listings"
                        isLoadingListings = false
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
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Bookmarked Listings") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            when {
                isLoadingListings -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                errorMessage != null -> {
                    Text(
                        text = "Error: $errorMessage",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                userListings.isEmpty() -> {
                    Text(
                        text = "No bookmarked listings available.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(paddingValues)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Bookmarked Listings",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }

                        items(userListings) { listing ->
                            Listings(
                                listing = listing,
                                navController = navController,
                                distance = null,
                                isAccountPage = false
                            )
                        }
                    }
                }
            }
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
    }
}