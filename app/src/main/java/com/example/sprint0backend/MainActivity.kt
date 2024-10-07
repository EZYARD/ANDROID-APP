package com.example.sprint0backend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sprint0backend.ui.theme.Sprint0BackendTheme

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Sprint0BackendTheme {
                MainScreen()
            }
        }
    }
}

@Preview
@Composable
fun MainScreen() {
    // State to hold the listings data fetched from the API
    var listings by remember { mutableStateOf<List<ListingComponent>>(emptyList()) }
    // State to manage loading status
    var isLoading by remember { mutableStateOf(true) }
    // State to hold any error messages
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch data from the API when the composable is launched
    // `LaunchedEffect(Unit)` ensures that the API call is made only once when the composable is first displayed
    LaunchedEffect(Unit) {
        RetrofitInstance.api.getListings().enqueue(object : Callback<List<ListingComponent>> {
            // Handles successful response from the server
            override fun onResponse(
                call: Call<List<ListingComponent>>,
                response: Response<List<ListingComponent>>
            ) {
                if (response.isSuccessful) {
                    // Update the listings state with the fetched data
                    listings = response.body() ?: emptyList()
                    isLoading = false
                } else {
                    // Set error message if response is not successful
                    errorMessage = "Failed to load data: ${response.errorBody()}"
                    isLoading = false
                }
            }

            // Handles failure of the network call
            override fun onFailure(call: Call<List<ListingComponent>>, t: Throwable) {
                // Set error message if there's a network failure
                errorMessage = "Network error: ${t.message}"
                isLoading = false
            }
        })
    }

    //  basic structure to manage the visual components (UI layout)
    Scaffold(
        content = { paddingValues ->
            // Display different content based on the current state (loading, error, or display listings)
            when {
                isLoading -> {
                    // Display a circular loading spinner while the data is being fetched
                    CircularProgressIndicator(modifier = Modifier.fillMaxSize())
                }
                errorMessage != null -> {
                    // Display an error message if the network call failed or the response was not successful
                    Text(
                        text = errorMessage!!,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
                else -> {
                    // Display the list of listings once the data has been successfully fetched
                    ListingsScreen(listings = listings)
                }
            }
        }
    )
}

//********************************DEFAULT STUFF**************************************//

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            Surface(color = MaterialTheme.colorScheme.background) {
//                MainScreen()
//            }
//        }
//    }



//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            Sprint0BackendTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    Sprint0BackendTheme {
//        Greeting("Android")
//    }
//}