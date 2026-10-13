package com.smartagrimarket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartagrimarket.model.network.NotificationSettingsDto
import com.smartagrimarket.ui.theme.*

/**
 * Email-only notification preferences. SMS/WhatsApp aren't offered —
 * there's no free way to send those, so this app sticks to email,
 * which is already wired via Resend.
 */
@Composable
fun NotificationSettingsScreen(
    settings: NotificationSettingsDto?,
    isLoading: Boolean = false,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onBack: () -> Unit = {},
    onSave: (NotificationSettingsDto) -> Unit = {}
) {
    var orderUpdates by remember(settings) { mutableStateOf(settings?.orderUpdates ?: true) }
    var priceAlerts by remember(settings) { mutableStateOf(settings?.priceAlerts ?: false) }
    var promotions by remember(settings) { mutableStateOf(settings?.promotions ?: false) }
    var newMessages by remember(settings) { mutableStateOf(settings?.newMessages ?: true) }

    val hasChanges = settings != null && (
            orderUpdates != settings.orderUpdates ||
                    priceAlerts != settings.priceAlerts ||
                    promotions != settings.promotions ||
                    newMessages != settings.newMessages
            )

    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onBack)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = AgriTextSecondary)
                Spacer(Modifier.width(4.dp))
                Text("Notification settings", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "We'll email you at your account address for the notifications you enable below.",
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary
            )

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AgriTextPrimary)
                }
            } else {
                Text("What you'll be notified about", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AgriCardBg)
                        .padding(vertical = 4.dp)
                ) {
                    ToggleRow("Order updates", orderUpdates) { orderUpdates = it }
                    Divider(color = AgriCreamBg, thickness = 1.dp)
                    ToggleRow("Price drops on wishlist items", priceAlerts) { priceAlerts = it }
                    Divider(color = AgriCreamBg, thickness = 1.dp)
                    ToggleRow("Promotions & Fresh Friday deals", promotions) { promotions = it }
                    Divider(color = AgriCreamBg, thickness = 1.dp)
                    ToggleRow("New messages", newMessages) { newMessages = it }
                }

                if (errorMessage != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(errorMessage, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        onSave(NotificationSettingsDto(orderUpdates, priceAlerts, promotions, newMessages))
                    },
                    enabled = !isSaving && hasChanges,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AgriYellow, contentColor = AgriTextPrimary),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = AgriTextPrimary, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Saving…", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Save changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AgriYellow, checkedTrackColor = AgriYellow.copy(alpha = 0.4f))
        )
    }
}