package com.smartagrimarket.model.network

object WeatherRepository {
    private val api = RetrofitClient.api

    suspend fun getAdvice(bearerToken: String, location: String): ApiResult<FarmerWeatherAdvice> {
        return try {
            val response = api.getWeatherAdvice(bearerToken, location)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Could not load weather.")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the weather service.")
        }
    }
}