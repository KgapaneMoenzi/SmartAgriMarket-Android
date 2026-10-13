package com.smartagrimarket.model.network

import com.smartagrimarket.model.DailyOrderCount
import com.smartagrimarket.model.FarmerOrder
import com.smartagrimarket.model.FarmerOrderAddress
import com.smartagrimarket.model.FarmerOrderLine
import com.smartagrimarket.model.FarmerStats
import com.smartagrimarket.model.FarmerStatsExtended

object FarmerRepository {
    private val api = RetrofitClient.api

    suspend fun fetchStats(): ApiResult<FarmerStats> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getFarmerStats("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(
                    FarmerStats(
                        activeListings = body.activeListings,
                        ordersThisWeek = body.ordersThisWeek,
                        pendingPaymentTotal = body.pendingPaymentTotal,
                        ordersToPack = body.ordersToPack
                    )
                )
            } else {
                ApiResult.Error("Could not load stats (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun fetchExtendedStats(): ApiResult<FarmerStatsExtended> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getFarmerStatsExtended("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(
                    FarmerStatsExtended(
                        activeListings = body.activeListings,
                        ordersThisWeek = body.ordersThisWeek,
                        ordersTotal = body.ordersTotal,
                        revenueThisWeek = body.revenueThisWeek,
                        revenueTotal = body.revenueTotal,
                        pendingPaymentTotal = body.pendingPaymentTotal,
                        ordersToPack = body.ordersToPack,
                        averageOrderValue = body.averageOrderValue,
                        topSellingProduct = body.topSellingProduct,
                        deliveryFeesThisWeek = body.deliveryFeesThisWeek,
                        openRequestsCount = body.openRequestsCount,
                        topOpenRequestName = body.topOpenRequestName,
                        topOpenRequestCount = body.topOpenRequestCount,
                        dailyOrderCounts = body.dailyOrderCounts.map {
                            DailyOrderCount(it.dayLabel, it.dateLabel, it.count)
                        }
                    )
                )
            } else {
                ApiResult.Error("Could not load stats (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun fetchOrders(): ApiResult<List<FarmerOrder>> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getFarmerOrders("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body.orders.map { it.toModel() })
            } else {
                ApiResult.Error("Could not load orders (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun updateOrderStatus(
        orderId: String,
        trackingStatus: String,
        trackingNote: String? = null,
        estimatedDelivery: String? = null
    ): ApiResult<FarmerOrder> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.updateFarmerOrderStatus(
                "Bearer $token",
                orderId,
                FarmerOrderStatusUpdate(
                    trackingStatus = trackingStatus,
                    trackingNote = trackingNote,
                    estimatedDelivery = estimatedDelivery
                )
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body.order.toModel())
            } else {
                ApiResult.Error("Could not update order (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun fetchCropRecommendations(): ApiResult<CropRecommendationsResponse> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getCropRecommendations("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error("Could not load crop recommendations (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    /**
     * Records that the farmer dismissed a crop recommendation.
     * Telemetry-only — feeds the Stage-3 learning-to-rank pipeline.
     */
    suspend fun dismissCropRecommendation(crop: String): ApiResult<Unit> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.dismissCropRecommendation("Bearer $token", crop)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error("Could not record dismiss (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server.")
        }
    }
}

private fun FarmerOrderDto.toModel(): FarmerOrder = FarmerOrder(
    id = id,
    trackingId = trackingId,
    buyerName = buyerName,
    buyerEmail = buyerEmail,
    items = items.map { FarmerOrderLine(it.name, it.quantity, it.price) },
    total = total,
    address = FarmerOrderAddress(
        line1 = address.line1,
        line2 = address.line2,
        city = address.city,
        province = address.province,
        postalCode = address.postalCode,
        street = address.street,
        zip = address.zip
    ),
    status = status,
    trackingStatus = trackingStatus,
    trackingNote = trackingNote,
    estimatedDelivery = estimatedDelivery,
    createdAt = createdAt
)