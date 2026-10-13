package com.smartagrimarket.model.network

data class NotificationDto(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val emoji: String?,
    val isRead: Boolean,
    val linkType: String?,
    val linkId: String?,
    val createdAt: String
)

data class NotificationsListResponse(
    val notifications: List<NotificationDto>,
    val unreadCount: Int
)

data class UnreadCountResponse(val unreadCount: Int)