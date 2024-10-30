package com.example.sprint0backend

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListingScreen(listing: ListingComponent, navController: NavHostController) {
    var name by remember { mutableStateOf(listing.name) }
    var description by remember { mutableStateOf(listing.description) }
    var city by remember { mutableStateOf(listing.city) }
    var state by remember { mutableStateOf(listing.state) }
    var zipcode by remember { mutableStateOf(listing.zipcode.toString()) }
    var priceRange by remember { mutableStateOf(listing.priceRange.toString()) }
    var tags by remember { mutableStateOf(listing.tags) }

    var showPrompt by remember { mutableStateOf(false) }
    var originalValues = remember(listing) {
        listOf(listing.name, listing.description, listing.city, listing.state, listing.zipcode.toString(), listing.priceRange.toString(), listing.tags)
    }

    val hasChanges by derivedStateOf {
        listOf(name, description, city, state, zipcode, priceRange, tags) != originalValues
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Listing") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) showPrompt = true else navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        originalValues = listOf(name, description, city, state, zipcode, priceRange, tags)
                        // save logic goes here in the future
                    }) {
                        Text("Save", style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            ListingTextField(label = "Name", value = name, onValueChange = { name = it })
            ListingTextField(label = "Description", value = description, onValueChange = { description = it })
            ListingTextField(label = "City", value = city, onValueChange = { city = it })
            ListingTextField(label = "State", value = state, onValueChange = { state = it })
            ListingTextField(label = "Zip Code", value = zipcode, onValueChange = { zipcode = it }, keyboardType = KeyboardType.Number)
            ListingTextField(label = "Price Range", value = priceRange, onValueChange = { priceRange = it }, keyboardType = KeyboardType.Number)
            ListingTextField(label = "Tags", value = tags, onValueChange = { tags = it }, placeholder = "e.g., Electronics, Toys")

            Spacer(modifier = Modifier.height(16.dp))

            // Placeholder for image upload(UNDECIDED)
            ImageSection()
        }
    }
    if (showPrompt) {
        UnsavedChangesDialog(
            onConfirm = { navController.popBackStack() },
            onDismiss = { showPrompt = false }
        )
    }
}

@Composable
fun ListingTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
fun ImageSection() {
    Text("Images", style = MaterialTheme.typography.bodyMedium)
    Text("[IMAGES DISPLAYED HERE]", color = Color.Gray, modifier = Modifier.padding(8.dp))

    TextButton(
        onClick = { /* Does nothing right now*/ },
        enabled = false,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text("Edit Images (WIP)")
    }
}

@Composable
fun UnsavedChangesDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unsaved Changes") },
        text = { Text("You have unsaved changes. Are you sure you want to go back?") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Yes") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("No") }
        }
    )
}
