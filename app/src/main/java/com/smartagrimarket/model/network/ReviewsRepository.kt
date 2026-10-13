package com.smartagrimarket.model.network

object ReviewsRepository {
    private val api = RetrofitClient.api

    suspend fun fetchReviews(productId: String): ApiResult<ReviewsListResponse> {
        return try {
            val response = api.getProductReviews(productId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error("Could not load reviews (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun checkEligibility(productId: String): ApiResult<ReviewEligibilityResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getReviewEligibility("Bearer $token", productId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error("Could not check eligibility (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun submitReview(productId: String, rating: Int, comment: String): ApiResult<ReviewDto> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.createReview(
                "Bearer $token",
                productId,
                CreateReviewRequest(rating, comment)
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body.review)
            } else {
                val msg = try {
                    response.errorBody()?.string()?.let {
                        val start = it.indexOf("\"error\":\"") + 9
                        val end = it.indexOf("\"", start)
                        if (start > 8 && end > start) it.substring(start, end) else null
                    }
                } catch (_: Exception) { null }
                ApiResult.Error(msg ?: "Could not save review (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }
}