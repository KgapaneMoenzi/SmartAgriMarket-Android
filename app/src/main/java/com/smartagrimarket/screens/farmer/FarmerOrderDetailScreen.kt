package com.smartagrimarket.screens.farmer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.FarmerOrder
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.FarmerRepository
import com.smartagrimarket.model.network.TokenStore
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FarmerOrderDetailScreen(
    order: FarmerOrder,
    onBack: () -> Unit,
    onStatusUpdated: (FarmerOrder) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentOrder by remember(order) { mutableStateOf(order) }
    var isSaving by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val myEmail = TokenStore.currentUser?.email

    LaunchedEffect(order.id) {
        isRefreshing = true
        when (val result = FarmerRepository.fetchOrders()) {
            is ApiResult.Success -> {
                val fresh = result.data.find { it.id == order.id }
                if (fresh != null) currentOrder = fresh
            }
            is ApiResult.Error -> { /* silent */ }
        }
        isRefreshing = false
    }

    // ── Status derivation ──
    val isPaid = currentOrder.status == "confirmed"
    val isPendingPayment = currentOrder.status == "pending_payment"
    val myItemsPacked = currentOrder.myItemsPacked(myEmail)
    val orderLocked = currentOrder.trackingStatus in setOf(
        "dispatched", "out_for_delivery", "delivered"
    )
    val needsPacking = isPaid && !myItemsPacked && !orderLocked
    val isDispatched = currentOrder.trackingStatus == "dispatched"
    val isOutForDelivery = currentOrder.trackingStatus == "out_for_delivery"
    val isDelivered = currentOrder.trackingStatus == "delivered"

    fun openHubNavigation() {
        val hub = currentOrder.hub ?: return
        val lat = hub.latitude ?: return
        val lng = hub.longitude ?: return
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(hub.name)})")
        try {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) { }
    }

    fun refreshOrder() {
        scope.launch {
            isRefreshing = true
            when (val result = FarmerRepository.fetchOrders()) {
                is ApiResult.Success -> {
                    val fresh = result.data.find { it.id == order.id }
                    if (fresh != null) currentOrder = fresh
                }
                else -> {}
            }
            isRefreshing = false
        }
    }

    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Top bar ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AgriCardBg)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", fontSize = 20.sp, color = AgriTextPrimary)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Order ${currentOrder.displayId}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AgriCardBg)
                        .clickable(enabled = !isRefreshing) { refreshOrder() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = AgriTextPrimary
                        )
                    } else {
                        Text("🔄", fontSize = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Header card: buyer + area + status ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AgriCardBg)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AgriMintLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👤", fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Buyer",
                                style = MaterialTheme.typography.labelSmall,
                                color = AgriTextSecondary
                            )
                            Text(
                                currentOrder.buyerName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = AgriTextPrimary
                            )
                        }
                    }
                    StatusPill(
                        trackingStatus = currentOrder.trackingStatus,
                        label = currentOrder.statusLabel
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AgriMintLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📍", fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Deliver to",
                            style = MaterialTheme.typography.labelSmall,
                            color = AgriTextSecondary
                        )
                        Text(
                            currentOrder.area,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = AgriTextPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── ACTION ZONE ──
            // The one thing the farmer needs to do right now.
            when {
                isPendingPayment -> {
                    InfoBanner(
                        emoji = "⏳",
                        title = "Awaiting payment",
                        body = "The buyer hasn't paid yet. You'll be able to pack this order as soon as payment is confirmed."
                    )
                }

                needsPacking -> {
                    ActionCard(
                        emoji = "📦",
                        title = "Ready to pack",
                        body = "Pack your items and drop them at the hub. The driver will collect from there.",
                        buttonLabel = "Packed & dropped at hub",
                        isSaving = isSaving,
                        onAction = { showConfirmDialog = true }
                    )
                }

                myItemsPacked && !isDispatched && !isOutForDelivery && !isDelivered -> {
                    InfoBanner(
                        emoji = "✅",
                        title = "Your items are at the hub",
                        body = "Waiting for the driver to collect. You don't need to do anything else."
                    )
                }

                isDispatched -> {
                    InfoBanner(
                        emoji = "🚚",
                        title = "Driver collected from hub",
                        body = "Your items are with the driver. No action needed."
                    )
                }

                isOutForDelivery -> {
                    InfoBanner(
                        emoji = "🛵",
                        title = "Out for delivery",
                        body = "The order is on its way to the buyer. No action needed."
                    )
                }

                isDelivered -> {
                    InfoBanner(
                        emoji = "🎉",
                        title = "Delivered",
                        body = "This order is complete. Your wallet will be credited shortly."
                    )
                }

                else -> {
                    InfoBanner(
                        emoji = "ℹ️",
                        title = "In progress",
                        body = "Current status: ${currentOrder.statusLabel}."
                    )
                }
            }

            // ── Feedback ──
            errorMessage?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AgriRedAlert.copy(alpha = 0.12f))
                        .padding(14.dp)
                ) {
                    Text(msg, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
                }
            }
            successMessage?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AgriGreen.copy(alpha = 0.15f))
                        .padding(14.dp)
                ) {
                    Text(
                        msg,
                        color = AgriGreenDark,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Hub card (reference) ──
            currentOrder.hub?.let { hub ->
                Spacer(Modifier.height(20.dp))
                Text(
                    "Drop-off point",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(AgriCardBg)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AgriGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏬", fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                hub.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = AgriTextPrimary
                            )
                            hub.address?.takeIf { it.isNotBlank() }?.let { addr ->
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    addr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AgriTextSecondary
                                )
                            }
                        }
                    }
                    if (hub.latitude != null && hub.longitude != null) {
                        Spacer(Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { openHubNavigation() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AgriGreenDark
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, AgriGreen.copy(alpha = 0.5f)
                            )
                        ) {
                            Text("🗺️  Navigate to hub", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // ── Items ──
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Your items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                Text(
                    "${currentOrder.items.size} item${if (currentOrder.items.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary
                )
            }
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AgriCardBg)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                currentOrder.items.forEach { line ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${line.name} × ${line.quantity}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AgriTextPrimary
                        )
                        Text(
                            "R${"%.2f".format(line.price * line.quantity)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AgriTextPrimary
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = AgriTextTertiary.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Your subtotal",
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    Text(
                        "R${"%.2f".format(currentOrder.total)}",
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Confirmation dialog ──
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSaving) showConfirmDialog = false },
            title = { Text("Mark as packed?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Confirm you've dropped your items at the hub. " +
                            "This notifies the driver that they can collect.\n\n" +
                            "You can't undo this."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !isSaving,
                    onClick = {
                        showConfirmDialog = false
                        errorMessage = null
                        successMessage = null
                        scope.launch {
                            isSaving = true
                            when (val result = FarmerRepository.updateOrderStatus(
                                orderId = currentOrder.id,
                                trackingStatus = "processing"
                            )) {
                                is ApiResult.Success -> {
                                    currentOrder = result.data
                                    onStatusUpdated(result.data)
                                    successMessage = "You're done. The driver has been notified."
                                }
                                is ApiResult.Error -> errorMessage = result.message
                            }
                            isSaving = false
                        }
                    }
                ) {
                    Text("Yes, packed", color = AgriGreenDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isSaving,
                    onClick = { showConfirmDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Action card — the primary CTA for the farmer on this screen.
// ─────────────────────────────────────────────────────────────
@Composable
private fun ActionCard(
    emoji: String,
    title: String,
    body: String,
    buttonLabel: String,
    isSaving: Boolean,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AgriGreen.copy(alpha = 0.10f))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AgriGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = AgriGreenDark
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    body,
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onAction,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AgriGreen,
                contentColor = Color.White
            )
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Saving…", fontWeight = FontWeight.Bold)
            } else {
                Text("📦  $buttonLabel", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Info banner — calm, low-key, still readable.
// ─────────────────────────────────────────────────────────────
@Composable
private fun InfoBanner(emoji: String, title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AgriCardBg)
            .padding(20.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AgriMintLight),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = AgriTextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Status pill — bigger, bolder badge for the header card.
// ─────────────────────────────────────────────────────────────
@Composable
private fun StatusPill(trackingStatus: String, label: String) {
    val color = when (trackingStatus) {
        "delivered" -> AgriGreen
        "shipped" -> AgriGreen
        "processing" -> AgriGreen
        "dispatched" -> AgriGreen
        "out_for_delivery" -> AgriGreen
        else -> AgriYellow
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AgriTextPrimary
        )
    }
}