package com.smartagrimarket.model.network

import com.google.gson.annotations.SerializedName

data class WalletResponse(
    val balance: Double,
    val pending: Double = 0.0,
    val pendingOrderCount: Int = 0,
    val withdrawals: List<WithdrawalDto>
)

data class WithdrawalDto(
    val id: String,
    val amount: Double,
    val platformFee: Double,
    val netAmount: Double,
    val status: String, // "pending" | "approved" | "rejected"
    val rejectionReason: String?,
    val processedAt: String?,
    val createdAt: String
)

data class WithdrawRequest(val amount: Double)

// The backend wraps the created withdrawal in { "withdrawal": {...} }
data class WithdrawResponse(val withdrawal: WithdrawalDto)