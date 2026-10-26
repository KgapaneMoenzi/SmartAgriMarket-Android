package com.smartagrimarket.model.network

import com.google.gson.annotations.SerializedName

object WalletRepository {
    private val api = RetrofitClient.api

    suspend fun getWallet(bearerToken: String): ApiResult<WalletResponse> {
        return try {
            val response = api.getWallet(bearerToken)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Could not load your wallet.")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun requestWithdrawal(
        bearerToken: String,
        amount: Double,
        payoutMethod: String,
        details: Map<String, String>
    ): ApiResult<WithdrawalDto> {
        return try {
            // Create the strict request object
            val request = WithdrawRequestDto(
                amount = amount,
                payoutMethod = payoutMethod,
                walletNumber = details["walletNumber"],
                bankName = details["bankName"],
                accountNumber = details["accountNumber"],
                accountName = details["accountName"]
            )

            val response = api.requestWithdrawal(bearerToken, request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.withdrawal)
            } else {
                val errorMsg = try {
                    response.errorBody()?.string()?.let { raw ->
                        Regex("\"error\"\\s*:\\s*\"([^\"]+)\"").find(raw)?.groupValues?.get(1)
                    } ?: "Withdrawal failed."
                } catch (_: Exception) { "Withdrawal failed." }
                ApiResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }
}

// This is the strict data class that will be sent to the backend
data class WithdrawRequestDto(
    val amount: Double,
    val payoutMethod: String,
    val walletNumber: String? = null,
    val bankName: String? = null,
    val accountNumber: String? = null,
    val accountName: String? = null
)