package com.example.sprint0backend

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

object ImageUpload {
    fun uploadListingImage(
        listingId: Int,
        filePath: String,
        backendSchema: BackendSchema,
        onSuccess: (ImageResponse?) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val file = File(filePath)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val call = backendSchema.uploadImage(listingId, filePart)
        call.enqueue(object : Callback<ImageResponse> {
            override fun onResponse(call: Call<ImageResponse>, response: Response<ImageResponse>) {
                if (response.isSuccessful) {
                    onSuccess(response.body())
                } else {
                    onFailure(Exception("Image upload failed: ${response.errorBody()?.string()}"))
                }
            }

            override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                onFailure(t)
            }
        })
    }
}
