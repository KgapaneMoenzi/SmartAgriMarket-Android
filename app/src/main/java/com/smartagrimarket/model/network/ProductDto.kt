package com.smartagrimarket.model.network

data class CreateProductRequest(
    val name: String,
    val price: Double,
    val stock: Int?,
    val unit: String? = null,
    val packSize: String? = null,
    val bulkMinQty1: Int? = null,
    val bulkPrice1: Double? = null,
    val bulkMinQty2: Int? = null,
    val bulkPrice2: Double? = null,
    val imageUrl: String? = null
)

data class ProductDto(
    val id: String,
    val name: String,
    val price: Double,
    val stock: Int? = null,
    val unit: String? = "item",
    val packSize: String? = null,
    val imageUrl: String? = null,
    val location: String? = null,
    val farmerEmail: String? = null,
    val averageRating: Double? = null,
    val reviewCount: Int? = null,
    val bulkMinQty1: Int? = null,
    val bulkPrice1: Double? = null,
    val bulkMinQty2: Int? = null,
    val bulkPrice2: Double? = null
)

data class ProductListResponse(val products: List<ProductDto>)
data class CreateProductResponse(val product: ProductDto)