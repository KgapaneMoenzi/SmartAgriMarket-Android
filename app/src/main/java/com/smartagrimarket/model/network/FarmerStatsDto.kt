package com.smartagrimarket.model.network

data class FarmerStatsDto(
    val activeListings: Int,
    val ordersThisWeek: Int,
    val pendingPaymentTotal: Double,
    val ordersToPack: Int
)

data class FarmerStatsExtendedDto(
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
    val dailyOrderCounts: List<DailyOrderCountDto>
)

data class DailyOrderCountDto(
    val dayLabel: String,
    val dateLabel: String,
    val count: Int
)

data class FarmerOrderLineDto(
    val name: String,
    val quantity: Int,
    val price: Double
)

data class FarmerOrderAddressDto(
    val line1: String? = null,
    val line2: String? = null,
    val city: String? = null,
    val province: String? = null,
    val postalCode: String? = null,
    val street: String? = null,
    val zip: String? = null
)

data class FarmerOrderDto(
    val id: String,
    val trackingId: String,
    val buyerName: String,
    val buyerEmail: String,
    val items: List<FarmerOrderLineDto>,
    val total: Double,
    val address: FarmerOrderAddressDto,
    val status: String,
    val trackingStatus: String,
    val trackingNote: String?,
    val estimatedDelivery: String?,
    val createdAt: String
)

data class FarmerOrdersListResponse(val orders: List<FarmerOrderDto>)
data class FarmerOrderDetailResponse(val order: FarmerOrderDto)

data class FarmerOrderStatusUpdate(
    val trackingStatus: String,
    val trackingNote: String? = null,
    val estimatedDelivery: String? = null
)