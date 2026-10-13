package com.smartagrimarket.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

/**
 * Shown when a user with role "ADMIN" signs in.
 * Lets them choose between the web admin panel or using the app as a Buyer/Farmer.
 */
@Composable
fun AdminRolePickerDialog(
    adminName: String,
    onOpenAdminPanel: () -> Unit,
    onContinueAs: (String) -> Unit, // "BUYER" or "FARMER"
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        ),
        title = {
            Column {
                Text("Welcome, $adminName", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "You're signed in as an Admin. How would you like to continue?",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column {
                RoleOption(
                    emoji = "🌐",
                    title = "Open Admin Panel",
                    subtitle = "Full dashboard: users, verifications, withdrawals",
                    onClick = onOpenAdminPanel
                )
                Spacer(Modifier.height(10.dp))
                RoleOption(
                    emoji = "🛒",
                    title = "Continue as Buyer",
                    subtitle = "Browse & shop like a customer",
                    onClick = { onContinueAs("BUYER") }
                )
                Spacer(Modifier.height(10.dp))
                RoleOption(
                    emoji = "🌾",
                    title = "Continue as Farmer",
                    subtitle = "Manage your farm, products & wallet",
                    onClick = { onContinueAs("FARMER") }
                )
            }
        },
        confirmButton = { /* intentionally empty — options are in the body */ }
    )
}

@Composable
private fun RoleOption(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 26.sp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
                subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text("→", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}