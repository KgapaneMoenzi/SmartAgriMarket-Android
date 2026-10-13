package com.smartagrimarket.model.network

object RecommendationsRepository {
    private val api = RetrofitClient.api

    suspend fun fetchForMe(): ApiResult<RecommendationsResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getRecommendations("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error("Could not load recommendations (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server.")
        }
    }

    suspend fun fetchSimilar(productId: String): ApiResult<RecommendationsResponse> {
        return try {
            val response = api.getSimilarProducts(productId)
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error("Could not load similar products (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server.")
        }
    }

    suspend fun fetchTrending(): ApiResult<RecommendationsResponse> {
        return try {
            val response = api.getTrendingProducts()
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error("Could not load trending (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server.")
        }
    }
}