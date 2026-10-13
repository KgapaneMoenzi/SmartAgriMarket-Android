package com.smartagrimarket.model.network

data class FavoriteDto(
    val id: String,
    val userEmail: String,
    val productId: String,
    val createdAt: String
)

data class FavoritesListResponse(
    val favorites: List<FavoriteDto>
)

data class FavoriteStatusResponse(
    val isFavorite: Boolean
)

