package com.smartagrimarket.model.network

object HelpRepository {
    private val api = RetrofitClient.api

    suspend fun fetchArticles(audience: String): ApiResult<List<HelpArticleDto>> {
        return try {
            val response = api.getHelpArticles(audience)
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body.articles)
            else ApiResult.Error("Could not load articles (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun fetchStatus(): ApiResult<SupportStatusResponse> {
        return try {
            val response = api.getSupportStatus()
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error("Could not load status (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun fetchChat(): ApiResult<List<SupportMessageDto>> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getSupportChat("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) ApiResult.Success(body.messages)
            else ApiResult.Error("Could not load messages (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun sendMessage(body: String): ApiResult<List<SupportMessageDto>> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.sendSupportMessage("Bearer $token", SendSupportMessageRequest(body))
            val responseBody = response.body()
            if (response.isSuccessful && responseBody != null) ApiResult.Success(responseBody.messages)
            else ApiResult.Error("Could not send message (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }
}