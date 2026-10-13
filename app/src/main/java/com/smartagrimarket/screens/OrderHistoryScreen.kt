package com.smartagrimarket.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.network.OrderDto
import com.smartagrimarket.ui.theme.*

@Composable
fun OrderHistoryScreen(
    orders: List<OrderDto>,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onBack: () -> Unit = {},
    onOrderClick: (OrderDto) -> Unit = {},
    onPickDeliverySlotClick: (OrderDto) -> Unit = {}
) {
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
                    "My Orders",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
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
                            errorMessage,
                            color = AgriRedAlert,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                orders.isEmpty() -> {
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
                            Text("🧾", fontSize = 44.sp)
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "No orders yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AgriTextPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Your past orders will show up here.",
                            color = AgriTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(orders, key = { it.id }) { order ->
                            OrderCard(
                                order = order,
                                onClick = { onOrderClick(order) },
                                onPickSlotClick = { onPickDeliverySlotClick(order) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: OrderDto,
    onClick: () -> Unit,
    onPickSlotClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top row — tracking ID + status pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    order.trackingId,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                StatusPill(order.status, order.trackingStatus)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = AgriBorderLight)
            Spacer(Modifier.height(12.dp))

            // Items count + total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "${order.items.size} item${if (order.items.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AgriTextSecondary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        statusLabel(order.status, order.trackingStatus),
                        style = MaterialTheme.typography.labelSmall,
                        color = AgriTextTertiary
                    )
                }
                Text(
                    "R${"%.2f".format(order.total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgriGreenDark
                )
            }

            // Row of item names preview
            if (order.items.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    order.items.take(3).joinToString(", ") { it.name } +
                            if (order.items.size > 3) " +${order.items.size - 3} more" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary,
                    maxLines = 1
                )
            }

            // Pick delivery window action
            if (order.trackingStatus == "shipped") {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onPickSlotClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriGreen,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Pick delivery window", fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "View details",
                        style = MaterialTheme.typography.labelMedium,
                        color = AgriGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = AgriGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: String, trackingStatus: String) {
    val (label, bg, fg) = when {
        status == "cancelled" -> Triple("Cancelled", AgriRedAlert.copy(alpha = 0.15f), AgriRedAlert)
        status == "pending_payment" -> Triple("Awaiting payment", AgriOrange.copy(alpha = 0.15f), AgriOrange)
        trackingStatus == "delivered" -> Triple("Delivered", AgriGreen.copy(alpha = 0.15f), AgriGreen)
        trackingStatus == "out_for_delivery" || trackingStatus == "shipped" ->
            Triple("Out for delivery", AgriMintLight, AgriGreenDark)
        trackingStatus == "processing" -> Triple("Processing", AgriMintLight, AgriGreenDark)
        else -> Triple("Confirmed", AgriMintLight, AgriGreenDark)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

private fun statusLabel(status: String, trackingStatus: String): String {
    return when (status) {
        "pending_payment" -> "Awaiting payment"
        "cancelled" -> "Cancelled"
        "confirmed" -> when (trackingStatus) {
            "order_placed" -> "Order placed"
            "processing" -> "Processing"
            "shipped" -> "Out for delivery"
            "delivered" -> "Delivered"
            else -> trackingStatus.replaceFirstChar { it.uppercase() }
        }
        else -> status.replaceFirstChar { it.uppercase() }
    }
}