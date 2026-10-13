package com.smartagrimarket.model

/**
 * Lightweight models for the Product Detail screen — reviews and Q&A.
 * Kept generic/reusable across all products for this frontend-only pass;
 * a real backend would key these by productId.
 */
data class ProductReview(
    val author: String,
    val stars: Int,
    val daysAgo: String,
    val comment: String
)

data class ProductQuestion(
    val question: String,
    val askedBy: String,
    val answer: String? = null
)

object ProductDetailSampleData {
    val rating: Double = 4.6
    val reviewCount: Int = 128

    val reviews: List<ProductReview> = listOf(
        ProductReview(
            author = "Thabo N.",
            stars = 5,
            daysAgo = "2 days ago",
            comment = "Super fresh, arrived same day. Will order again from this farm."
        ),
        ProductReview(
            author = "Rina D.",
            stars = 4,
            daysAgo = "1 week ago",
            comment = "Good quality, a couple of leaves were a bit wilted but overall happy."
        )
    )

    val questions: List<ProductQuestion> = listOf(
        ProductQuestion(
            question = "Is this pesticide-free?",
            askedBy = "Rina D.",
            answer = "Yes, fully organic and pesticide-free — certified by Mpumalanga Organic Growers."
        ),
        ProductQuestion(
            question = "How long does it stay fresh?",
            askedBy = "Thabo N.",
            answer = "Best within 5–7 days if refrigerated. Harvested the morning of delivery."
        )
    )
}