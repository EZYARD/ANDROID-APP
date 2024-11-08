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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListingScreen(
    listing: ListingComponent,
    navController: NavHostController,
    backendService: BackendSchema
) {
    var name by remember { mutableStateOf(listing.name) }
    var description by remember { mutableStateOf(listing.description) }
    var city by remember { mutableStateOf(listing.city) }
    var state by remember { mutableStateOf(listing.state) }
    var zipcode by remember { mutableStateOf(listing.zipcode.toString()) }
    var priceRange by remember { mutableStateOf(listing.priceRange ?: "") }
    var rating by remember { mutableStateOf(listing.rating ?: "") }
    var reviews by remember { mutableStateOf(listing.reviews ?: "") }
    var selectedTags by remember { mutableStateOf(listing.tags.split(", ").toSet()) }
    val availableTags = listOf("Clothing", "Electronics", "Toys", "Books", "Miscellaneous")
    var showTagDialog by remember { mutableStateOf(false) }
    var showPrompt by remember { mutableStateOf(false) }

    val hasChanges by derivedStateOf {
        listOf(name, description, city, state, zipcode, priceRange, selectedTags.joinToString(", ")) !=
                listOf(listing.name, listing.description, listing.city, listing.state, listing.zipcode.toString(), listing.priceRange ?: "", listing.tags)
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
                        val tagsString = selectedTags.joinToString(", ")
                        BackendWrapper.updateListing(
                            listingId = listing.id,
                            name = name.takeIf { it.isNotBlank() },
                            streetNumber = listing.streetNumber.takeIf { it != 0 },
                            streetName = listing.streetName.takeIf { it.isNotBlank() },
                            city = city.takeIf { it.isNotBlank() },
                            state = state.takeIf { it.isNotBlank() },
                            zipcode = zipcode.toIntOrNull(),
                            description = description.takeIf { it.isNotBlank() },
                            tags = tagsString.takeIf { it.isNotBlank() },
                            priceRange = priceRange.takeIf { it.isNotBlank() },
                            rating = rating.takeIf { it.isNotBlank() },
                            reviews = reviews.takeIf { it.isNotBlank() },
                            onSuccess = { navController.popBackStack() },
                            onError = { showPrompt = true } // Show prompt on failure
                        )
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
            ListingTextField(label = "Rating", value = rating, onValueChange = { rating = it })
            ListingTextField(label = "Reviews", value = reviews, onValueChange = { reviews = it })

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { showTagDialog = true }) {
                Text("Edit Tags")
            }
            Text("Selected Tags: ${selectedTags.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
        }
    }

    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Select Tags") },
            text = {
                Column {
                    availableTags.forEach { tag ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = tag in selectedTags,
                                onCheckedChange = {
                                    selectedTags = if (it) selectedTags + tag else selectedTags - tag
                                }
                            )
                            Text(text = tag)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text("OK")
                }
            }
        )
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

/**
 * As of right now, this function isn't used.
 *
 * This would contain the implementation to upload
 * and change the images of the listing
 * */
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
