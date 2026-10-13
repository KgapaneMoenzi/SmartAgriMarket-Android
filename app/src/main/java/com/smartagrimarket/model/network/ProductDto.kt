package com.smartagrimarket.model.network

// ═══════════════════════════════════════════════════════════
// PRODUCTS — single source of truth
// ═══════════════════════════════════════════════════════════

data class ProductDto(
    val id: String,
    val name: String,
    val price: Double,
    val description: String? = null,
    val location: String? = null,
    val imageUrl: String? = null,
    val stock: Int? = null,
    val isApproved: Boolean = true,
    val createdAt: String? = null,
    val farmerEmail: String? = null,
    // ── Rating aggregates ──
    // Populated by the backend on every product response.
    // Nullable + defaulted so old responses still parse.
    val averageRating: Double? = null,
    val reviewCount: Int? = null,
)

data class ProductListResponse(
    val products: List<ProductDto>
)

data class CreateProductRequest(
    val name: String,
    val price: Double,
    val stock: Int? = null,
    val region: String? = null,
    val description: String? = null,
    val imageUrl: String? = null
)

data class CreateProductResponse(
    val product: ProductDto
)