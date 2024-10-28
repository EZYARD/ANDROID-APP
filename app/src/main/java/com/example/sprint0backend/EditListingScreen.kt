package com.example.sprint0backend

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListingScreen(
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Listing") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Listing")
                    }
                }
            )
        }
    ) {
        // Empty body, just a placeholder to verify navigation works
        Text("Edit Listing Screen", modifier = Modifier.padding(16.dp))
    }
}