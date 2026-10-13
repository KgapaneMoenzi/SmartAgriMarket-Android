package com.smartagrimarket.model.network

object FavoritesRepository {
    private val api = RetrofitClient.api

    /** Returns false for signed-out users without hitting the server. */
    suspend fun checkFavorite(productId: String): ApiResult<Boolean> {
        val token = TokenStore.token ?: return ApiResult.Success(false)
        return try {
            val response = api.checkFavorite("Bearer $token", productId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body.isFavorite)
            } else {
                ApiResult.Error("Could not check favorite (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Couldn't reach the server.")
        }
    }

    suspend fun addFavorite(productId: String): ApiResult<Boolean> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in to save items.")
        return try {
            val response = api.addFavorite("Bearer $token", productId)
            if (response.isSuccessful) ApiResult.Success(true)
            else ApiResult.Error("Could not save (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Couldn't reach the server.")
        }
    }

    suspend fun removeFavorite(productId: String): ApiResult<Boolean> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in.")
        return try {
            val response = api.removeFavorite("Bearer $token", productId)
            if (response.isSuccessful) ApiResult.Success(false)
            else ApiResult.Error("Could not remove (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Couldn't reach the server.")
        }
    }

    suspend fun fetchFavorites(): ApiResult<List<FavoriteDto>> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in.")
        return try {
            val response = api.getFavorites("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body.favorites)
            } else {
                ApiResult.Error("Could not load favorites (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Couldn't reach the server.")
        }
    }
}

