package com.example.sprint0backend

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.tasks.await


@Composable
fun ProfileScreen(navController: NavHostController) {
    // Get the current user from Firebase Auth
    val user = Firebase.auth.currentUser
    val coroutineScope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Add padding around the column
        verticalArrangement = Arrangement.SpaceBetween, // Space between items
        horizontalAlignment = Alignment.CenterHorizontally // Center the content horizontally
    ) {
        // Back Arrow Button
        IconButton(
            onClick = { navController.navigate("ListingsScreen") }, // Navigate directly to ListingsScreen
            modifier = Modifier
                .align(Alignment.Start) // Align the button to the start
                .padding(bottom = 16.dp) // Space below the button
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        // Welcome Image (if applicable, you can add your app's logo or image)
        Image(
            painter = painterResource(id = R.drawable.ezyard), // Replace with your logo image
            contentDescription = "Profile image",
            modifier = Modifier.size(200.dp) // Set size as needed
        )

        Spacer(modifier = Modifier.height(16.dp)) // Space below the image

        if (user != null) {
            // User is signed in
            Text(
                text = "Welcome, ${user.displayName ?: user.email}!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp) // Space below the welcome text
            )
            Text(
                text = "Here you will find the settings and components for the User Profile.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp) // Space below the description text
            )

            // Sign Out Button
            Button(
                onClick = {
                    Firebase.auth.signOut() // Sign out from Firebase
                    navController.navigate("LoginScreen") // Navigate to Login screen
                }
            ) {
                Text(text = "Sign Out")
            }

            Spacer(modifier = Modifier.height(16.dp)) // Space above the delete button

            // Delete Account Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            Firebase.auth.currentUser!!.delete().await() // Delete user account
                            navController.navigate("LoginScreen") // Navigate to Login screen after deletion
                        } catch (e: Exception) {
                            errorMessage = e.localizedMessage // Display error message if deletion fails
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red) // Red color for delete button
            ) {
                Text(text = "Delete Account")
            }

            TextButton(onClick = { navController.navigate("CreateListingsScreen") }) {
                Text(text = "Don't have an account? Create one")
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
                modifier = Modifier.padding(bottom = 8.dp) // Space below the message
            )
            TextButton(onClick = {
                // Navigate to LoginScreen
                navController.navigate("LoginScreen")
            }) {
                Text(text = "Login")
            }
        }
    }
}