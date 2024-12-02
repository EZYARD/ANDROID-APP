package com.example.ezyardfrontend

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.example.ezyardfrontend.BackendWrapper.Companion.createListing
import com.example.ezyardfrontend.BackendWrapper.Companion.getBookmarkedListings
import com.example.ezyardfrontend.BackendWrapper.Companion.getFilePathFromUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

private var imageListingId: Int = 0
private val user = Firebase.auth.currentUser
private var uid = ""

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

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val visualDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d h:mm a")
    val calendar = Calendar.getInstance()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var newImages by remember { mutableStateOf<List<String>>(emptyList()) }

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 64.dp )
                .fillMaxWidth()
        ) {
            item {
                UploadImageSection(
                    newImages = newImages,
                    onAddImage = { newPaths -> newImages = newImages + newPaths },
                    onRemoveImage = { imagePath -> newImages = newImages - imagePath }
                )
                Spacer(modifier = Modifier.height(16.dp))

                ListingTextField(label = "Name", value = name, onValueChange = { name = it })
                ListingTextField(label = "Description", value = description, onValueChange = { description = it })
                ListingTextField(label = "City", value = city, onValueChange = { city = it })
                ListingTextField(label = "State", value = state, onValueChange = { state = it })
                ListingTextField(label = "Street", value = street, onValueChange = { street = it })
                ListingTextField(label = "Street Number", value = streetNumber, onValueChange = { streetNumber = it }, keyboardType = KeyboardType.Number)
                ListingTextField(
                    label = "Zip Code",
                    value = zipcode,
                    onValueChange = {
                        if (it.length <= 5 && it.all { char -> char.isDigit() })
                        {
                            zipcode = it
                        } },
                    keyboardType = KeyboardType.Number)
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
                        "Start: ${it.format(visualDateFormatter)}",
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
                        "End: ${it.format(visualDateFormatter)}",
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
                        if(name.isNotEmpty() && streetNumber.isNotEmpty() && street.isNotEmpty() && city.isNotEmpty() &&
                            state.isNotEmpty() && zipcode.isNotEmpty() && description.isNotEmpty() && startDateTime != null &&
                            endDateTime != null && selectedTags.isNotEmpty() && priceRange.isNotEmpty())
                        {
                            if(newImages.isEmpty())
                            {
                                scope.launch {
                                    snackbarHostState.showSnackbar("You must have at least one image!")
                                }
                                return@Button
                            }
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
                                        onSuccess = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Listing created successfully!")
                                                initiateImageUpload(
                                                    navController = navController,
                                                    newImages = newImages
                                                )
                                            }
                                        },
                                        onError = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Error Creating Listing!")
                                            }
                                        }
                                    )
                                    // Navigate to ProfileScreen
                                    //navController.navigate("ListingsScreen")
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Error Creating Listing!")
                                    }
                                }
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("You must fill out all fields!")
                            }
                            return@Button
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

@Composable
fun UploadImageSection(
    newImages: List<String>,
    onAddImage: (List<String>) -> Unit,
    onRemoveImage: (String) -> Unit
) {

    val context = LocalContext.current
    var listingImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val newPaths = uris.mapNotNull { uri -> getFilePathFromUri(context, uri) }
        onAddImage(newPaths)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.inverseSurface, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "Images",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (listingImages.isNotEmpty() || newImages.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().height(250.dp)
            ) {
                // Display new images (added by user)
                items(newImages) { imagePath ->
                    Box(
                        modifier = Modifier.padding(4.dp).fillMaxHeight().width(300.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(imagePath),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = {
                                onRemoveImage(imagePath)
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(50))
                                .padding(2.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove New Image")
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No images added")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Add Images")
        }

        uploadMessage?.let { message ->
            Text(
                text = message,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


fun initiateImageUpload(
    navController: NavHostController,
    newImages: List<String>
) {
    var userListings: List<ListingComponent> = emptyList()

    if (user != null) {
        uid = user.uid

        BackendWrapper.getListings(
            onSuccess = { listings ->
                userListings = listings.filter { it.uid == uid }
                val targetListing = userListings.maxByOrNull { it.id }
                if (targetListing != null) {
                    imageListingId = targetListing.id
                }
                newImages.forEach { imagePath ->
                    BackendWrapper.uploadListingImage(
                        listingId = imageListingId,
                        filePath = imagePath,
                        onSuccess = {
                            println("SUCCESS")
                            navController.navigate("ProfileScreen")

                        },
                        onError = {
                            println("FAIL")

                        }
                    )
                }
            },
            onError = {
                println("ERROR")
            }
        )
    }
}
