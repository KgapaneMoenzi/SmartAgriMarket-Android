package com.smartagrimarket.model.network

import android.content.Context
import android.net.Uri
import com.google.gson.JsonParser
import com.smartagrimarket.model.DailyOrderCount
import com.smartagrimarket.model.FarmerOrder
import com.smartagrimarket.model.FarmerOrderAddress
import com.smartagrimarket.model.FarmerOrderLine
import com.smartagrimarket.model.FarmerStats
import com.smartagrimarket.model.FarmerStatsExtended
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import retrofit2.Response

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

    suspend fun fetchBusinessDashboard(): ApiResult<BusinessDashboardDto> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getBusinessDashboard("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error("Could not load business stats (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun fetchQuotes(): ApiResult<List<BulkQuoteDto>> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in.")
        return try {
            val res = api.getFarmerQuotes("Bearer $token")
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!.quotes)
            else ApiResult.Error("Could not load quotes")
        } catch (e: Exception) { ApiResult.Error("Network error") }
    }

    suspend fun replyToQuote(quoteId: String, price: Double, date: String, note: String?): ApiResult<Unit> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in.")
        return try {
            val res = api.replyToBulkQuote("Bearer $token", quoteId, ReplyQuoteRequest(price, date, note))
            if (res.isSuccessful) ApiResult.Success(Unit) else ApiResult.Error("Could not send reply")
        } catch (e: Exception) { ApiResult.Error("Network error") }
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

    suspend fun uploadButcherCertificate(context: Context, uri: Uri): ApiResult<String> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return ApiResult.Error("Could not read the selected image.")

            val tempFile = File.createTempFile("butcher_", ".jpg", context.cacheDir)
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("certificate", tempFile.name, requestFile)

            val response = api.uploadButcherCertificate("Bearer $token", body)
            val responseBody = response.body()

            if (response.isSuccessful && responseBody != null) {
                ApiResult.Success(responseBody["certificateUrl"] ?: "")
            } else {
                val errorMsg = extractErrorBody(response)
                ApiResult.Error(errorMsg ?: "Could not upload certificate (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't upload image: ${e.message}")
        }
    }

    suspend fun getButcherStatus(): ApiResult<Boolean> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getButcherStatus("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body["isButcherVerified"] ?: false)
            } else {
                ApiResult.Error("Could not load status")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach server")
        }
    }

    private fun extractErrorBody(response: Response<*>): String? {
        return try {
            val raw = response.errorBody()?.string() ?: return null
            val json = JsonParser.parseString(raw).asJsonObject
            json.get("error")?.asString?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
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