package com.smartagrimarket.model.network

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.smartagrimarket.MainActivity
import com.smartagrimarket.R
import com.smartagrimarket.model.network.NotificationDto

object NotificationHelper {

    /** Extra keys used to pass the deep-link payload to MainActivity. */
    const val EXTRA_LINK_TYPE = "notification_link_type"
    const val EXTRA_LINK_ID = "notification_link_id"
    const val EXTRA_NOTIFICATION_ID = "notification_id"

    /**
     * Fire a system notification for a single [NotificationDto].
     * Uses the notification's id (hashed) as the system notification id,
     * so repeated posts for the same item replace rather than stack.
     */
    fun show(context: Context, dto: NotificationDto) {
        // Only fire for actionable types
        if (dto.type !in FIRE_TYPES) return

        val channel = NotificationChannels.channelFor(dto.type)

        // Deep-link intent — opens MainActivity with the payload attached
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_LINK_TYPE, dto.linkType)
            putExtra(EXTRA_LINK_ID, dto.linkId)
            putExtra(EXTRA_NOTIFICATION_ID, dto.id)
        }

        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pending = PendingIntent.getActivity(
            context,
            dto.id.hashCode(),
            intent,
            pendingFlags
        )

        // Emoji + title as the content title (emoji first so it looks like
        // a friendly banner)
        val title = if (!dto.emoji.isNullOrBlank()) {
            "${dto.emoji}  ${dto.title}"
        } else {
            dto.title
        }

        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(dto.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(dto.body))
            .setAutoCancel(true)
            .setPriority(
                if (dto.type == "order") NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pending)

        try {
            NotificationManagerCompat.from(context)
                .notify(dto.id.hashCode(), builder.build())
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS denied — nothing to do
        }
    }

    /**
     * Which notification types should pop as a system banner.
     * Edit this set to change behavior globally.
     */
    val FIRE_TYPES = setOf("order", "question", "request", "message")
}