package com.smartagrimarket.model.network

data class HomeBannerDto(
    val title: String,
    val subtitle: String,
    val isPersonalized: Boolean
)

data class HomeBannerResponse(
    val banner: HomeBannerDto
)