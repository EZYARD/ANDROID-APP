package com.example.sprint0backend


import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.launch

@Composable
public fun AuthenticationButton(
    buttonText: String,
    onGetCredentialResponse: (Credential) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)

    Button(
        onClick = {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            coroutineScope.launch {
                try {
                    val result = credentialManager.getCredential(request = request, context = context)
                    onGetCredentialResponse(result.credential)
                } catch (e: GetCredentialException) {
                    Log.d("ERROR_TAG", e.message.orEmpty())
                }
            }
        },
        modifier = Modifier
    ) {
        Text(text = buttonText)
    }
}