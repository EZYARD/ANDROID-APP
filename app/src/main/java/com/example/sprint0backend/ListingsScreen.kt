package com.example.sprint0backend

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.runtime.derivedStateOf
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun ListingsScreen(navController: NavHostController) {
    val categories = listOf("Clothing", "Electronics", "Toys", "Books", "Miscellaneous")
    var selectedCategories by rememberSaveable { mutableStateOf(setOf<String>()) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var zipCodeInput by rememberSaveable { mutableStateOf("") }
    var uid by rememberSaveable { mutableStateOf("") }
    val user = Firebase.auth.currentUser
    var listings by rememberSaveable { mutableStateOf<List<ListingComponent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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

    // Filter listings based on selected categories
    val filteredListings by remember {
        derivedStateOf {
            val filteredList = if (selectedCategories.isEmpty()) {
                listings // Show all listings
            } else {
                listings.filter { listing ->
                    val tagsList = listing.tags.split(",").map { it.trim() }
                    tagsList.any { tag -> selectedCategories.contains(tag) }
                }
            }

            // Sort the filtered list to put listings with uid === listing.uid at the top
            filteredList.sortedWith(compareByDescending { listing -> listing.uid == uid })
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        ZipCodeInput(
            zipCodeInput = zipCodeInput,
            onZipCodeChange = { newZipCode ->
                zipCodeInput = newZipCode
            }
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
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(filteredListings) { listing ->
                        Listings(listing = listing, navController = navController)
                    }
                }
            }
        }
    }
}


@Composable
fun ZipCodeInput(zipCodeInput: String, onZipCodeChange: (String) -> Unit) {
    OutlinedTextField(
        value = zipCodeInput,
        onValueChange = {
            if (it.length <= 5 && it.all { char -> char.isDigit() }) {
                onZipCodeChange(it)
            } },
        label = { Text("Enter Zip Code") },
        placeholder = { Text("e.g., 12345") },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
        //.padding(horizontal = 16.dp)
    )

    // This is to provide a line between the ZipCode TextField and the category filters
    // [MAY BE REMOVED IN THE FUTURE]
    HorizontalDivider(
        thickness = 1.dp,
        color = Color.LightGray
    )
}

@Composable
fun CategoryFilter(categories: List<String>, selectedCategories: Set<String>,
                   onCategorySelected: (Set<String>) -> Unit, isExpanded: Boolean, onToggleExpanded: () -> Unit) {
    val summaryText = if (selectedCategories.isEmpty()) {
        "Filter by Categories"
    }
    else {
        "${selectedCategories.size} Categories Selected"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onToggleExpanded() }
            .padding(20.dp)
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
                .background(
                    color = Color.LightGray, // Color could change ub the future
                    //shape = MaterialTheme.shapes.large
                )
                .padding(8.dp)
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

/**
 * A way to filter listings
 *
 * This is included in the main code in the ListingsScreen function
 * [BRING UP IN MEETING FOR POTENTIAL DELETION]
 * */
//private fun FetchAndFilterListings(
//    selectedCategories: Set<String>,
//    allListings: List<ListingComponent>,
//    onLoading: (Boolean) -> Unit,
//    onSuccess: (List<ListingComponent>) -> Unit
//) {
//    onLoading(true)
//
//    // Filter listings based on selected categories
//    val filtered = if (selectedCategories.isEmpty()) {
//        allListings
//    } else {
//        allListings.filter { listing ->
//            val tagsList = listing.tags.split(",").map { it.trim() }
//            tagsList.any { tag -> selectedCategories.contains(tag) }
//        }
//    }
//
//    onLoading(false)
//    onSuccess(filtered)
//}