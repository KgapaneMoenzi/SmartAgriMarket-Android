package com.smartagrimarket.model.network

object DeliverySlotRepository {
    private val api = RetrofitClient.api

    suspend fun fetchDeliverySlots(orderId: String): ApiResult<DeliverySlotsResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getDeliverySlots("Bearer $token", orderId)
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error("Could not load slots (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun selectDeliverySlot(
        orderId: String,
        start: String,
        end: String
    ): ApiResult<SelectSlotResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.selectDeliverySlot(
                "Bearer $token",
                orderId,
                SelectSlotRequest(start = start, end = end)
            )
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error("Could not save slot (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }
}