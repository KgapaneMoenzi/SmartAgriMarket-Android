package com.smartagrimarket.model.network

object HomeBannerRepository {
    private val api = RetrofitClient.api

    suspend fun fetchBanner(): ApiResult<HomeBannerDto> {
        val token = TokenStore.token
            ?: return ApiResult.Error("Please sign in.")
        return try {
            val response = api.getHomeBanner("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body.banner)
            } else {
                ApiResult.Error("Could not load banner (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Couldn't reach the server.")
        }
    }
}

