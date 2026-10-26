package com.smartagrimarket.model.network

import android.content.Context
import android.net.Uri
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import retrofit2.Response

object DriverRepository {
    private val api = RetrofitClient.api

    // ─────────────────────────────────────────────────────────────
    // Jobs
    // ─────────────────────────────────────────────────────────────

    suspend fun fetchJobs(): ApiResult<DriverJobsResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getDriverJobs("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error("Could not load jobs (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    @Deprecated("Jobs are assigned by dispatch. Self-accept is disabled.")
    suspend fun acceptJob(jobId: String): ApiResult<DriverJobDto> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.acceptDriverJob("Bearer $token", jobId)
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body.job)
            else ApiResult.Error("Could not accept job (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun updateStatus(jobId: String, status: String): ApiResult<DriverJobDto> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.updateDriverJobStatus(
                "Bearer $token",
                jobId,
                DriverStatusUpdate(status = status)
            )
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body.job)
            else ApiResult.Error("Could not update status (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun sendLocation(jobId: String, lat: Double, lng: Double): ApiResult<Unit> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.updateDriverLocation(
                "Bearer $token",
                jobId,
                DriverLocationUpdate(latitude = lat, longitude = lng)
            )
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error("Could not send location (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server.")
        }
    }

    suspend fun skipJob(jobId: String): ApiResult<Unit> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.skipDriverJob("Bearer $token", jobId)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error("Could not record skip (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server.")
        }
    }

    suspend fun setAvailability(isAvailable: Boolean): ApiResult<DriverAvailabilityResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.setDriverAvailability(
                "Bearer $token",
                DriverAvailabilityRequest(isAvailable = isAvailable)
            )
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else {
                val msg = extractErrorBody(response)
                ApiResult.Error(msg ?: "Could not update availability (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun fetchStats(): ApiResult<DriverStatsDto> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getDriverStats("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error("Could not load stats (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CANOPY FRIDGE (COLD-CHAIN)
    // ─────────────────────────────────────────────────────────────

    suspend fun updateVehicleDetails(hasFridge: Boolean): ApiResult<Boolean> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.updateDriverVehicle(
                "Bearer $token",
                VehicleUpdateRequest(hasFridge = hasFridge)
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body.hasFridge)
            } else {
                val msg = extractErrorBody(response)
                ApiResult.Error(msg ?: "Could not update canopy fridge (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Zones
    // ─────────────────────────────────────────────────────────────

    suspend fun fetchZones(): ApiResult<ZonesResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getDriverZones("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error("Could not load zones (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun saveZones(zones: List<String>): ApiResult<ZonesResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.saveDriverZones(
                "Bearer $token",
                ZonesSaveRequest(zones = zones)
            )
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error("Could not save zones (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Legacy batch endpoint
    // ─────────────────────────────────────────────────────────────

    suspend fun fetchBatch(): ApiResult<BatchDto> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getDriverBatch("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error("Could not load batch (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Batch lifecycle — start, collect, deliver
    // ─────────────────────────────────────────────────────────────

    suspend fun startBatch(): ApiResult<BatchActionResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.startDriverBatch("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else {
                val msg = extractErrorBody(response)
                ApiResult.Error(msg ?: "Could not start batch (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun collectBatch(): ApiResult<BatchActionResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.collectDriverBatch("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else {
                val msg = extractErrorBody(response)
                ApiResult.Error(msg ?: "Could not collect batch (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun deliverStop(deliveryId: String): ApiResult<BatchActionResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.deliverDriverStop("Bearer $token", deliveryId)
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else {
                val msg = extractErrorBody(response)
                ApiResult.Error(msg ?: "Could not mark delivered (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Wallet
    // ─────────────────────────────────────────────────────────────

    suspend fun getWallet(): ApiResult<DriverWalletDto> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getDriverWallet("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error("Could not load wallet (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    // ── UPDATED FOR E-WALLET / BANK ──
    suspend fun requestWithdrawal(
        amount: Double,
        payoutMethod: String,
        details: Map<String, String>
    ): ApiResult<DriverWithdrawalDto> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val request = WithdrawRequestDto(
                amount = amount,
                payoutMethod = payoutMethod,
                walletNumber = details["walletNumber"],
                bankName = details["bankName"],
                accountNumber = details["accountNumber"],
                accountName = details["accountName"]
            )

            val response = api.requestDriverWithdrawal("Bearer $token", request)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body.withdrawal)
            } else {
                val msg = extractErrorBody(response)
                ApiResult.Error(msg ?: "Could not process withdrawal (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Verification
    // ─────────────────────────────────────────────────────────────

    suspend fun fetchVerificationStatus(): ApiResult<DriverVerificationMeResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getDriverVerification("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error("Could not load verification (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun fetchVerification(): ApiResult<DriverVerificationMeResponse> =
        fetchVerificationStatus()

    suspend fun submitVerification(
        context: Context,
        licence: Uri,
        permit: Uri,
        registration: Uri,
        carImage: Uri,
        carImage2: Uri?,
        carImage3: Uri?,
        vehicleMake: String,
        vehicleModel: String,
        vehiclePlate: String,
        vehicleColor: String,
        addressLine1: String,
        addressLine2: String,
        addressCity: String,
        addressProvince: String,
        addressPostalCode: String
    ): ApiResult<DriverVerificationSubmitResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")

        return try {
            val licencePart = uriToMultipart(context, licence, "licence")
                ?: return ApiResult.Error("Could not read licence image.")
            val permitPart = uriToMultipart(context, permit, "permit")
                ?: return ApiResult.Error("Could not read permit image.")
            val registrationPart = uriToMultipart(context, registration, "registration")
                ?: return ApiResult.Error("Could not read registration image.")
            val carImagePart = uriToMultipart(context, carImage, "carImage")
                ?: return ApiResult.Error("Could not read vehicle image.")

            val carImage2Part = carImage2?.let { uriToMultipart(context, it, "carImage2") }
            val carImage3Part = carImage3?.let { uriToMultipart(context, it, "carImage3") }

            val textType = "text/plain".toMediaTypeOrNull()

            val response = api.submitDriverVerification(
                "Bearer $token",
                licencePart,
                permitPart,
                registrationPart,
                carImagePart,
                carImage2Part,
                carImage3Part,
                vehicleMake.toRequestBody(textType),
                vehicleModel.toRequestBody(textType),
                vehiclePlate.toRequestBody(textType),
                vehicleColor.toRequestBody(textType),
                addressLine1.toRequestBody(textType),
                addressLine2.toRequestBody(textType),
                addressCity.toRequestBody(textType),
                addressProvince.toRequestBody(textType),
                addressPostalCode.toRequestBody(textType)
            )

            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error("Could not submit verification (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't submit verification: ${e.message ?: "unknown error"}")
        }
    }

    private fun uriToMultipart(
        context: Context,
        uri: Uri,
        fieldName: String
    ): MultipartBody.Part? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(fieldName, tempFile.name, requestFile)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractErrorBody(response: Response<*>): String? {
        return try {
            val raw = response.errorBody()?.string() ?: return null
            val json = JsonParser.parseString(raw).asJsonObject
            json.get("error")?.asString?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}