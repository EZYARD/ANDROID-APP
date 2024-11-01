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
fun EditListingScreen(listing: ListingComponent, navController: NavHostController, backendService: BackendSchema) {
    var name by remember { mutableStateOf(listing.name) }
    var description by remember { mutableStateOf(listing.description) }
    var city by remember { mutableStateOf(listing.city) }
    var state by remember { mutableStateOf(listing.state) }
    var zipcode by remember { mutableStateOf(listing.zipcode.toString()) }
    var priceRange by remember { mutableStateOf(listing.priceRange.toString()) }
//    var tags by remember { mutableStateOf(listing.tags) }

    var selectedTags by remember { mutableStateOf(listing.tags.split(", ").toSet()) }
    val availableTags = listOf("Clothing", "Electronics", "Toys", "Books", "Miscellaneous")
    var showTagDialog by remember { mutableStateOf(false) }

    var showPrompt by remember { mutableStateOf(false) }
    var originalValues = remember(listing) {
        listOf(listing.name, listing.description, listing.city, listing.state, listing.zipcode.toString(), listing.priceRange.toString(), listing.tags)
    }

    val hasChanges by derivedStateOf {
        listOf(name, description, city, state, zipcode, priceRange, selectedTags.joinToString(", ")) != originalValues    }

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
                        originalValues = listOf(name, description, city, state, zipcode, priceRange, tagsString)
                        // save logic goes here in the future
                        listing.reviews?.let {
                            saveListingChanges(
                                backendService = backendService,
                                listingId = listing.id,
                                name = name,
                                streetNumber = listing.streetNumber.toString(),
                                streetName = listing.streetName,
                                city = city,
                                state = state,
                                zipcode = zipcode.toIntOrNull() ?: 0,
                                description = description,
                                tags = tagsString,
                                priceRange = (priceRange.toIntOrNull() ?: 0).toString(),
                                rating = listing.rating.toString(),
                                reviews = it,
                                navController = navController,
                                onSaveFailure = { showPrompt = true }
                            )
                        }
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

fun saveListingChanges(
    backendService: BackendSchema,
    listingId: Int,
    name: String,
    streetNumber: String,
    streetName: String,
    city: String,
    state: String,
    zipcode: Int,
    description: String,
    tags: String,
    priceRange: String,
    rating: String,
    reviews: String,
    navController: NavHostController,
    onSaveFailure: () -> Unit
) {
    val parsedStreetNumber = streetNumber.toIntOrNull() ?: 0

    val call = backendService.updateListing(
        listingId = listingId,
        name = name,
        streetNumber = parsedStreetNumber,
        streetName = streetName,
        city = city,
        state = state,
        zipcode = zipcode,
        description = description,
        tags = tags.ifEmpty { null },
        priceRange = priceRange.ifEmpty { null },
        rating = rating.ifEmpty { null },
        reviews = reviews.ifEmpty { null }
    )

    call.enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                navController.popBackStack()
            } else {
                onSaveFailure()
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            onSaveFailure()
        }
    })
}
