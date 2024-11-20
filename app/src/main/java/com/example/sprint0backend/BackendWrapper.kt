package com.example.sprint0backend

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

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

        fun updateListing(
            listingId: Int,
            name: String?,
            streetNumber: Int?,
            streetName: String?,
            city: String?,
            state: String?,
            zipcode: Int?,
            description: String?,
            tags: String?,
            priceRange: String?,
            rating: String?,
            reviews: String?,
            startTime: String?, // New parameter
            endTime: String?,   // New parameter
            onSuccess: () -> Unit,
            onError: (String) -> Unit
        ) {
            val call = RetrofitInstance.api.updateListing(
                listingId = listingId,
                name = name,
                streetNumber = streetNumber,
                streetName = streetName,
                city = city,
                state = state,
                zipcode = zipcode,
                description = description,
                tags = tags,
                priceRange = priceRange,
                rating = rating,
                reviews = reviews,
                startTime = startTime, // Pass startTime to Retrofit call
                endTime = endTime      // Pass endTime to Retrofit call
            )

            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onError("Failed to update listing: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    onError("Network error: ${t.message}")
                }
            })
        }


        fun uploadListingImage(
            listingId: Int,
            filePath: String,
            onSuccess: (ImageResponse) -> Unit,
            onError: (String) -> Unit
        ) {
            try {
                val file = File(filePath)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val call = RetrofitInstance.api.uploadImage(
                    listingId = listingId,
                    file = body
                )

                call.enqueue(object : Callback<ImageResponse> {
                    override fun onResponse(call: Call<ImageResponse>, response: Response<ImageResponse>) {
                        if (response.isSuccessful) {
                            response.body()?.let { onSuccess(it) }
                        } else {
                            onError("Failed to upload image: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                        onError("Network error: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                onError("Error preparing file for upload: ${e.message}")
            }
        }


        /**
         * Helper function to retrieve the file path from a URI(images, videos, or documents)
         */
        fun getFilePathFromUri(context: Context, uri: Uri): String? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    cursor.moveToFirst()
                    val fileName = cursor.getString(nameIndex)
                    val fileSize = cursor.getLong(sizeIndex)
                }
            }

            var filePath: String? = null
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    filePath = it.getString(columnIndex)
                }
            }
            return filePath
        }


        fun bookmarkListing(
            idToken: String,
            listingId: Int,
            onSuccess: (Void) -> Unit,
            onError: (String) -> Unit
        ) {
            val call = RetrofitInstance.api.createBookmark("Bearer $idToken", listingId)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) } ?: onError("Empty response from server")
                    } else {
                        onError("Failed to create listing: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    onError("Network error: ${t.message}")
                }
            })
        }

        fun getBookmarkedListings(
            idToken: String,
            onSuccess: (List<Int>) -> Unit,
            onError: (String) -> Unit
        ) {
            val call = RetrofitInstance.api.getBookmarks("Bearer $idToken")
            call.enqueue(object : Callback<BookmarkList> {
                override fun onResponse(call: Call<BookmarkList>, response: Response<BookmarkList>) {
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it.bookmarked_listings) } ?: onError("Empty response from server")
                    } else {
                        onError("Failed to get bookmarked listings: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<BookmarkList>, t: Throwable) {
                    onError("Network error: ${t.message}")
                }
            })
        }
    }
}
