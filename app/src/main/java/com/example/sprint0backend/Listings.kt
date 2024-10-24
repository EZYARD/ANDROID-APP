package com.example.sprint0backend

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

// Utility function to save image to local storage
suspend fun saveImageToLocalStorage(
    context: Context,
    imageUrl: String,
    imageFileName: String
): File? {
    return withContext(Dispatchers.IO) {
        try {
            val input = URL(imageUrl).openStream() // Download the image
            val bitmap = BitmapFactory.decodeStream(input)
            val file = File(context.cacheDir, imageFileName) // Save the image in cache directory

            FileOutputStream(file).use { out ->
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    100,
                    out
                ) // Compress the bitmap to a file
            }

            file // Return the file path
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Composable
fun Listings(listing: ListingComponent, navController: NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    var imageFile by remember { mutableStateOf<File?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }

    // Get the context using LocalContext
    val context = LocalContext.current

    // File name for local storage
    val fileName = "listing_${listing.id}.jpg"
    val imagePath = File(context.cacheDir, fileName)

    // Check if the image is already in local storage
    LaunchedEffect(listing.id) {
        if (imagePath.exists()) {
            // If the image exists in local storage, load it directly
            imageFile = imagePath
        } else {
            // Fetch image URL from the backend and download it
            BackendWrapper.getImageUrlsForListing(
                listing.id,
                onSuccess = { imageUrls ->
                    imageUrl = imageUrls.firstOrNull()
                    imageUrl?.let { url ->
                        // Launch a coroutine to save the image to local storage
                        coroutineScope.launch {
                            val savedFile = saveImageToLocalStorage(context, url, fileName)
                            imageFile = savedFile
                        }
                    }
                },
                onError = {
                    // Handle error case if necessary
                    imageUrl = null
                }
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate("OwnerListingScreen/${listing.id}")
            },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (imageFile != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageFile),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Display listing details as text below the image
            Text(text = "Name: ${listing.name}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Description: ${listing.description}")
            Text(text = "City: ${listing.city}, ${listing.state}")
            Text(text = "Street: ${listing.streetNumber} ${listing.streetName}, ${listing.zipcode}")
            Text(text = "Price Range: ${listing.priceRange}")
            Text(text = "Rating: ${listing.rating}")
            Text(text = "Start Time: ${listing.startTime}")
            Text(text = "End Time: ${listing.endTime}")
            Text(text = "Reviews: ${listing.reviews}")
        }
    }
}

