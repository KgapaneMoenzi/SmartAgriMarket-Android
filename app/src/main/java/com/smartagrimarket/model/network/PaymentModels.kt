package com.smartagrimarket.model.network

data class PaymentItemPayload(
    val name: String,
    val quantity: Int,
    val price: Double,
    val productId: String? = null,      // used by backend stock check
    val farmerEmail: String? = null     // used to notify the right farmer
)

data class PaymentCreateRequest(
    val amount: String,
    val itemName: String,
    val buyerEmail: String,
    val buyerName: String,
    val items: List<PaymentItemPayload>? = null,
    val deliveryMethod: String? = null,
    val address: AddressDto? = null,
    val pickupPointId: String? = null,
    val deliveryLat: Double? = null,
    val deliveryLng: Double? = null,
    val deliveryZone: String? = null    // ← optional; server derives from coords when null
)

data class PaymentCreateResponse(
    val paymentUrl: String,
    val fields: Map<String, String>,
    val orderId: String,
    val trackingId: String
)