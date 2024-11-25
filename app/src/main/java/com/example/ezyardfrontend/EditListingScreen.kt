package com.example.ezyardfrontend

import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.example.ezyardfrontend.BackendWrapper.Companion.getFilePathFromUri
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

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
    val rating by remember { mutableStateOf(listing.rating ?: "") }
    val reviews by remember { mutableStateOf(listing.reviews ?: "") }
    var selectedTags by remember { mutableStateOf(listing.tags.split(", ").toSet()) }
    val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val displayDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d h:mm a")

    var startDateTime by remember {
        mutableStateOf(
            listing.startTime.let { LocalDateTime.parse(it, dateFormatter) }
        )
    }
    var endDateTime by remember {
        mutableStateOf(
            listing.endTime.let { LocalDateTime.parse(it, dateFormatter) }
        )
    }
    val availableTags = listOf("Clothing", "Electronics", "Toys", "Books", "Miscellaneous")
    var showTagDialog by remember { mutableStateOf(false) }
    var showPrompt by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var newImages by remember { mutableStateOf<List<String>>(emptyList()) }

    val hasChanges by derivedStateOf {
        newImages.isNotEmpty() ||
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
                        scope.launch {
                            newImages.forEach { imagePath ->
                                BackendWrapper.uploadListingImage(
                                    listingId = listing.id,
                                    filePath = imagePath,
                                    onSuccess = { /* nothing */ },
                                    onError = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Failed to upload image: $imagePath")
                                        }

                                    }
                                )
                            }

                            // Update listing details
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
                                onError = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Failed to save listing")

                                    }
                                }
                            )
                        }
                    }) {
                        Text("Save", style = MaterialTheme.typography.labelLarge)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            ImageSection(
                listingId = listing.id,
                navController = navController,
                newImages = newImages,
                onAddImage = { newPaths -> newImages = newImages + newPaths },
                onRemoveImage = { imagePath -> newImages = newImages - imagePath },
                onRemoveExistingImage = { /* No-op for now */ } // placeholder in case we add remove images
            )

            Spacer(modifier = Modifier.height(16.dp))

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
    navController: NavHostController,
    newImages: List<String>,
    onAddImage: (List<String>) -> Unit,
    onRemoveImage: (String) -> Unit,
    onRemoveExistingImage: (String) -> Unit = {} // I'm keeping this here in case we want to remove existing images
) {
    val context = LocalContext.current
    var listingImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }

    // Fetch existing images
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
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val newPaths = uris.mapNotNull { uri -> getFilePathFromUri(context, uri) }
        onAddImage(newPaths) // Add selected new images to the list
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.inverseSurface,
                shape = RoundedCornerShape(8.dp)
            )
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
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onRemoveExistingImage(imageUrl) }, // Placeholder
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(50))
                                .padding(2.dp)
                        ) {
                            Icon(       // 'X' on the corner of image
                                Icons.Default.Close,
                                contentDescription = "Remove Existing Image",
                                tint = Color.Gray
                            )
                        }
                    }
                }

                //   Spot for new images   //
                items(newImages) { imagePath ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxHeight()
                            .width(300.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(imagePath),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onRemoveImage(imagePath) },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No images available")
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