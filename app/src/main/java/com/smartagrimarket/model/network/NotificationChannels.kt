
package com.smartagrimarket.model.network
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService

object NotificationChannels {

    const val CHANNEL_ORDERS = "orders"
    const val CHANNEL_QUESTIONS = "questions"
    const val CHANNEL_REQUESTS = "requests"
    const val CHANNEL_GENERAL = "general"

    /**
     * Create all channels. Safe to call repeatedly — Android ignores
     * existing channels. Call this once from MainActivity.onCreate.
     */
    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService<NotificationManager>() ?: return

        val orders = NotificationChannel(
            CHANNEL_ORDERS,
            "Orders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "New orders and order status updates"
            enableVibration(true)
        }

        val questions = NotificationChannel(
            CHANNEL_QUESTIONS,
            "Buyer questions",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Buyers asking about your products"
        }

        val requests = NotificationChannel(
            CHANNEL_REQUESTS,
            "Buyer requests",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Buyers looking for products you might sell"
        }

        val general = NotificationChannel(
            CHANNEL_GENERAL,
            "General",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Everything else"
        }

        manager.createNotificationChannels(listOf(orders, questions, requests, general))
    }

    /** Pick the right channel for a notification type. */
    fun channelFor(type: String): String = when (type) {
        "order" -> CHANNEL_ORDERS
        "question" -> CHANNEL_QUESTIONS
        "request" -> CHANNEL_REQUESTS
        else -> CHANNEL_GENERAL
    }
}