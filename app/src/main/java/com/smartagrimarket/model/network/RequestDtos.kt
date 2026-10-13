package com.smartagrimarket.model.network

data class ProductRequestDto(
    val id: String,
    val buyerEmail: String,
    val buyerName: String,
    val farmerEmail: String?,
    val productName: String,
    val note: String?,
    val status: String,
    val replyBody: String?,
    val repliedAt: String?,
    val createdAt: String
)

data class ProductRequestListResponse(
    val requests: List<ProductRequestDto>
)

data class ProductRequestResponse(
    val request: ProductRequestDto
)

data class CreateProductRequestBody(
    val productName: String,
    val note: String? = null,
    val farmerEmail: String? = null
)

data class ReplyRequestBody(val replyBody: String)

data class FarmerListItemDto(
    val email: String,
    val name: String
)

data class FarmersListResponse(
    val farmers: List<FarmerListItemDto>
)