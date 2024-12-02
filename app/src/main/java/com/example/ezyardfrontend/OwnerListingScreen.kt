package com.example.ezyardfrontend

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerListingScreen(listing: ListingComponent, navController: NavHostController, isOwner: Boolean) {
    var imageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var newReviewText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(listing.id) {
        coroutineScope.launch {
            // Fetch images
            BackendWrapper.getImageUrlsForListing(
                listing.id,
                onSuccess = { urls ->
                    imageUrls = urls
                    isLoading = false
                },
                onError = { error ->
                    errorMessage = error
                    isLoading = false
                }
            )

            // Fetch reviews with error handling for empty or missing reviews
            BackendWrapper.getReviews(
                listing.id,
                onSuccess = { fetchedReviews ->
                    reviews = fetchedReviews
                },
                onError = { error ->
                    // Handle specific error for no reviews differently
                    if (error.contains("No reviews found", ignoreCase = true)) {
                        reviews = emptyList() // Treat as no reviews rather than an error
                    } else {
                        errorMessage = error // Display other errors
                    }
                }
            )
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listing Details", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isOwner) {
                        IconButton(onClick = {
                            navController.navigate("EditListingScreen/${listing.id}")
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Listing")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            errorMessage != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMessage!!)
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Carousel
                Text(
                    text = "Photos",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (imageUrls.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        items(imageUrls) { imageUrl ->
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
                                        error = painterResource(R.drawable.placeholder),
                                        placeholder = painterResource(R.drawable.placeholder)
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(MaterialTheme.shapes.medium),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No images available")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Name: ${listing.name}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Address: \n${listing.streetNumber} ${listing.streetName}, \n${listing.city}, ${listing.state} ${listing.zipcode}",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                DisplayFormattedDate(
                    startTime = listing.startTime,
                    endTime = listing.endTime
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Price Range: ${listing.priceRange}",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Rating: ${listing.rating ?: "N/A"}",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (listing.tags.isNotEmpty()) {
                    Text(
                        text = "Categories:",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    listing.tags.split(",").forEach { tag ->
                        Text(text = "- $tag", fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Description: ${listing.description}",
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.bodyMedium
                )

                // Reviews Section
                Text(
                    text = "Reviews",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (reviews.isNotEmpty()) {
                    reviews.forEach { review ->
                        Text(
                            text = "${review.uid}: ${review.review}",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                } else {
                    Text(
                        text = "No reviews available",
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                androidx.compose.material3.TextField(
                    value = newReviewText,
                    onValueChange = { newReviewText = it },
                    placeholder = { Text("Write your review...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.Button(
                    onClick = {
                        coroutineScope.launch {
                            val mUser = FirebaseAuth.getInstance().currentUser
                            mUser?.getIdToken(true)?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val token = task.result?.token
                                    if (token != null) {
                                        BackendWrapper.createReview(
                                            idToken = token, // Replace with actual ID token
                                            listingId = listing.id,
                                            review = newReviewText,
                                            onSuccess = {
                                                reviews = reviews + Review(
                                                    id = reviews.size + 1,
                                                    uid = mUser.uid,
                                                    review = newReviewText
                                                )
                                                newReviewText = ""
                                            },
                                            onError = { error ->
                                                errorMessage = error
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Submit")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.height(16.dp))


            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DisplayFormattedDate(startTime: String, endTime: String) {
    val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val outputFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d h:mm a")

    val formattedStartTime = LocalDateTime.parse(startTime, inputFormatter).format(outputFormatter)
    val formattedEndTime = LocalDateTime.parse(endTime, inputFormatter).format(outputFormatter)

    Text(
        text = "Date: \n$formattedStartTime - \n$formattedEndTime",
        fontSize = 20.sp,
        style = MaterialTheme.typography.bodyMedium
    )
}
