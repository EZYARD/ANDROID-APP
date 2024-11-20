package com.example.sprint0backend

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.example.sprint0backend.BackendWrapper.Companion.testAuth
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(navController: NavHostController) {
    val sharedPreferences = LocalContext.current.getSharedPreferences("auth", Context.MODE_PRIVATE)
    var userToken by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var uid by remember { mutableStateOf<String?>(null) }

    val user = Firebase.auth.currentUser
    val coroutineScope = rememberCoroutineScope()

    // Retrieve the token from SharedPreferences if user is not authenticated yet
    LaunchedEffect(user) {
        if (user != null) {
            userToken = user.getIdToken(true).await().token
            uid = user.uid
            testAuth(userToken!!, onSuccess = { println(it) }, onError = { println(it) })
        } else {
            userToken = sharedPreferences.getString("userToken", null)
            if (userToken != null) {
                testAuth(userToken!!, onSuccess = { println(it) }, onError = { println(it) })
            } else {
                errorMessage = "No token found. Please log in again."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top, // Align elements to the top
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Section - Logo and Welcome Message
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ezyard),
                contentDescription = "Profile image",
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (user != null || userToken != null) {
                Text(
                    text = "Welcome, ${user?.email ?: "User"}!",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "User ID: $uid",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else {
                Text(
                    text = "You are not signed in.",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp)) // Add a gap between sections

        // Middle Section - Buttons
        if (user != null || userToken != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {
                    Firebase.auth.signOut()
                    sharedPreferences.edit().remove("userToken").apply()
                    navController.navigate("LoginScreen")
                }) {
                    Text(text = "Sign Out")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { navController.navigate("CreateListingScreen") }) {
                    Text(text = "Create a Listing")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showDeleteConfirmation = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(text = "Delete Account", color = Color.White)
                }
            }
        } else {
            Button(onClick = { navController.navigate("LoginScreen") }) {
                Text(text = "Login")
            }
        }

        // Error Message Section
        errorMessage?.let {
            Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
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
