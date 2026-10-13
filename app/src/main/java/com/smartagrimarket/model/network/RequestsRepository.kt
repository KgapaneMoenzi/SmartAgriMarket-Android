package com.smartagrimarket.model.network

object RequestsRepository {
    private val api = RetrofitClient.api

    suspend fun create(
        productName: String,
        note: String?,
        farmerEmail: String?
    ): ApiResult<ProductRequestDto> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.createRequest(
                "Bearer $token",
                CreateProductRequestBody(productName, note, farmerEmail)
            )
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body.request)
            else ApiResult.Error("Could not submit your request (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun fetchMine(): ApiResult<List<ProductRequestDto>> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getMyRequests("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body.requests)
            else ApiResult.Error("Could not load your requests (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun fetchFarmers(): ApiResult<List<FarmerListItemDto>> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getFarmersForRequest("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body.farmers)
            else ApiResult.Error("Could not load farmers (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun fetchFarmerRequests(): ApiResult<List<ProductRequestDto>> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getFarmerRequests("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body.requests)
            else ApiResult.Error("Could not load requests (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun reply(id: String, replyBody: String): ApiResult<ProductRequestDto> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.replyToRequest("Bearer $token", id, ReplyRequestBody(replyBody))
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body.request)
            else ApiResult.Error("Could not save reply (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun markFulfilled(id: String): ApiResult<ProductRequestDto> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.markRequestFulfilled("Bearer $token", id)
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body.request)
            else ApiResult.Error("Could not update (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }
}