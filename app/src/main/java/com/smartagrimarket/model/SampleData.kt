package com.smartagrimarket.model

object SampleData {

    val buyerFirstName = "Thabo"

    val banner = HomeBanner(
        title = "Because you buy kale often",
        subtitle = "Riverdale Farm has fresh kale — 15% off today",
        isPersonalized = true
    )

    val pickedForYou = listOf(
        Product(
            id = "p1",
            name = "Kale",
            farmName = "Riverdale Farm",
            price = 28.00,
            category = ProductCategory.VEGETABLES,
            fromFarmerBadge = true
        ),
        Product(
            id = "p2",
            name = "Strawberries",
            farmName = "Berry Hill Farm",
            price = 45.00,
            category = ProductCategory.FRUITS,
            lowStock = true
        ),
        Product(
            id = "p3",
            name = "Carrots",
            farmName = "Sunfield Farms",
            price = 22.00,
            category = ProductCategory.VEGETABLES
        )
    )

    val marketplace = listOf(
        Product(
            id = "m1",
            name = "Organic kale",
            farmName = "Riverdale Farm",
            price = 28.00,
            category = ProductCategory.VEGETABLES,
            fromFarmerBadge = true
        ),
        Product(
            id = "m2",
            name = "Vine tomatoes",
            farmName = "Green Valley Co-op",
            price = 35.00,
            category = ProductCategory.VEGETABLES,
            fromFarmerBadge = true
        ),
        Product(
            id = "m3",
            name = "Baby carrots",
            farmName = "Sunfield Farms",
            price = 22.00,
            category = ProductCategory.VEGETABLES
        ),
        Product(
            id = "m4",
            name = "Strawberries",
            farmName = "Berry Hill Farm",
            price = 45.00,
            category = ProductCategory.FRUITS,
            lowStock = true
        ),
        Product(
            id = "m5",
            name = "Naartjies",
            farmName = "Sunfield Farms",
            price = 30.00,
            category = ProductCategory.FRUITS
        ),
        Product(
            id = "m6",
            name = "Broccoli",
            farmName = "Green Valley Co-op",
            price = 30.00,
            category = ProductCategory.VEGETABLES
        )
    )

    val trending = listOf(
        TrendingItem("🍊", "Naartjies", 34),
        TrendingItem("🥦", "Broccoli", 21)
    )

    val seasonalTip = SeasonalTip(
        emoji = "🌱",
        message = "Good week to plant leafy greens · Light rain expected tomorrow"
    )
}