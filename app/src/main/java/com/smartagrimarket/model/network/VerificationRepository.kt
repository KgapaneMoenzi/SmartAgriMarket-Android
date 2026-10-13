package com.smartagrimarket.model.network

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

object VerificationRepository {

    suspend fun fetchStatus(): ApiResult<VerificationStatusResponse> {
        val token = TokenStore.token
            ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = RetrofitClient.api.getVerificationStatus("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error("Could not load status (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server.")
        }
    }

    suspend fun submitVerification(
        context: Context,
        idDocumentUri: Uri,
        certificateUri: Uri,
        farmName: String,
        farmTown: String,
        farmPrimaryCrops: List<String>,
        farmSizeHectares: String,  // kept as string, server parses to float
        phone: String
    ): ApiResult<VerificationRecordDto> {
        val token = TokenStore.token
            ?: return ApiResult.Error("Please sign in again.")

        return try {
            val idBytes = context.contentResolver.openInputStream(idDocumentUri)?.use {
                it.readBytes()
            } ?: return ApiResult.Error("Could not read ID document.")

            val certBytes = context.contentResolver.openInputStream(certificateUri)?.use {
                it.readBytes()
            } ?: return ApiResult.Error("Could not read certificate.")

            val idPart = MultipartBody.Part.createFormData(
                "idDocument", "id.jpg",
                idBytes.toRequestBody("image/*".toMediaTypeOrNull())
            )
            val certPart = MultipartBody.Part.createFormData(
                "certificate", "certificate.jpg",
                certBytes.toRequestBody("image/*".toMediaTypeOrNull())
            )

            // Text fields
            val textType = "text/plain".toMediaTypeOrNull()
            val farmNamePart = farmName.toRequestBody(textType)
            val farmTownPart = farmTown.toRequestBody(textType)
            // Send crops as comma-separated — server handles it
            val cropsPart = farmPrimaryCrops.joinToString(",").toRequestBody(textType)
            val sizePart = farmSizeHectares.toRequestBody(textType)
            val phonePart = phone.toRequestBody(textType)

            val response = RetrofitClient.api.submitVerification(
                "Bearer $token",
                idPart, certPart,
                farmNamePart, farmTownPart, cropsPart, sizePart, phonePart
            )

            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body.verification)
            } else {
                val msg = try {
                    response.errorBody()?.string()?.let {
                        val start = it.indexOf("\"error\":\"") + 9
                        val end = it.indexOf("\"", start)
                        if (start > 8 && end > start) it.substring(start, end) else null
                    }
                } catch (_: Exception) { null }
                ApiResult.Error(msg ?: "Could not submit (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Couldn't reach the server.")
        }
    }
}