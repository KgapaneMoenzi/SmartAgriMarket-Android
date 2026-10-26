package com.smartagrimarket.model.network

// ─────────────────────────────────────────────────────────────
// A single pickup stop within a driver job.
//
// In hub mode (pickupType == "HUB"), the stop is the platform's
// collection hub — the driver goes there once, not to each farm.
// In fallback mode (pickupType == "FARM"), it's a farm visit.
// ─────────────────────────────────────────────────────────────
data class PickupStopDto(
    val pickupType: String = "FARM",       // "HUB" | "FARM"

    // Farm fields (FARM mode)
    val farmerEmail: String = "",
    val farmerName: String? = null,
    val farmName: String? = null,
    val farmTown: String? = null,
    val farmLatitude: Double? = null,
    val farmLongitude: Double? = null,

    // Hub fields (HUB mode)
    val hubId: String? = null,
    val hubCode: String? = null,
    val hubName: String? = null,
    val hubAddress: String? = null,

    // Farmers contributing to this stop (1 for farm, N for hub)
    val farmerEmails: List<String> = emptyList(),

    val items: List<Map<String, Any?>> = emptyList(),
    val hasPacked: Boolean = false,
)

data class DriverJobDto(
    val id: String,
    val orderId: String,
    val trackingId: String,
    val status: String,
    val driverEmail: String? = null,

    val buyerName: String = "",
    val buyerEmail: String = "",
    val deliveryAddress: Map<String, Any?>? = null,

    // Multi-farm / hub pickups
    val pickups: List<PickupStopDto> = emptyList(),
    val pickupMode: String = "FARM",        // "HUB" | "FARM"
    val packedByFarmers: List<String> = emptyList(),

    // Legacy single-pickup fields (still sent for backward compat)
    val pickupFarmerName: String? = null,
    val pickupFarmName: String? = null,
    val pickupFarmTown: String? = null,
    val pickupLatitude: Double? = null,
    val pickupLongitude: Double? = null,

    // Delivery destination
    val deliveryLatitude: Double? = null,
    val deliveryLongitude: Double? = null,
    val deliveryZone: String? = null,

    // Live driver location (last reported)
    val driverLatitude: Double? = null,
    val driverLongitude: Double? = null,
    val driverLocationAt: String? = null,

    val items: List<Map<String, Any?>> = emptyList(),
    val total: Double = 0.0,

    val payout: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val deliveryDistanceKm: Double? = null,
    val valuePerKm: Double? = null,

    val score: Double? = null,

    val assignedAt: String? = null,
    val acceptedAt: String? = null,
    val pickedUpAt: String? = null,
    val deliveredAt: String? = null,
    val createdAt: String = "",
)

