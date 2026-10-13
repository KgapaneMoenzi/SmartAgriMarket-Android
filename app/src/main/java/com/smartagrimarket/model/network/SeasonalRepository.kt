package com.smartagrimarket.model.network

object SeasonalRepository {
    private val api = RetrofitClient.api

    suspend fun fetch(month: Int, region: String = "Mpumalanga"): ApiResult<SeasonalResponse> {
        return try {
            val response = api.getSeasonal(month, region)
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error("Could not load calendar (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }
}