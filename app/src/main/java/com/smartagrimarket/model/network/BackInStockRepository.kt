package com.smartagrimarket.model.network

object BackInStockRepository {
    private val api = RetrofitClient.api

    suspend fun checkBackInStock(productId: String): ApiResult<BackInStockResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.checkBackInStock("Bearer $token", productId)
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error("Could not check (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun subscribeBackInStock(productId: String): ApiResult<BackInStockResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.subscribeBackInStock(
                "Bearer $token",
                BackInStockRequest(productId)
            )
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error("Could not subscribe (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }
}