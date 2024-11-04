package com.example.sprint0backend

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BackendWrapper {
    companion object {
        // Helper function to make network calls
        private fun <T, R> makeCall(
            call: Call<T>,
            transform: (T?) -> R,
            onSuccess: (R) -> Unit,
            onError: (String) -> Unit
        ) {
            call.enqueue(object : Callback<T> {
                override fun onResponse(
                    call: Call<T>,
                    response: Response<T>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        onSuccess(transform(body))
                    } else {
                        onError("Failed to load data: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    onError("Network error: ${t.message}")
                }
            })
        }

        fun testAuth(
            idToken: String,
            onSuccess: (String) -> Unit,
            onError: (String) -> Unit
        ) {
            RetrofitInstance.api.testAuth("Bearer $idToken").enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        onSuccess(response.body() ?: "No response from server")
                    } else {
                        onError("Failed to verify: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    onError("Network error: ${t.message}")
                }
            })
        }

        // call to return a list of listing components from the backend
        fun getListings(
            onSuccess: (List<ListingComponent>) -> Unit,
            onError: (String) -> Unit
        ) {
            makeCall(
                call = RetrofitInstance.api.getListings(),
                transform = { body ->
                    body ?: emptyList()
                },
                onSuccess = onSuccess,
                onError = onError
            )
        }

        // call to return a list of image urls that are connected to a listing id from the backend
        fun getImageUrlsForListing(
            listingId: Int,
            onSuccess: (List<String>) -> Unit,
            onError: (String) -> Unit
        ) {
            makeCall(
                call = RetrofitInstance.api.getListingImages(listingId),
                transform = { body ->
                    body?.map { image -> "${Constants().BACKEND_URL.dropLast(1)}${image.download_url}" }
                        ?: emptyList()
                },
                onSuccess = onSuccess,
                onError = onError
            )
        }

        fun getListingsByRange(
            location: String,
            range: Float,
            onSuccess: (List<RangeListingResponse>) -> Unit,
            onError: (String) -> Unit
        ) {
            makeCall(
                call = RetrofitInstance.api.getRangeListings(location, range),
                transform = { body ->
                    body ?: emptyList()
                },
                onSuccess = onSuccess,
                onError = onError
            )
        }

        fun createListing(
            idToken: String,
            listingRequest: ListingCreateRequest,
            onSuccess: (ListingComponent) -> Unit,
            onError: (String) -> Unit
        ) {
            val call = RetrofitInstance.api.createListing("Bearer $idToken", listingRequest)
            call.enqueue(object : Callback<ListingComponent> {
                override fun onResponse(call: Call<ListingComponent>, response: Response<ListingComponent>) {
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) } ?: onError("Empty response from server")
                    } else {
                        onError("Failed to create listing: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ListingComponent>, t: Throwable) {
                    onError("Network error: ${t.message}")
                }
            })
        }
    }
}
