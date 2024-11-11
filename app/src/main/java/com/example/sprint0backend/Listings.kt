package com.example.sprint0backend

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Listings(listing: ListingComponent, navController: NavHostController, distance: Float?) {
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
        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Display Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
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

                // Position Countdown Timer in the top-right corner
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd) // Position in the top-right corner
                        .padding(8.dp)
                        .background(Color.DarkGray.copy(alpha = 0.7f), shape = RoundedCornerShape(4.dp)) // Darker background
                        .padding(horizontal = 12.dp, vertical = 6.dp)  // Smaller padding to shrink the box size
                ) {
                    CountdownTimer(startTime = listing.startTime) // Add the countdown timer here
                }
            }

            Spacer(modifier = Modifier.height(8.dp))


            // Display listing details as text below the image
            Text(text = "Name: ${listing.name}", style = MaterialTheme.typography.bodyMedium)
            distance ?.let {
                Text(text = "Distance: ${it} miles away", style = MaterialTheme.typography.bodyMedium)
            }
            //Text(text = "Description: ${listing.description}")
            Text(text = "City: ${listing.city}, ${listing.state}")
            Text(text = "Street: ${listing.streetNumber} ${listing.streetName}, ${listing.zipcode}")
            Text(text = "Price Range: ${listing.priceRange}")
            //Text(text = "Rating: ${listing.rating}")
            //Text(text = "Start Time: ${listing.startTime}")
            //Text(text = "End Time: ${listing.endTime}")
            //Text(text = "Reviews: ${listing.reviews}")
        }
    }
}

