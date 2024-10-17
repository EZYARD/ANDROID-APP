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
    }
}
