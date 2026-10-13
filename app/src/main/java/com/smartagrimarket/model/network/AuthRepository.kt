package com.smartagrimarket.model.network

import com.google.gson.Gson
import retrofit2.Response

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val needsVerification: Boolean = false) : ApiResult<Nothing>()
}

object AuthRepository {
    private val api = RetrofitClient.api
    private val gson = Gson()

    private fun <T> parseError(response: Response<T>): ApiResult.Error {
        return try {
            val errorBody = response.errorBody()?.string()
            val parsed = gson.fromJson(errorBody, ApiErrorResponse::class.java)
            ApiResult.Error(parsed?.error ?: "Something went wrong", parsed?.needsVerification ?: false)
        } catch (e: Exception) {
            ApiResult.Error("Something went wrong (${response.code()})")
        }
    }

    suspend fun signUp(fullName: String, email: String, phone: String?, password: String, role: String): ApiResult<SignUpResponse> {
        return try {
            val response = api.signUp(SignUpRequest(fullName, email, phone, password, role))
            if (response.isSuccessful && response.body() != null) ApiResult.Success(response.body()!!) else parseError(response)
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun verifyEmail(email: String, code: String): ApiResult<AuthResponse> {
        return try {
            val response = api.verifyEmail(VerifyEmailRequest(email, code))
            if (response.isSuccessful && response.body() != null) ApiResult.Success(response.body()!!) else parseError(response)
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun resendCode(email: String): ApiResult<ResendCodeResponse> {
        return try {
            val response = api.resendCode(ResendCodeRequest(email))
            if (response.isSuccessful && response.body() != null) ApiResult.Success(response.body()!!) else parseError(response)
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun signIn(email: String, password: String): ApiResult<AuthResponse> {
        return try {
            val response = api.signIn(SignInRequest(email, password))
            if (response.isSuccessful && response.body() != null) ApiResult.Success(response.body()!!) else parseError(response)
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    // --- ADDED: Forgot & Reset Password Functions ---
    suspend fun forgotPassword(email: String): ApiResult<Unit> {
        return try {
            val response = api.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) ApiResult.Success(Unit) else parseError(response)
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun resetPassword(email: String, code: String, newPassword: String): ApiResult<Unit> {
        return try {
            val response = api.resetPassword(ResetPasswordRequest(email, code, newPassword))
            if (response.isSuccessful) ApiResult.Success(Unit) else parseError(response)
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }
    // ------------------------------------------------

    // ─────────────────────────────────────────────────────────────
    // Deactivate account (replaces delete)
    //
    // Self-service account deactivation. The backend flags the
    // account inactive, hides any farmer products, and keeps the
    // data safe. Signing in again reactivates automatically.
    // ─────────────────────────────────────────────────────────────
    suspend fun deactivateAccount(bearerToken: String): ApiResult<Unit> {
        return try {
            val response = api.deactivateAccount(bearerToken)
            if (response.isSuccessful) ApiResult.Success(Unit) else parseError(response)
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    /**
     * Deprecated. The app no longer hard-deletes accounts — it deactivates
     * them. Kept here so any legacy call sites still compile, and so the
     * backend's `DELETE /me` route (retained for admin use) can still be
     * reached if needed.
     */
    @Deprecated("Use deactivateAccount instead — hard delete is admin-only.")
    suspend fun deleteAccount(bearerToken: String): ApiResult<Unit> {
        return try {
            val response = api.deleteAccount(bearerToken)
            if (response.isSuccessful) ApiResult.Success(Unit) else parseError(response)
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun updateProfile(bearerToken: String, fullName: String, phone: String?): ApiResult<UserDto> {
        return try {
            val response = api.updateProfile(bearerToken, UpdateProfileRequest(fullName, phone))
            if (response.isSuccessful && response.body() != null) ApiResult.Success(response.body()!!) else parseError(response)
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun changePassword(bearerToken: String, currentPassword: String, newPassword: String): ApiResult<Unit> {
        return try {
            val response = api.changePassword(bearerToken, ChangePasswordRequest(currentPassword, newPassword))
            if (response.isSuccessful) ApiResult.Success(Unit) else parseError(response)
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun getAddress(bearerToken: String): ApiResult<AddressDto> {
        return try {
            val response = api.getAddress(bearerToken)
            if (response.isSuccessful && response.body() != null) ApiResult.Success(response.body()!!) else parseError(response)
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun updateAddress(bearerToken: String, request: AddressRequest): ApiResult<AddressDto> {
        return try {
            val response = api.updateAddress(bearerToken, request)
            if (response.isSuccessful && response.body() != null) ApiResult.Success(response.body()!!) else parseError(response)
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun getFarmProfile(bearerToken: String): ApiResult<FarmProfileDto> {
        return try {
            val response = api.getFarmProfile(bearerToken)
            if (response.isSuccessful && response.body() != null) ApiResult.Success(response.body()!!) else parseError(response)
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun updateFarmProfile(bearerToken: String, request: FarmProfileDto): ApiResult<FarmProfileDto> {
        return try {
            val response = api.updateFarmProfile(bearerToken, request)
            if (response.isSuccessful && response.body() != null) ApiResult.Success(response.body()!!) else parseError(response)
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun getOrders(bearerToken: String): ApiResult<List<OrderDto>> {
        return try {
            val response = api.getOrders(bearerToken)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.orders)
            } else {
                parseError(response)
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun getOrderDetail(bearerToken: String, orderId: String): ApiResult<OrderDto> {
        return try {
            val response = api.getOrderDetail(bearerToken, orderId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.order)
            } else {
                parseError(response)
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun getNotificationSettings(bearerToken: String): ApiResult<NotificationSettingsDto> {
        return try {
            val response = api.getNotificationSettings(bearerToken)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                parseError(response)
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun updateNotificationSettings(bearerToken: String, settings: NotificationSettingsDto): ApiResult<NotificationSettingsDto> {
        return try {
            val response = api.updateNotificationSettings(bearerToken, settings)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                parseError(response)
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }
}