package com.example.ezyardfrontend

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


@Composable
fun ListingsScreen(navController: NavHostController) {
    val categories = listOf("Clothing", "Electronics", "Toys", "Books", "Miscellaneous")
    var selectedCategories by rememberSaveable { mutableStateOf(setOf<String>()) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var uid by rememberSaveable { mutableStateOf("") }
    val user = Firebase.auth.currentUser
    var listings by rememberSaveable { mutableStateOf<List<ListingComponent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var locationState by rememberSaveable { mutableStateOf("Location not set") }
    var rangeState: Float? by rememberSaveable { mutableStateOf(null) }
    var showLocationDialog by rememberSaveable { mutableStateOf(false) }
    var listByLocation by rememberSaveable { mutableStateOf<List<RangeListingResponse>>(emptyList()) }

    // Fetch listings only once when the screen loads
    LaunchedEffect(Unit) {
        BackendWrapper.getListings(
            onSuccess = { backendListings ->
                listings = backendListings
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    LaunchedEffect(user) {
        if (user != null) {
            uid = user.uid
        }
    }

// Filter listings based on selected categories and location
    val filteredListings by remember {
        derivedStateOf {
            // Step 1: Filter by selected categories if any, otherwise show all listings
            val filteredList = if (selectedCategories.isEmpty()) {
                listings // Show all listings
            } else {
                listings.filter { listing ->
                    val tagsList = listing.tags.split(",").map { it.trim() }
                    tagsList.any { tag -> selectedCategories.contains(tag) }
                }
            }

            // Step 2: Filter the list to only include listings that have matching IDs in listByLocation
            val locationFilteredList = if (listByLocation.isNotEmpty()) {
                filteredList.filter { listing ->
                    listByLocation.any { rangeListing -> rangeListing.id == listing.id }
                }
            } else {
                filteredList // If listByLocation is empty, retain the original filtered list
            }

            // Step 3: Sort the list to put listings with uid === listing.uid at the top
            locationFilteredList.sortedWith(compareByDescending { listing -> listing.uid == uid })
        }
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp) // Add padding at the top
    ) {
        LocationSelector(
            locationState = locationState,
            onLocationClick = { showLocationDialog = true }
        )

        CategoryFilter(
            categories = categories,
            selectedCategories = selectedCategories,
            onCategorySelected = { updatedCategories ->
                selectedCategories = updatedCategories
            },
            isExpanded = isExpanded,
            onToggleExpanded = { isExpanded = !isExpanded }
        )

        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "An error occurred while fetching listings.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            filteredListings.isNotEmpty() -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 102.dp) // Add bottom padding
                ) {
                    items(filteredListings) { listing ->
                        Listings(listing = listing, navController = navController, distance = listByLocation.find { it.id == listing.id }?.distance_miles)
                    }
                }

            }
        }

        if (showLocationDialog) {
            LocationDialog(
                onDismiss = { showLocationDialog = false },
                onUpdate = { newLocation, range ->
                    locationState = newLocation
                    rangeState = range
                    showLocationDialog = false

                    if (rangeState != 200.0f) {
                        // Fetch listings based on location and range
                        BackendWrapper.getListingsByRange(newLocation, range,
                            onSuccess = { backendListings ->
                                listByLocation = backendListings
                            },
                            onError = { error ->
                                errorMessage = error
                                listByLocation = emptyList()
                            }
                        )
                    } else {
                        BackendWrapper.getListingsByRange(newLocation, 9.9999998E10f,
                            onSuccess = { backendListings ->
                                listByLocation = backendListings
                            },
                            onError = { error ->
                                errorMessage = error
                                listByLocation = emptyList()
                            }
                        )
                    }
                }
            )
        }
    }
}


@Composable
fun LocationSelector(
    locationState: String,
    onLocationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLocationClick() }
            .border(0.dp, Color.Gray, shape = RectangleShape)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            tint = Color.Black
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = locationState,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )
    }
}

@Composable
fun LocationDialog(
    onDismiss: () -> Unit,
    onUpdate: (String, Float) -> Unit
) {
    var locationInput by remember { mutableStateOf("") }
    var range by remember { mutableStateOf(200f) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Set Location") },
        text = {
            Column {
                OutlinedTextField(
                    value = locationInput,
                    onValueChange = { locationInput = it },
                    label = { Text("Enter Zip Code or Address") },
                    placeholder = { Text("e.g., 12345 or 123 Main St") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Select Range: ${if (range != 200f) range.toInt() else "∞"} miles")
                Slider(
                    value = range,
                    onValueChange = { range = it },
                    valueRange = 0f..200f,
                    steps = 10,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onUpdate(locationInput, range)
            }) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CategoryFilter(
    categories: List<String>, selectedCategories: Set<String>,
    onCategorySelected: (Set<String>) -> Unit, isExpanded: Boolean, onToggleExpanded: () -> Unit
) {
    val summaryText = if (selectedCategories.isEmpty()) {
        "         Filter by Categories"
    } else {
        "${selectedCategories.size} Categories Selected"
    }
g
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, shape = RectangleShape)
            .clickable { onToggleExpanded() }
            .padding(16.dp)
    ) {
        Text(text = summaryText, color = Color.Black)
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            modifier = Modifier.align(Alignment.CenterEnd),
            tint = Color.Black
        )
    }

    if (isExpanded) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                //.padding(horizontal = 5.dp)
                .border(1.dp, Color.DarkGray, shape = RectangleShape)
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategories.contains(category),
                        onClick = {
                            val updatedCategories = if (selectedCategories.contains(category)) {
                                selectedCategories - category
                            } else {
                                selectedCategories + category
                            }
                            onCategorySelected(updatedCategories)
                        },
                        label = { Text(category) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                    )
                }
            }
        }
    }
}

