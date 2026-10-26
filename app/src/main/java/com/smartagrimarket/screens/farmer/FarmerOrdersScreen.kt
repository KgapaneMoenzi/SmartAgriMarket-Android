package com.smartagrimarket.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.FarmerOrder
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.FarmerRepository
import com.smartagrimarket.model.network.TokenStore
import com.smartagrimarket.ui.theme.*

private enum class OrderFilter(val label: String) {
    ALL("All"),
    TO_PACK("To pack"),
    IN_PROGRESS("In progress"),
    DELIVERED("Delivered")
}

@Composable
fun FarmerOrdersScreen(
    onOrderClick: (FarmerOrder) -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    var orders by remember { mutableStateOf<List<FarmerOrder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var filter by remember { mutableStateOf(OrderFilter.ALL) }
    var reloadKey by remember { mutableStateOf(0) }

    val myEmail = TokenStore.currentUser?.email

    LaunchedEffect(reloadKey) {
        isLoading = true
        errorMessage = null
        when (val result = FarmerRepository.fetchOrders()) {
            is ApiResult.Success -> orders = result.data
            is ApiResult.Error -> errorMessage = result.message
        }
        isLoading = false
    }

    fun matchesFilter(order: FarmerOrder, f: OrderFilter): Boolean = when (f) {
        OrderFilter.ALL -> true

        OrderFilter.TO_PACK ->
            order.status == "confirmed" &&
                    !myEmail.isNullOrBlank() &&
                    !order.packedByFarmers.contains(myEmail) &&
                    order.trackingStatus != "dispatched" &&
                    order.trackingStatus != "out_for_delivery" &&
                    order.trackingStatus != "delivered"

        OrderFilter.IN_PROGRESS ->
            order.trackingStatus == "dispatched" ||
                    order.trackingStatus == "out_for_delivery"

        OrderFilter.DELIVERED ->
            order.trackingStatus == "delivered"
    }

    val filtered = orders.filter { matchesFilter(it, filter) }

    Scaffold(
        containerColor = AgriCreamBg,
        bottomBar = bottomBar
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // ── Header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Orders",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (orders.isEmpty()) "No orders yet"
                        else "${orders.size} total · ${orders.count { matchesFilter(it, OrderFilter.TO_PACK) }} to pack",
                        style = MaterialTheme.typography.bodySmall,
                        color = AgriTextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AgriCardBg)
                        .clickable(enabled = !isLoading) { reloadKey++ },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
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

            Spacer(Modifier.height(18.dp))

            // ── Filter chips with counts ──
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OrderFilter.values().forEach { f ->
                    val selected = f == filter
                    val count = orders.count { matchesFilter(it, f) }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) AgriYellow else AgriCardBg)
                            .clickable { filter = f }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = if (f == OrderFilter.ALL) f.label else "${f.label} · $count",
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = AgriTextPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            when {
                isLoading && orders.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AgriTextPrimary)
                    }
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AgriRedAlert.copy(alpha = 0.12f))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            errorMessage!!,
                            color = AgriRedAlert,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(10.dp))
                        TextButton(onClick = { reloadKey++ }) {
                            Text("Retry", fontWeight = FontWeight.Bold, color = AgriGreenDark)
                        }
                    }
                }

                filtered.isEmpty() -> {
                    EmptyState(
                        emoji = "📦",
                        title = when {
                            orders.isEmpty() -> "No orders yet"
                            filter == OrderFilter.TO_PACK -> "Nothing to pack right now"
                            else -> "No orders in this filter"
                        },
                        hint = when {
                            orders.isEmpty() -> "Orders appear here once buyers purchase your products."
                            filter == OrderFilter.TO_PACK -> "All caught up. You've packed and dropped off everything."
                            else -> "Try a different filter."
                        }
                    )
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filtered, key = { it.id }) { order ->
                            OrderRow(order = order, onClick = { onOrderClick(order) })
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

// ── NEW: Added the missing OrderRow component ──
@Composable
fun OrderRow(order: FarmerOrder, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AgriCardBg)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(order.buyerName, fontWeight = FontWeight.Bold, color = AgriTextPrimary)
                Text("R${"%.2f".format(order.total)}", fontWeight = FontWeight.Bold, color = AgriGreenDark)
            }
            Spacer(Modifier.height(4.dp))
            Text("Order #${order.trackingId}", fontSize = 12.sp, color = AgriTextSecondary)
            Spacer(Modifier.height(12.dp))

            val statusColor = when (order.status) {
                "confirmed" -> AgriGreenDark
                "pending_payment" -> AgriOrange
                else -> AgriTextSecondary
            }

            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = order.status.replace("_", " ").uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun EmptyState(emoji: String, title: String, hint: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AgriCardBg)
            .padding(vertical = 32.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(AgriMintLight),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 28.sp)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = AgriTextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            hint,
            style = MaterialTheme.typography.bodySmall,
            color = AgriTextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}