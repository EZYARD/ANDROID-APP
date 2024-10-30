package com.example.sprint0backend

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box


@Composable
fun CreateListingScreen(navController: NavHostController) {
    var title by remember { mutableStateOf("") } // Field for the title of the yard sale
    var date by remember { mutableStateOf("") } // Field for the date
    var time by remember { mutableStateOf("") } // Field for the time
    var areaCode by remember { mutableStateOf("") } // Field for area code
    var selectedTags by remember { mutableStateOf(setOf<String>()) } // Field for selected tags/categories
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Column for the form fields
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp), // Add padding at the top to avoid overlap with the back button
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Input field for Title of Yard Sale
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title of Yard Sale") })

            // Input field for Date
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") })

            // Input field for Time
            OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time") })

            // Input field for Area Code
            OutlinedTextField(value = areaCode, onValueChange = { areaCode = it }, label = { Text("Area Code") })

            // Categories / Tags section (to be filled out)
            // TODO: Add UI components for selecting tags (e.g., FilterChip similar to SearchScreen)

            Button(
                onClick = {
                    coroutineScope.launch {
                        // This is just for UI purposes, no action taken
                        // You can add backend connection code here later
                        // val newListing = ListingComponent(...)
                    }
                }
            ) {
                Text("Create Listing")
            }

            errorMessage?.let {
                Text(text = it, color = Color.Red)
            }
        }

        // Back button to navigate to Profile screen
        IconButton(
            onClick = { navController.navigate("ProfileScreen") },
            modifier = Modifier
                .align(Alignment.TopStart) // Align to top start of the box
                .padding(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
    }
}