package com.smartagrimarket.model.network

data class ReviewDto(
    val id: String,
    val reviewerName: String,
    val rating: Int,
    val comment: String,
    val createdAt: String
)

data class RatingBreakdownDto(
    val stars: Int,
    val count: Int
)

data class ReviewsListResponse(
    val reviews: List<ReviewDto>,
    val averageRating: Double,
    val totalReviews: Int,
    val breakdown: List<RatingBreakdownDto>
)

data class ReviewEligibilityResponse(
    val eligible: Boolean,
    val reason: String? = null
)

data class CreateReviewRequest(
    val rating: Int,
    val comment: String
)

data class CreateReviewResponse(
    val review: ReviewDto
)