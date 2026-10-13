package com.smartagrimarket.model.network

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object UploadRepository {
    private val api = RetrofitClient.api

    // ──────────────────────────────────────────────────────────────
    // PRODUCT IMAGE UPLOAD
    // Used by the farmer's UploadProductScreen.
    // Returns the Cloudinary URL of the uploaded image.
    // ──────────────────────────────────────────────────────────────
    suspend fun uploadProductImage(
        context: Context,
        uri: Uri,
        bearerToken: String
    ): ApiResult<String> {
        return try {
            val file = uriToTempFile(context, uri, prefix = "product")
                ?: return ApiResult.Error("Could not read the selected image.")

            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData(
                name = "image",           // matches upload.single("image") on the backend
                filename = file.name,
                body = requestBody
            )

            val response = api.uploadProductImage(bearerToken, part)

            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.imageUrl)
            } else {
                ApiResult.Error("Could not upload image (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't upload: ${e.message ?: "unknown error"}")
        } finally {
            // Best-effort cleanup — the OS will also clean the cacheDir eventually
            // but this keeps things tidy.
            try {
                uriToTempFile(context, uri, prefix = "product")?.delete()
            } catch (_: Exception) { /* ignore */ }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // PROFILE PHOTO UPLOAD
    // Used by the AccountSettingsScreen when a user taps their avatar.
    // Returns the Cloudinary URL of the uploaded photo.
    // ──────────────────────────────────────────────────────────────
    suspend fun uploadProfilePhoto(
        context: Context,
        uri: Uri,
        bearerToken: String
    ): ApiResult<String> {
        return try {
            val file = uriToTempFile(context, uri, prefix = "profile")
                ?: return ApiResult.Error("Could not read the selected image.")

            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData(
                name = "photo",           // matches upload.single("photo") on the backend
                filename = file.name,
                body = requestBody
            )

            val response = api.uploadProfilePhoto(bearerToken, part)

            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.photoUrl)
            } else {
                ApiResult.Error("Could not upload photo (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't upload: ${e.message ?: "unknown error"}")
        } finally {
            try {
                uriToTempFile(context, uri, prefix = "profile")?.delete()
            } catch (_: Exception) { /* ignore */ }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // HELPER: copy a content:// URI into a temp file so Retrofit can
    // stream it as multipart/form-data.
    // ──────────────────────────────────────────────────────────────
    private fun uriToTempFile(context: Context, uri: Uri, prefix: String): File? {
        return try {
            val file = File(
                context.cacheDir,
                "${prefix}_${System.currentTimeMillis()}.jpg"
            )
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (file.exists() && file.length() > 0) file else null
        } catch (e: Exception) {
            null
        }
    }
}