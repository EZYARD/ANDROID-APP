package com.example.sprint0backend

import OwnerListingScreen
import android.content.ContentValues.TAG
import android.credentials.GetCredentialException
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                MainApp()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun GoogleSignInButton() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val onClick: () -> Unit = {
        val credentialManager = CredentialManager.create(context)

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("303294655432-0d6a54k63l7s0fj2f0jh7eh45769raup.apps.googleusercontent.com")
            .setNonce(hashedNonce)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )
                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                Log.i(TAG, googleIdToken)
                Toast.makeText(context, "You are signed in!", Toast.LENGTH_SHORT).show()
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Credential exception: ${e.message}", e)
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            } catch (e: GoogleIdTokenParsingException) {
                Log.e(TAG, "Token parsing exception: ${e.message}", e)
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Button with padding for better visibility
    Button(onClick = onClick, modifier = Modifier.padding(16.dp)) {
        Text("Sign in with Google")
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Preview
@Composable
fun MainApp() {
    val navController = rememberNavController()
    var listings by rememberSaveable { mutableStateOf<List<ListingComponent>>(emptyList()) }

    // Using a Column to layout the components
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Google Sign-In Button (placed first to ensure it's visible)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            GoogleSignInButton()
        } else {
            Text("Google Sign-In is not supported on this Android version.")
        }

        // NavHost for navigation between screens
        NavHost(navController = navController, startDestination = "ListingsScreen") {
            composable("ListingsScreen") {
                ListingsScreen(navController = navController)
            }
            composable("OwnerListingScreen/{listingId}") { backStackEntry ->
                var listingIdString: String = ""
                var listingId: Int = -1

                if (backStackEntry.arguments != null) {
                    val tempId = backStackEntry.arguments!!.getString("listingId")
                    if (!tempId.isNullOrEmpty()) {
                        listingIdString = tempId
                    }
                }

                listingId = if (listingIdString.isNotEmpty()) {
                    try {
                        listingIdString.toInt()
                    } catch (e: NumberFormatException) {
                        -1
                    }
                } else {
                    -1
                }

                BackendWrapper.getListings(
                    onSuccess = { backendListings ->
                        listings = backendListings
                    },
                    onError = {
                        // Handle error (optional)
                    }
                )

                val selectedListing = listings.find { it.id == listingId }
                if (selectedListing != null) {
                    OwnerListingScreen(listing = selectedListing, navController = navController)
                } else {
                    Text(text = "Loading...", modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}