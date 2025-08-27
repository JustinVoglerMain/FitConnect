package com.example.fitconnect.tools

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.fitconnect.BuildConfig
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException


private const val TAG = "ImageTools"
private const val IMGUR_URL = "https://api.imgur.com/3/image"

class ImageTools {

    /**
     * Gets the file extension of an image from the given uri
     *
     * @param uri the uri to get the image extension from
     * @param context the context that is using this function
     * @return the image extension if possible, null otherwise
     */
    private fun getFileExtension(context: Context, uri: Uri): String? {
        Log.d(TAG, "getFileExtension() called for uri: $uri")
        return when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> {
                val mime = MimeTypeMap.getSingleton()
                val extension = mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
                Log.d(TAG, "File extension from MIME type: $extension")
                extension
            }

            ContentResolver.SCHEME_FILE -> {
                val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                Log.d(TAG, "File extension from URL: $extension")
                extension
            }

            else -> {
                Log.e(TAG, "Unknown scheme for URI: $uri")
                null
            }
        }
    }


    /**
     * Deletes an image from the imgur database
     *
     * @param deleteHash the hash code related to the image to delete
     * @param callback true if deletion was successful, false otherwise
     */
    fun deleteImageFromImgur(deleteHash: String, callback: (Boolean) -> Unit) {
        Log.d(TAG, "deleteImageFromImgur() called")
        val accessToken = BuildConfig.IMGUR_ACCESS_TOKEN

        val request = Request.Builder()
            .url("$IMGUR_URL/$deleteHash")
            .addHeader("Authorization", "Bearer $accessToken")
            .delete()
            .build()
        val okHttpClient: OkHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Delete failed: ${e.message}")
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Image Deleted Successfully")
                    callback(true)
                } else {
                    Log.e(TAG, "Delete error: ${response.message}")
                    callback(false)
                }
            }
        })
    }

    /**
     * Gets the file path from the user's phone
     *
     * @param context the context of the activity/fragment for posting
     * @param uri the uri related to the image
     * @return the filepath of the image if found, null otherwise
     */
    private fun getFilePathFromURI(context: Context, uri: Uri): String? {
        Log.d(TAG, "getFilePathFromURI() called with uri: $uri")
        val contentResolver: ContentResolver = context.contentResolver
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        return if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val filePath = cursor.getString(columnIndex)
            cursor.close()
            Log.d(TAG, "File path for uri: $uri is $filePath")
            filePath
        } else {
            Log.e(TAG, "Error getting file path from uri $uri")
            null
        }
    }

    /**
     * Uploads an image to the imgur database
     *
     * @param context the context where the image is created
     * @param uri the uri of the image
     * @param callback the url of the image and the delete hash code if successfully adding, (null, null) otherwise
     */
    fun uploadImageToImgur(context: Context, uri: Uri, callback: (String?, String?) -> Unit) {
        Log.d(TAG, "uploadImageToImgur() called")
        val accessToken = BuildConfig.IMGUR_ACCESS_TOKEN

        val filepath = getFilePathFromURI(context, uri)
        if (filepath == null) {
            Log.e(TAG, "Image file path is null for uri: $uri")
            callback(null, null)
            return
        }
        val imageFile = File(filepath)

        if (!imageFile.exists()) {
            Log.e(TAG, "Image file does not exist at path: $filepath")
            callback(null, null)
            return
        }

        // Log file details for debugging
        Log.d(TAG, "Image file size: ${imageFile.length()} bytes")
        Log.d(TAG, "Image file path: $filepath")

        val fileExtension = getFileExtension(context, uri) ?: "png"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "image.$fileExtension", imageFile.asRequestBody())
            .build()

        val request = Request.Builder()
            .url(IMGUR_URL)
            .addHeader("Authorization", "Bearer $accessToken")
            .post(requestBody)
            .build()

        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Upload failed: ${e.message}")
                Handler(Looper.getMainLooper()).post {
                    callback(null, null)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Upload successful")
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody ?: "")
                    val imgUrl = jsonResponse.getJSONObject("data").getString("link")
                    val deleteHash = jsonResponse.getJSONObject("data").getString("deletehash")
                    Handler(Looper.getMainLooper()).post {
                        callback(imgUrl, deleteHash)
                    }
                } else {
                    Log.d(TAG, "Upload error: ${response.code} - ${response.message}")
                    val responseBody = response.body?.string()
                    Log.d(TAG, "Response body: $responseBody")
                    Handler(Looper.getMainLooper()).post {
                        callback(null, null)
                    }
                }
            }
        })
    }
}