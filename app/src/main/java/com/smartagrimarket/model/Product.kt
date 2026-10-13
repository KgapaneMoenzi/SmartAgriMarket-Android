package com.smartagrimarket.model

data class Product(
    val id: String,
    val name: String,
    val farmName: String,
    val price: Double,
    val category: ProductCategory,
    val fromFarmerBadge: Boolean = false,
    val lowStock: Boolean = false,
    val stock: Int? = null,
    val imageUrl: String? = null,
    val farmerEmail: String? = null,
    // ── Rating aggregates (from real reviews) ──
    // Populated by the backend on every product DTO.
    // 0.0 / 0 means the product has no reviews yet.
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
)

enum class ProductCategory(val label: String) {
    ALL("All"),
    VEGETABLES("Vegetables"),
    FRUITS("Fruits"),
    DAIRY("Dairy")
}

data class HomeBanner(
    val title: String,
    val subtitle: String,
    val isPersonalized: Boolean
)

data class TrendingItem(
    val emoji: String,
    val name: String,
    val percentChange: Int
)

data class SeasonalTip(
    val emoji: String,
    val message: String
)