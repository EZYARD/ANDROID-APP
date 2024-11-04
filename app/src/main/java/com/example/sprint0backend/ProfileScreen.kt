package com.example.sprint0backend

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.sp
import com.example.sprint0backend.BackendWrapper.Companion.testAuth
import kotlinx.coroutines.tasks.await


@Composable
fun ProfileScreen(navController: NavHostController) {
    // Get the current user from Firebase Auth
    val user = Firebase.auth.currentUser
    val coroutineScope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var uid by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user) {
        if (user != null) {
            try {
                userToken = user.getIdToken(true).await().token
                uid = user.uid
                testAuth(userToken!!, onSuccess = { println(it) }, onError = { println(it) })
            } catch (e: Exception) {
                errorMessage = e.localizedMessage
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back Arrow Button
            IconButton(
                onClick = { navController.navigate("ListingsScreen") },
                modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            // Welcome Image
            Image(
                painter = painterResource(id = R.drawable.ezyard),
                contentDescription = "Profile image",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (user != null) {
                // User is signed in
                Text(
                    text = "Welcome, ${user.email}!",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Here you will find the settings and components for the User Profile.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "User ID: $uid",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )


                // Sign Out Button
                Button(onClick = {
                    Firebase.auth.signOut()
                    userToken = null
                    navController.navigate("LoginScreen")
                }) {
                    Text(text = "Sign Out")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigate to CreateListingScreen Button
                Button(
                    onClick = { navController.navigate("CreateListingScreen") },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "Create a Listing")
                }

                // Delete Account Button
                Button(
                    onClick = { showDeleteConfirmation = true }, // Show confirmation dialog
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(150.dp, 48.dp) // Increased size for better visibility
                ) {
                    Text(text = "Delete Account", fontSize = 14.sp) // Larger font size for readability
                }

                // Display error message if any action fails
                errorMessage?.let {
                    Text(text = it, color = Color.Red)
                }
            } else {
                // User is not signed in
                Text(
                    text = "You are not signed in.",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TextButton(onClick = {
                    navController.navigate("LoginScreen")
                }) {
                    Text(text = "Login")
                }
            }
        }

        // Confirmation dialog for account deletion
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
                                navController.navigate("LoginScreen")
                            } catch (e: Exception) {
                                errorMessage = e.localizedMessage
                            }
                        }
                        showDeleteConfirmation = false // Close dialog after action
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
}