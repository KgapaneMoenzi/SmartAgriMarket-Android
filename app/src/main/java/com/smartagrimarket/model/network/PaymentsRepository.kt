package com.smartagrimarket.model.network

import android.util.Log
import com.google.gson.JsonParser

object PaymentsRepository {
    private val api = RetrofitClient.api

    suspend fun createPayment(
        amount: Double,
        itemName: String,
        buyerEmail: String,
        buyerName: String,
        items: List<PaymentItemPayload>,
        deliveryMethod: String? = null,
        address: AddressDto? = null,
        pickupPointId: String? = null,
        deliveryLat: Double? = null,
        deliveryLng: Double? = null,
    ): ApiResult<PaymentCreateResponse> {
        return try {
            val response = api.createPayment(
                PaymentCreateRequest(
                    amount = "%.2f".format(amount),
                    itemName = itemName,
                    buyerEmail = buyerEmail,
                    buyerName = buyerName,
                    items = items,
                    deliveryMethod = deliveryMethod,
                    address = address,
                    pickupPointId = pickupPointId,
                    deliveryLat = deliveryLat,
                    deliveryLng = deliveryLng,
                )
            )
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                val raw = response.errorBody()?.string()
                Log.e("PaymentsRepo", "createPayment failed ${response.code()}: $raw")
                val friendly = extractErrorMessage(raw)
                    ?: "Could not start payment (${response.code()})"
                ApiResult.Error(friendly)
            }
        } catch (e: Exception) {
            Log.e("PaymentsRepo", "createPayment crashed", e)
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    private fun extractErrorMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            val json = JsonParser.parseString(raw).asJsonObject
            json.get("error")?.asString?.takeIf { it.isNotBlank() }
                ?: json.get("message")?.asString?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            raw.take(200)
        }
    }
}