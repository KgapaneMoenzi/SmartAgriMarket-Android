package com.smartagrimarket.model.network

// ---- Loyalty ----

data class RedeemOptionDto(
    val kind: String,
    val label: String,
    val amountOff: Double,
    val minSpend: Double,
    val pointsCost: Int
)

data class PointsHistoryDto(
    val id: String,
    val delta: Int,
    val reason: String,
    val balanceAfter: Int,
    val createdAt: String
)

data class CouponDto(
    val id: String,
    val code: String,
    val kind: String,
    val amountOff: Double,
    val minSpend: Double
)

data class LoyaltyMeResponse(
    val balance: Int,
    val randsEquivalent: Double,
    val tierLabel: String,
    val nextTierLabel: String?,
    val pointsToNextTier: Int,
    val tierProgressFraction: Float,
    val redeemOptions: List<RedeemOptionDto>,
    val history: List<PointsHistoryDto>,
    val coupons: List<CouponDto>
)

data class RedeemRequest(val kind: String)

data class RedeemResponse(
    val coupon: CouponDto,
    val newBalance: Int
)

// ---- Referrals ----

data class ReferralItemDto(
    val id: String,
    val referredEmail: String,
    val isCredited: Boolean,
    val rewardAmount: Double,
    val createdAt: String
)

data class ReferralsMeResponse(
    val code: String,
    val referrals: List<ReferralItemDto>
)

// ---- Data export ----

data class DataExportResponse(
    val message: String,
    val emailed: Boolean
)