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
    val unit: String = "item",
    val packSize: String? = null,
    val imageUrl: String? = null,
    val farmerEmail: String? = null,
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,

    // BULK FIELDS
    val bulkMinQty1: Int? = null,
    val bulkPrice1: Double? = null,
    val bulkMinQty2: Int? = null,
    val bulkPrice2: Double? = null,
)

enum class ProductCategory(val label: String) {
    ALL("All"),
    VEGETABLES("Vegetables"),
    FRUITS("Fruits"),
    DAIRY("Dairy"),
    MEAT("Meat & Livestock"),
    POULTRY("Poultry & Eggs")
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