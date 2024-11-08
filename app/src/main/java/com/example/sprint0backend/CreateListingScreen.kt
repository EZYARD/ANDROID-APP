package com.example.sprint0backend

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.sprint0backend.BackendWrapper.Companion.createListing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var streetNumber by remember { mutableStateOf("") }
    var zipcode by remember { mutableStateOf("") }
    var priceRange by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    val availableTags = listOf("Clothing", "Electronics", "Toys", "Books", "Miscellaneous")
    var showTagDialog by remember { mutableStateOf(false) }
    var startDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var endDateTime by remember { mutableStateOf<LocalDateTime?>(null) }

    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d h:mm a")
    val calendar = Calendar.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Listing") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            item {
                ListingTextField(label = "Name", value = name, onValueChange = { name = it })
                ListingTextField(label = "Description", value = description, onValueChange = { description = it })
                ListingTextField(label = "City", value = city, onValueChange = { city = it })
                ListingTextField(label = "State", value = state, onValueChange = { state = it })
                ListingTextField(label = "Street", value = street, onValueChange = { street = it })
                ListingTextField(label = "Street Number", value = streetNumber, onValueChange = { streetNumber = it })
                ListingTextField(label = "Zip Code", value = zipcode, onValueChange = { zipcode = it }, keyboardType = KeyboardType.Number)
                ListingTextField(label = "Price Range", value = priceRange, onValueChange = { priceRange = it }, keyboardType = KeyboardType.Number)

                Spacer(modifier = Modifier.height(16.dp))

                // Enhanced Start Date/Time Picker
                Button(
                    onClick = {
                        DatePickerDialog(navController.context, { _, year, month, day ->
                            TimePickerDialog(navController.context, { _, hour, minute ->
                                startDateTime = LocalDateTime.of(year, month + 1, day, hour, minute)
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(text = "Pick Start Date/Time")
                }
                startDateTime?.let {
                    Text(
                        "${it.format(dateFormatter)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Enhanced End Date/Time Picker
                Button(
                    onClick = {
                        DatePickerDialog(navController.context, { _, year, month, day ->
                            TimePickerDialog(navController.context, { _, hour, minute ->
                                endDateTime = LocalDateTime.of(year, month + 1, day, hour, minute)
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(text = "Pick End Date/Time")
                }
                endDateTime?.let {
                    Text(
                        "${it.format(dateFormatter)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { showTagDialog = true }) {
                    Text("Add Tags")
                }
                Text(
                    "Selected Tags: ${selectedTags.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )

                Button(
                    onClick = {
                        val listing = ListingCreateRequest(
                            name = name,
                            streetNumber = streetNumber.toInt(),
                            streetName = street,
                            city = city,
                            state = state,
                            zipcode = zipcode.toInt(),
                            description = description,
                            startTime = startDateTime?.format(dateFormatter).orEmpty(),
                            endTime = endDateTime?.format(dateFormatter).orEmpty(),
                            tags = selectedTags.joinToString(", "),
                            priceRange = priceRange
                        )
                        val mUser = FirebaseAuth.getInstance().currentUser
                        mUser?.getIdToken(true)?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Store the token
                                userToken = task.result?.token
                                createListing(
                                    idToken = userToken!!,
                                    listingRequest = listing,
                                    onSuccess = { println("Listing created") },
                                    onError = { println("Error creating listing") }
                                )
                                // Navigate to ProfileScreen
                                navController.navigate("ListingsScreen")
                            } else {
                                // Do nothing
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
                ) {
                    Text(text = "Create Listing")
                }
            }
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
}
