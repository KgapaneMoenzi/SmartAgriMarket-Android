package com.smartagrimarket.model.network

// ---- Profile ----
data class UpdateProfileRequest(
    val fullName: String,
    val phone: String?
)

data class UpdateProfilePhotoResponse(
    val photoUrl: String
)

// ---- Password ----
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

// ---- Buyer delivery address (single address, not a list) ----
data class AddressDto(
    val line1: String?,
    val line2: String?,
    val city: String?,
    val province: String?,
    val postalCode: String?,
    // Captured with the in-app LocationPicker. Used by checkout
    // to compute distance-based delivery fees and to plot the
    // destination on live tracking maps.
    val latitude: Double? = null,
    val longitude: Double? = null,
)

data class AddressRequest(
    val line1: String,
    val line2: String?,
    val city: String,
    val province: String,
    val postalCode: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

// ---- Farm details (farmer) ----
data class FarmProfileDto(
    val farmName: String?,
    val farmSizeHectares: Double?,
    val town: String?,
    val primaryCrops: List<String>,
    val farmLatitude: Double? = null,
    val farmLongitude: Double? = null,
)

// ---- Order history (buyer) ----
data class OrderDto(
    val id: String,
    val trackingId: String,
    val buyerEmail: String,
    val buyerName: String,
    val items: List<OrderLineItem>,
    val total: Double,
    val address: OrderAddressJson,
    val status: String,
    val trackingStatus: String,
    val trackingNote: String?,
    val estimatedDelivery: String?,
    val createdAt: String,
    val deliveryLatitude: Double? = null,
    val deliveryLongitude: Double? = null,
    val deliveryFee: Double? = null,
    val deliveryDistanceKm: Double? = null,
)

data class OrderLineItem(
    val name: String,
    val quantity: Int,
    val price: Double
)

data class OrderAddressJson(
    val line1: String?,
    val line2: String?,
    val city: String?,
    val province: String?,
    val postalCode: String?
)

data class OrdersListResponse(val orders: List<OrderDto>)
data class OrderDetailResponse(val order: OrderDto)

data class NotificationSettingsDto(
    val orderUpdates: Boolean,
    val priceAlerts: Boolean,
    val promotions: Boolean,
    val newMessages: Boolean
)