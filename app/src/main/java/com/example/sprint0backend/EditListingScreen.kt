package com.example.sprint0backend

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.sprint0backend.BackendWrapper.Companion.getFilePathFromUri
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
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

    // Formatter for parsing the ISO date-time format
    val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // Formatter for displaying dates in a user-friendly format
    val displayDateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")

    // Initialize startDateTime and endDateTime with parsing
    var startDateTime by remember {
        mutableStateOf(
            listing.startTime?.let { LocalDateTime.parse(it, dateFormatter) }
        )
    }
    var endDateTime by remember {
        mutableStateOf(
            listing.endTime?.let { LocalDateTime.parse(it, dateFormatter) }
        )
    }

    val availableTags = listOf("Clothing", "Electronics", "Toys", "Books", "Miscellaneous")
    var showTagDialog by remember { mutableStateOf(false) }
    var showPrompt by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()

    // SnackbarHostState for managing the Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val hasChanges by derivedStateOf {
        listOf(name, description, city, state, zipcode, priceRange, selectedTags.joinToString(", "),
            startDateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), endDateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        ) != listOf(listing.name, listing.description, listing.city, listing.state, listing.zipcode.toString(),
            listing.priceRange ?: "", listing.tags, listing.startTime, listing.endTime
        )
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
                            startTime = startDateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            endTime = endDateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Listing saved successfully")
                                    navController.popBackStack()
                                }
                            },
                            onError = { showPrompt = true }
                        )
                    }) {
                        Text("Save", style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) } // Add SnackbarHost to display the Snackbar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Display images at the top
            ImageSection(
                listingId = listing.id,
                navController = navController
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display the text fields below images
            ListingTextField(label = "Name", value = name, onValueChange = { name = it })
            ListingTextField(label = "Description", value = description, onValueChange = { description = it })
            ListingTextField(label = "City", value = city, onValueChange = { city = it })
            ListingTextField(label = "State", value = state, onValueChange = { state = it })
            ListingTextField(label = "Zip Code", value = zipcode, onValueChange = { zipcode = it }, keyboardType = KeyboardType.Number)
            ListingTextField(label = "Price Range", value = priceRange, onValueChange = { priceRange = it }, keyboardType = KeyboardType.Number)

            // Start Date/Time Picker
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
                    "Start: ${it.format(displayDateFormatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // End Date/Time Picker
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
                    "End: ${it.format(displayDateFormatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

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


@Composable
fun ImageSection(
    listingId: Int,
    navController: NavHostController // Pass NavController to handle navigation refresh
) {
    val context = LocalContext.current
    var selectedImagePath by remember { mutableStateOf<String?>(null) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }
    var listingImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }

    // Load existing images
    LaunchedEffect(listingId) {
        BackendWrapper.getImageUrlsForListing(
            listingId = listingId,
            onSuccess = { images ->
                listingImages = images
            },
            onError = { error ->
                uploadMessage = "Failed to load images: $error"
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImagePath = getFilePathFromUri(context, uri)
            uploadMessage = null
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        // Image Carousel
        if (listingImages.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                items(listingImages) { imageUrl ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxHeight()
                            .width(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = imageUrl,
                                error = painterResource(R.drawable.placeholder), // Optional placeholder
                                placeholder = painterResource(R.drawable.placeholder) // Optional placeholder
                            ),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        } else {
            // Display a placeholder if there are no images
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No images available")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { launcher.launch("image/*") }) {
            Text("Choose Image from Gallery")
        }

        selectedImagePath?.let { path ->
            val fileName = path.substringAfterLast('/')
            Text("Selected Image: $fileName", style = MaterialTheme.typography.bodyMedium)

            Button(onClick = {
                isUploading = true
                selectedImagePath?.let { filePath ->
                    BackendWrapper.uploadListingImage(
                        listingId = listingId,
                        filePath = filePath,
                        onSuccess = { response ->
                            uploadMessage = "Image uploaded successfully!"
                            selectedImagePath = null
                            isUploading = false

                            navController.navigate("EditListingScreen/$listingId") {
                                popUpTo("EditListingScreen/$listingId") { inclusive = true }
                            }
                        },
                        onError = { error ->
                            uploadMessage = "Failed to upload image: $error"
                            isUploading = false
                        }
                    )
                }
            }) {
                Text("Upload Image")
            }
        }

        uploadMessage?.let { message ->
            Text(message, color = Color.Green, style = MaterialTheme.typography.bodyMedium)
        }
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
