package com.example.ezyardfrontend

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.ezyardfrontend.BackendWrapper.Companion.bookmarkListing
import com.example.ezyardfrontend.BackendWrapper.Companion.getBookmarkedListings
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

// Suspend function to get user token
suspend fun getUserToken(): String? = suspendCoroutine { continuation ->
    val mUser = FirebaseAuth.getInstance().currentUser
    if (mUser != null) {
        mUser.getIdToken(true).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result?.token
                continuation.resume(token)
            } else {
                continuation.resume(null)
            }
        }
    } else {
        continuation.resume(null)
    }
}

@Composable
fun Listings(
    listing: ListingComponent,
    navController: NavHostController,
    distance: Float?,
    isAccountPage: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    var imageFile by remember { mutableStateOf<File?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var userToken by remember { mutableStateOf<String?>(null) }
    var isBookmarked by remember { mutableStateOf(false) }


    val bitmap = remember(imageFile) {
        BitmapFactory.decodeFile(imageFile?.absolutePath ?: "")
    }

    // Use SnapshotStateList for bookmarks
    var bookmarks by remember { mutableStateOf<Set<Int>>(emptySet()) }

    // Get the context using LocalContext
    val context = LocalContext.current

    // File name for local storage
    val fileName = "listing_${listing.id}.jpg"
    val imagePath = File(context.cacheDir, fileName)

    // Fetch user token and bookmarks when the listing ID changes
    LaunchedEffect(listing.id) {
        userToken = getUserToken()
        userToken?.let { token ->
            getBookmarkedListings(token, onSuccess = { bookmarkedIds ->
                isBookmarked = bookmarkedIds.contains(listing.id)
            }, onError = {
                println("Error getting bookmarked listings")
            })
        }

        // Check if image exists
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
                    AsyncImage(
                        model = imageFile,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.placeholder), // Optional placeholder
                        error = painterResource(R.drawable.placeholder) // Optional error image
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
                        .background(
                            Color.DarkGray.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp)
                        ) // Darker background
                        .padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )  // Smaller padding to shrink the box size
                ) {
                    CountdownTimer(startTime = listing.startTime) // Add the countdown timer here
                }

                // Bookmark Button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(2.dp)
                ) {
                    IconButton(
                        onClick = {
                            isBookmarked = !isBookmarked
                            coroutineScope.launch {
                                userToken?.let { token ->
                                    if (isBookmarked) {
                                        // Unbookmark the listing
                                        bookmarkListing(token, listing.id, onSuccess = {

                                        }, onError = {
                                            println("Error unbookmarking listing")
                                        })
                                    } else {
                                        // Bookmark the listing
                                        bookmarkListing(token, listing.id, onSuccess = {

                                        }, onError = {
                                            println("Error bookmarking listing")
                                        })
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Image(
                            painter = if (isBookmarked || isAccountPage) {
                                painterResource(id = R.drawable.bookmarked) // Replace with your image resource
                            } else {
                                painterResource(id = R.drawable.bookmark) // Replace with your image resource
                            },
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
        //Spacer(modifier = Modifier.height(8.dp))

        // Display listing details as text below the image
        Text(text = "  Name: ${listing.name}", style = MaterialTheme.typography.bodyMedium)
        distance?.let {
            Text(
                text = "  Distance: ${"%.2f".format(it)} miles away",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(text = "  City: ${listing.city}, ${listing.state}")
        Text(text = "  Street: ${listing.streetNumber} ${listing.streetName}, ${listing.zipcode}")
        Text(text = "  Price Range: ${listing.priceRange}")
        Spacer(modifier = Modifier.height(8.dp))
    }


//            Text("Bookmarked${bookmarks.toList().toString()}")
}