data class HubDto(
    val id: String,
    val code: String? = null,
    val name: String,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

// ─────────────────────────────────────────────────────────────
// Batch delivery — one stop in the driver's delivery queue.
//
// A "stop" is one delivery (one order → one buyer). The queue is
// sorted by distance from hub ascending; the driver does the
// closest one first.
// ─────────────────────────────────────────────────────────────
data class BatchStopDto(
    val deliveryId: String,
    val orderId: String = "",
    val trackingId: String = "",
    val status: String = "accepted",

    val buyerName: String = "",
    val buyerEmail: String = "",
    val deliveryAddress: Map<String, Any?>? = null,
    val deliveryLatitude: Double? = null,
    val deliveryLongitude: Double? = null,
    val deliveryZone: String? = null,
    val distanceFromHubKm: Double? = null,

    val itemCount: Int = 0,
    val items: List<Map<String, Any?>> = emptyList(),
    val payout: Double = 0.0,

    // ── COLD-CHAIN ──
    // True if any item in this order needs to be carried in the
    // driver's canopy fridge. The UI shows a 🧊 badge when true.
    val requiresColdChain: Boolean = false,

    val assignedAt: String? = null,
    val acceptedAt: String? = null,
    val pickedUpAt: String? = null,
    val deliveredAt: String? = null,
)

data class BatchInfoDto(
    val count: Int = 0,
    val queue: List<BatchStopDto> = emptyList(),
    val nextStop: BatchStopDto? = null,
    val totalDistanceKm: Double = 0.0,
    val estimatedMinutes: Int = 0,
)

data class DriverJobsResponse(
    val batchState: String = "idle",       // "idle" | "at_hub" | "collected"
    val batch: BatchInfoDto = BatchInfoDto(),
    val past: List<BatchStopDto> = emptyList(),
    val isAvailable: Boolean = false,
    val verificationStatus: String = "not_submitted",

    val preferredZones: List<String> = emptyList(),
    val batchSize: Int = 0,
    val maxBatch: Int = 10,

    // ── COLD-CHAIN (CANOPY FRIDGE) ──
    val hasFridge: Boolean = false,
    val vehicleType: String? = null,
    val vehiclePlate: String? = null,

    val hub: HubDto? = null,
)

data class BatchActionResponse(
    val ok: Boolean = false,
    val batchState: String = "idle",
    val batchComplete: Boolean = false,
    val batch: BatchInfoDto = BatchInfoDto(),
)

data class DriverJobResponse(val job: DriverJobDto)

data class DriverStatsDto(
    val activeCount: Int = 0,
    val completedCount: Int = 0,
    val todayCount: Int = 0,
    val verificationStatus: String = "not_submitted",
    val batchState: String = "idle",
    val maxBatch: Int = 10,

    // ── COLD-CHAIN (CANOPY FRIDGE) ──
    val hasFridge: Boolean = false,
)

data class DriverAvailabilityRequest(val isAvailable: Boolean)
data class DriverAvailabilityResponse(val isAvailable: Boolean)
data class DriverStatusUpdate(val status: String)

// ── Zones ──

data class ZoneDto(
    val code: String,
    val label: String,
    val lat: Double? = null,
    val lng: Double? = null,
)

data class ZonesResponse(
    val zones: List<ZoneDto> = emptyList(),
    val preferredZones: List<String> = emptyList(),
)

data class ZonesSaveRequest(val zones: List<String>)

// ── Legacy batch DTO (kept for /driver/batch) ──

data class BatchActiveOrderDto(
    val deliveryId: String,
    val trackingId: String? = null,
    val zone: String? = null,
    val distanceFromHubKm: Double? = null,
    val requiresColdChain: Boolean = false,   // ── COLD-CHAIN ──
)

data class BatchDto(
    val count: Int = 0,
    val max: Int = 10,
    val zones: List<String> = emptyList(),
    val remaining: Int = 0,
    val batchState: String = "idle",
    val nextStop: BatchStopDto? = null,
    val activeOrders: List<BatchActiveOrderDto> = emptyList(),
)

// ── Verification ──

data class DriverVerificationDto(
    val id: String,
    val status: String,
    val rejectionReason: String? = null,
    val reviewedAt: String? = null,
    val submittedAt: String? = null,
    val licenceUrl: String? = null,
    val permitUrl: String? = null,
    val registrationUrl: String? = null,
    val carImageUrl: String? = null,
    val vehicleMake: String? = null,
    val vehicleModel: String? = null,
    val vehiclePlate: String? = null,
)

data class DriverVerificationMeResponse(
    val status: String = "not_submitted",
    val verification: DriverVerificationDto? = null,

    // ── COLD-CHAIN (CANOPY FRIDGE) ──
    val hasFridge: Boolean = false,
    val vehicleType: String? = null,
    val vehiclePlate: String? = null,
)

data class DriverVerificationSubmitResponse(
    val message: String? = null,
    val verification: DriverVerificationDto? = null,
)

// ── Driver wallet ──

data class DriverWalletDto(
    val balance: Double = 0.0,
    val pending: Double = 0.0,
    val pendingJobCount: Int = 0,
    val withdrawals: List<DriverWithdrawalDto> = emptyList(),
)

data class DriverWithdrawalDto(
    val id: String,
    val userEmail: String = "",
    val amount: Double = 0.0,
    val platformFee: Double = 0.0,
    val netAmount: Double = 0.0,
    val status: String = "pending",
    val rejectionReason: String? = null,
    val processedAt: String? = null,
    val createdAt: String = "",

    // ── PAYOUT DETAILS ──
    val payoutMethod: String? = null,    // "EWALLET" | "BANK"
    val walletNumber: String? = null,
    val bankName: String? = null,
    val accountNumber: String? = null,
    val accountName: String? = null,
)

data class DriverWithdrawResponse(val withdrawal: DriverWithdrawalDto)

// ── COLD-CHAIN (CANOPY FRIDGE) ──

data class VehicleUpdateRequest(
    val hasFridge: Boolean? = null,
    val vehicleType: String? = null,
    val vehiclePlate: String? = null,
)

data class VehicleUpdateResponse(
    val hasFridge: Boolean = false,
    val vehicleType: String? = null,
    val vehiclePlate: String? = null,
)