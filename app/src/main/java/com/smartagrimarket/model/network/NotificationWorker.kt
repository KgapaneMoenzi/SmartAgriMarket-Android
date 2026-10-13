package com.smartagrimarket.model.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class NotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // No session → nothing to do
        if (TokenStore.token == null) return Result.success()

        val prefs = applicationContext.getSharedPreferences(
            PREFS_NAME, Context.MODE_PRIVATE
        )

        // First run after install/login → seed the seen set, don't spam
        val hasSeenBefore = prefs.contains(KEY_SEEN_IDS)

        return when (val result = NotificationsRepository.fetchNotifications()) {
            is ApiResult.Success -> {
                val all = result.data.notifications
                val seenIds = prefs.getStringSet(KEY_SEEN_IDS, emptySet())?.toMutableSet()
                    ?: mutableSetOf()

                // Fire for each new item that isn't already read
                all.forEach { dto ->
                    val already = dto.id in seenIds
                    if (!already && !hasSeenBefore) {
                        // Seed mode — remember but don't pop
                        seenIds.add(dto.id)
                    } else if (!already && !dto.isRead) {
                        NotificationHelper.show(applicationContext, dto)
                        seenIds.add(dto.id)
                    } else {
                        seenIds.add(dto.id)
                    }
                }

                // Trim to the most recent ~200 ids so prefs don't grow forever
                val trimmed = seenIds.toList().takeLast(200).toSet()
                prefs.edit().putStringSet(KEY_SEEN_IDS, trimmed).apply()

                Result.success()
            }
            is ApiResult.Error -> {
                // Network hiccup — try again next cycle
                Result.retry()
            }
        }
    }

    companion object {
        const val PREFS_NAME = "agrimarket_notifications"
        const val KEY_SEEN_IDS = "seen_notification_ids"
    }
}