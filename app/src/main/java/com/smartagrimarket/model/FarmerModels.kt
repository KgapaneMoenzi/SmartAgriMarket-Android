package com.smartagrimarket.model

import com.smartagrimarket.model.network.HubDto

/**
 * Models for the Farmer section.
 * Everything is backed by the real backend now.
 */

data class FarmerStats(
    val activeListings: Int,
    val ordersThisWeek: Int,
    val pendingPaymentTotal: Double,
    val ordersToPack: Int
)

data class FarmerStatsExtended(
    val activeListings: Int,
    val ordersThisWeek: Int,
    val ordersTotal: Int,
    val revenueThisWeek: Double,
    val revenueTotal: Double,
    val pendingPaymentTotal: Double,
    val ordersToPack: Int,
    val averageOrderValue: Double,
    val topSellingProduct: String?,
    val deliveryFeesThisWeek: Double = 0.0,
    val openRequestsCount: Int = 0,
    val topOpenRequestName: String? = null,
    val topOpenRequestCount: Int = 0,
    val dailyOrderCounts: List<DailyOrderCount>
)

data class DailyOrderCount(
    val dayLabel: String, // "Mon"
    val dateLabel: String, // "12 Aug"
    val count: Int
)

data class FarmerOrderLine(
    val name: String,
    val quantity: Int,
    val price: Double
)

data class FarmerOrderAddress(
    val line1: String? = null,
    val line2: String? = null,
    val city: String? = null,
    val province: String? = null,
    val postalCode: String? = null,
    val street: String? = null,
    val zip: String? = null
)

data class FarmerOrder(
    val id: String,
    val trackingId: String,
    val buyerName: String,
    val buyerEmail: String,
    val items: List<FarmerOrderLine>,
    val total: Double,
    val address: FarmerOrderAddress,
    val status: String,
    val trackingStatus: String,
    val trackingNote: String?,
    val estimatedDelivery: String?,
    val createdAt: String,

    /**
     * Emails of farmers who've dropped their portion at the hub.
     * Populated by the backend. Empty list is the safe default so old
     * responses without this field still parse.
     */
    val packedByFarmers: List<String> = emptyList(),

    /**
     * The platform-operated hub where this order is collected.
     * Null if no active hub is configured. Used by the detail screen
     * to render a hub card and a "Navigate to hub" button.
     */
    val hub: HubDto? = null
) {
    val displayId: String get() = trackingId

    val statusLabel: String
        get() = when (trackingStatus) {
            "order_placed" -> "Pack now"
            "processing" -> "Packed"
            "shipped" -> "Out for delivery"
            "delivered" -> "Delivered"
            else -> status.replaceFirstChar { it.uppercase() }
        }

    val area: String
        get() = address.city ?: address.province ?: address.street ?: address.line1 ?: "—"

    val itemCount: Int get() = items.sumOf { it.quantity }

    /**
     * True if the given farmer (by email) has already dropped their
     * items at the hub. Multi-farmer safe — two farmers in the same
     * order each see their own state independently.
     */
    fun myItemsPacked(userEmail: String?): Boolean {
        if (userEmail.isNullOrBlank()) return false
        return packedByFarmers.contains(userEmail)
    }
}