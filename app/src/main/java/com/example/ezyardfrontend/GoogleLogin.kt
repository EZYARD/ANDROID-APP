import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun GoogleLogin() {
    val context = LocalContext.current
    val activity = context as? Activity

    // Initialize Firebase Auth and Credential Manager
    val auth = remember { FirebaseAuth.getInstance() }
    val credentialManager = remember { CredentialManager.create(context) }

    // Obtain a CoroutineScope tied to this composable
    val coroutineScope = rememberCoroutineScope()

    Button(onClick = {
        // Start the sign-in/sign-up process when the button is clicked
        signInOrSignUpWithGoogle(
            auth = auth,
            credentialManager = credentialManager,
            context = context,
            coroutineScope = coroutineScope,
            activity = activity
        )
    }) {
        Text("Sign in or Sign up")
    }
}

private fun signInOrSignUpWithGoogle(
    auth: FirebaseAuth,
    credentialManager: CredentialManager,
    context: Context,
    coroutineScope: CoroutineScope,
    activity: Activity?
) {
    // Build options for passwords, passkeys, and Google Sign-In
    val getPasswordOption = GetPasswordOption()
    val requestJson = generatePasskeyRequestJson()
    val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(
        requestJson = requestJson
    )

    val getGoogleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true) // Only authorized accounts
        .setServerClientId(WEB_CLIENT_ID)
        .build()

    val getCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(getPasswordOption)
        .addCredentialOption(getPublicKeyCredentialOption)
        .addCredentialOption(getGoogleIdOption)
        .build()

    coroutineScope.launch {
        try {
            val result = credentialManager.getCredential(
                request = getCredentialRequest,
                context = context
            )
            handleSignInResult(
                result = result,
                auth = auth,
                activity = activity
            )
        } catch (e: GetCredentialException) {
            // No credentials found, prompt the user to sign up
            Log.e(TAG, "No credentials found. Prompting for sign-up.", e)
            promptUserToSignUp(
                auth = auth,
                credentialManager = credentialManager,
                context = context,
                coroutineScope = coroutineScope,
                activity = activity
            )
        }
    }
}

private suspend fun promptUserToSignUp(
    auth: FirebaseAuth,
    credentialManager: CredentialManager,
    context: Context,
    coroutineScope: CoroutineScope,
    activity: Activity?
) {
    // Use GetSignInWithGoogleOption to prompt the user to sign up
    val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(
        serverClientId = WEB_CLIENT_ID
    ).build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(signInWithGoogleOption)
        .build()

    try {
        val result = credentialManager.getCredential(
            request = request,
            context = context
        )
        handleSignInResult(
            result = result,
            auth = auth,
            activity = activity
        )
    } catch (e: GetCredentialException) {
        // Handle failure
        Log.e(TAG, "Credential retrieval failed during sign-up.", e)
        // Optionally, show your own sign-up UI
    }
}

private fun handleSignInResult(
    result: GetCredentialResponse,
    auth: FirebaseAuth,
    activity: Activity?
) {
    val credential = result.credential

    when (credential) {
        is PasswordCredential -> {
            val email = credential.id
            val password = credential.password
            firebaseAuthWithEmailPassword(
                email = email,
                password = password,
                auth = auth,
                activity = activity
            )
        }
        is PublicKeyCredential -> {
            val responseJson = credential.authenticationResponseJson
            firebaseAuthWithPasskey(
                responseJson = responseJson,
                auth = auth,
                activity = activity
            )
        }
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    // Authenticate with Firebase using the ID token
                    firebaseAuthWithGoogle(
                        idToken = idToken,
                        auth = auth,
                        activity = activity
                    )
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e(TAG, "Received an invalid Google ID token response", e)
                }
            } else {
                // Handle unrecognized credential type
                Log.e(TAG, "Unexpected type of credential")
            }
        }
        else -> {
            // Handle other credential types if necessary
            Log.e(TAG, "Unexpected type of credential")
        }
    }
}

private fun firebaseAuthWithEmailPassword(
    email: String,
    password: String,
    auth: FirebaseAuth,
    activity: Activity?
) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(activity ?: return) { task ->
            if (task.isSuccessful) {
                // Sign-in successful
                val user = auth.currentUser
                Log.d(TAG, "signInWithEmailAndPassword:success")
            } else {
                // Sign-in failed
                Log.w(TAG, "signInWithEmailAndPassword:failure", task.exception)
            }
        }
}

private fun firebaseAuthWithPasskey(
    responseJson: String,
    auth: FirebaseAuth,
    activity: Activity?
) {
    // Send responseJson to your server for validation
    // This requires server-side implementation of WebAuthn validation
    // Assuming server returns a custom token after validation
    val customToken = getCustomTokenFromServer(responseJson)
    if (customToken != null) {
        auth.signInWithCustomToken(customToken)
            .addOnCompleteListener(activity ?: return) { task ->
                if (task.isSuccessful) {
                    // Sign-in successful
                    val user = auth.currentUser
                    Log.d(TAG, "signInWithCustomToken:success")
                } else {
                    // Sign-in failed
                    Log.w(TAG, "signInWithCustomToken:failure", task.exception)
                }
            }
    } else {
        Log.e(TAG, "Failed to get custom token from server")
    }
}

private fun firebaseAuthWithGoogle(
    idToken: String,
    auth: FirebaseAuth,
    activity: Activity?
) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener(activity ?: return) { task ->
            if (task.isSuccessful) {
                // Sign-in successful
                val user = auth.currentUser
                Log.d(TAG, "signInWithCredential:success")
            } else {
                // Sign-in failed
                Log.w(TAG, "signInWithCredential:failure", task.exception)
            }
        }
}

private fun generatePasskeyRequestJson(): String {
    // Fetch the request options from your server
    // This typically includes challenge, relying party ID, allowed credentials, etc.
    // For demonstration, we'll use a placeholder
    return "{ /* Your request JSON here */ }"
}

private fun getCustomTokenFromServer(responseJson: String): String? {
    // Implement the network call to your server to validate the passkey
    // and return a custom token for Firebase Authentication
    // For demonstration, we'll return null
    return null
}

private const val TAG = "GoogleLogin"
private const val WEB_CLIENT_ID = "573390401337-j5h1nv5jhe703mrd2h1srkkn9v85us4u.apps.googleusercontent.com"
