package com.example.ezyardfrontend

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ezyardfrontend.BackendWrapper.Companion.testAuth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun CreateAccount(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Create Account", fontSize = 28.sp)

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

        // Display error message if account creation fails
        errorMessage?.let {
            Text(text = it, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(onClick = {
            // Validate email and password
            if (email.isBlank() || password.isBlank()) {
                errorMessage = "Please fill in both fields."
                return@Button
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                errorMessage = "Please enter a valid email address."
                return@Button
            }

            if (password.length < 6) {
                errorMessage = "Password must be at least 6 characters."
                return@Button
            }

            // Create account logic
            val auth = Firebase.auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Retrieve the current user
                        val mUser = FirebaseAuth.getInstance().currentUser
                        mUser?.getIdToken(true)?.addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                // Store the token
                                userToken = task2.result?.token
                                testAuth(userToken!!, onSuccess = { println(it) }, onError = { println(it) })
                                // Navigate to ProfileScreen
                                navController.navigate("ProfileScreen")
                            } else {
                                // Handle token retrieval error
                                errorMessage = task2.exception?.localizedMessage
                            }
                        }
                    } else {
                        // Handle account creation failure
                        errorMessage = task.exception?.localizedMessage
                    }
                }
        }) {
            Text(text = "Create Account")
        }
    }
}
