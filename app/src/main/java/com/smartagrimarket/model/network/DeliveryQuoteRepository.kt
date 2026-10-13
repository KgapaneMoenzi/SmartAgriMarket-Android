package com.smartagrimarket.model.network

object DeliveryQuoteRepository {
    private val api = RetrofitClient.api

    suspend fun quote(
        items: List<DeliveryQuoteItem>,
        latitude: Double,
        longitude: Double,
    ): ApiResult<DeliveryQuoteResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getDeliveryQuote(
                "Bearer $token",
                DeliveryQuoteRequest(items = items, latitude = latitude, longitude = longitude),
            )
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error("Could not compute delivery fee (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server.")
        }
    }
}