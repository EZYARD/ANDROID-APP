package com.example.sprint0backend

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

var userToken by mutableStateOf<String?>(null)

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Back Arrow Button
        IconButton(
            onClick = { navController.navigate("ListingsScreen") },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ezyard),
                contentDescription = "Login image",
                modifier = Modifier.size(200.dp)
            )
            Text(text = "Welcome", fontSize = 28.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Login to your account")

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = "Email Address") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(text = "Password") },
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display error message if login fails
            errorMessage?.let {
                Text(text = it, color = Color.Red)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            // Sign in with Firebase
                            Firebase.auth.signInWithEmailAndPassword(email, password).await()

                            // Retrieve the current user
                            val mUser = FirebaseAuth.getInstance().currentUser
                            mUser?.getIdToken(true)?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Store the token
                                    userToken = task.result?.token
                                    // Navigate to ProfileScreen
                                    navController.navigate("ProfileScreen")
                                } else {
                                    // Handle token retrieval error
                                    errorMessage = task.exception?.localizedMessage
                                }
                            }
                        } catch (e: Exception) {
                            // Display error message if sign-in fails
                            errorMessage = e.localizedMessage
                        }
                    }
                }
            ) {
                Text(text = "Login")
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = {
                // Send password reset email
                coroutineScope.launch {
                    try {
                        Firebase.auth.sendPasswordResetEmail(email).await()
                        errorMessage = "Password reset email sent. Please check your inbox."
                    } catch (e: Exception) {
                        errorMessage = e.localizedMessage
                    }
                }
            }) {
                Text(text = "Forgot Password?")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Text(text = "Or sign in with")

// Spacer(modifier = Modifier.height(4.dp))

// Image(
//     painter = painterResource(id = R.drawable.google),
//     contentDescription = "Google",
//     modifier = Modifier
//         .size(60.dp)
//         .clickable {
//             // Google login logic goes here
//         }
// )

            Spacer(modifier = Modifier.height(4.dp))

            // Add a TextButton to navigate to CreateAccount
            TextButton(onClick = { navController.navigate("CreateAccount") }) {
                Text(text = "Don't have an account? Create one")
            }
        }
    }
}