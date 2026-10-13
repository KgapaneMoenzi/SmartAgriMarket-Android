package com.smartagrimarket.model.network

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

    suspend fun requestWithdrawal(bearerToken: String, amount: Double): ApiResult<WithdrawalDto> {
        return try {
            val response = api.requestWithdrawal(bearerToken, WithdrawRequest(amount))
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.withdrawal)
            } else {
                // Try to read the error message from the body
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

