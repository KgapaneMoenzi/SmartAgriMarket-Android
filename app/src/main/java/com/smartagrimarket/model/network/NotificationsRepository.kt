package com.smartagrimarket.model.network

object NotificationsRepository {
    private val api = RetrofitClient.api

    suspend fun fetchNotifications(): ApiResult<NotificationsListResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getNotifications("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error("Could not load notifications (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun fetchUnreadCount(): ApiResult<Int> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getUnreadNotificationCount("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body.unreadCount)
            } else {
                ApiResult.Error("Could not load count (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun markRead(id: String): ApiResult<Unit> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.markNotificationRead("Bearer $token", id)
            if (response.isSuccessful) ApiResult.Success(Unit) else ApiResult.Error("Could not mark read (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun markAllRead(): ApiResult<Unit> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.markAllNotificationsRead("Bearer $token")
            if (response.isSuccessful) ApiResult.Success(Unit) else ApiResult.Error("Could not mark all read (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }
}