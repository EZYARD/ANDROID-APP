package com.example.sprint0backend

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.navigation.NavHostController

//@Preview
@Composable
fun ListingsScreen(navController: NavHostController) {
    var listings by remember { mutableStateOf<List<ListingComponent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        // Get backend listings //////////////////////////////////////
//                getBackendListings(
//            onSuccess = { backendListings ->
//                listings = backendListings
//                isLoading = false
//            },
//            onError = { error ->
//                errorMessage = error
//                isLoading = false
//            }
//        )
        // end of backend listings ///////////////////////////////////

        // get hardcoded listings ////////////////////////////////////

        listings = listings + getHardcodedListings()
        isLoading = false

        // end of hardcoded listings /////////////////////////////////
    }

    // Display loading indicator, error message, or listings based on the state
    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
    } else if (errorMessage != null) {
        Text(text = errorMessage!!, modifier = Modifier.fillMaxSize())
    } else {
        // Display the listings (hardcoded or backend) in a LazyColumn
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(listings) { listing ->
                Listings(listing = listing, navController = navController)
            }
        }
    }
}

fun getBackendListings(
    onSuccess: (List<ListingComponent>) -> Unit,
    onError: (String) -> Unit
) {
    RetrofitInstance.api.getListings().enqueue(object : Callback<List<ListingComponent>> {
        override fun onResponse(
            call: Call<List<ListingComponent>>,
            response: Response<List<ListingComponent>>
        ) {
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    onSuccess(body)
                } else {
                    onSuccess(emptyList())
                }
            } else {
                onError("Failed to load data: ${response.errorBody()}")
            }
        }

        override fun onFailure(call: Call<List<ListingComponent>>, t: Throwable) {
            onError("Network error: ${t.message}")
        }
    })
}

/////////////////FOR TEST USE ONLY/////////////////////
//////////////////HARDCODED STUFF/////////////////////

fun getHardcodedListings(): List <ListingComponent> {
    return listOf(
        // the following is hard coded info for testing purposes
        ListingComponent(
            id = 1,
            owner = "Max Holloway",
            address = "123 plae ground stret",
            picture = R.drawable.image4090,
            date = "Sept 30, 9 AM - 5 PM",
            tags = listOf("Electronics", "Books", "Clothes", "Sports"),
            priceRange = "$1 - $2,499",
            rating = 4.3f
        ),
        ListingComponent(
            id = 2,
            owner = "Alex Volk",
            address = "Kangaroo Australia",
            picture = R.drawable.alienwarex14r22, // Use the same image or a different one
            date = "Oct 10, 10 AM - 4 PM",
            tags = listOf("Furniture", "Electronics"),
            priceRange = "$10 - $700",
            rating = 4.6f
        ),
        ListingComponent(
            id = 3,
            owner = "Joe Biden",
            address = "123 White House Blvd",
            picture = R.drawable.ps5pro, // Use the same image or another
            date = "Oct 22, 8 AM - 1 PM",
            tags = listOf("Toys", "Books", "Games"),
            priceRange = "$5 - $1000",
            rating = 4.9f
        // end of testing info
        )
    )
}
