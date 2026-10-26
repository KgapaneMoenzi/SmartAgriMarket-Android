package com.smartagrimarket.model.network

object WeatherRepository {
    private val api = RetrofitClient.api

    suspend fun getAdvice(
        bearerToken: String,
        location: String? = null,
        lat: Double? = null,
        lon: Double? = null
    ): ApiResult<FarmerWeatherAdvice> {
        return try {
            // This calls the updated ApiService which accepts location, lat, and lon
            val response = api.getWeatherAdvice(bearerToken, location, lat, lon)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Could not load weather,please turn on your location (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server.")
        }
    }
}