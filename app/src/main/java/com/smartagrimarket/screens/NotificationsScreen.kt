package com.smartagrimarket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.NotificationDto
import com.smartagrimarket.model.network.NotificationsRepository
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onNotificationClick: (NotificationDto) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var allNotifications by remember { mutableStateOf<List<NotificationDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val visibleNotifications = allNotifications.filter { !it.isRead }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        when (val result = NotificationsRepository.fetchNotifications()) {
            is ApiResult.Success -> allNotifications = result.data.notifications
            is ApiResult.Error -> errorMessage = result.message
        }
        isLoading = false
    }

    fun dismissNotification(id: String) {
        allNotifications = allNotifications.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AgriCardBg)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AgriTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Notifications",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                Spacer(Modifier.weight(1f))
                if (visibleNotifications.isNotEmpty()) {
                    TextButton(onClick = {
                        allNotifications = allNotifications.map { it.copy(isRead = true) }
                        scope.launch { NotificationsRepository.markAllRead() }
                    }) {
                        Text(
                            "Mark all read",
                            style = MaterialTheme.typography.labelMedium,
                            color = AgriGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AgriGreen)
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriRedAlert.copy(alpha = 0.12f))
                            .padding(14.dp)
                    ) {
                        Text(
                            errorMessage!!,
                            color = AgriRedAlert,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                visibleNotifications.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(AgriMintLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔔", fontSize = 44.sp)
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "No new notifications",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AgriTextPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Updates about your orders and questions will show up here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AgriTextSecondary
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(visibleNotifications, key = { it.id }) { item ->
                            NotificationCard(
                                item = item,
                                onClick = {
                                    dismissNotification(item.id)
                                    scope.launch { NotificationsRepository.markRead(item.id) }
                                    onNotificationClick(item)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(item: NotificationDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AgriMintLight),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji ?: "🔔", fontSize = 22.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = AgriTextPrimary,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    item.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary,
                    maxLines = 2
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    relativeTime(item.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = AgriTextTertiary
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AgriGreen)
                )
            }
        }
    }
}

private fun relativeTime(iso: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val then = parser.parse(iso)?.time ?: return ""
        val diff = System.currentTimeMillis() - then
        when {
            diff < 60_000 -> "now"
            diff < 3_600_000 -> "${diff / 60_000}m"
            diff < 86_400_000 -> "${diff / 3_600_000}h"
            diff < 604_800_000 -> "${diff / 86_400_000}d"
            else -> "${diff / 604_800_000}w"
        }
    } catch (_: Exception) { "" }
}