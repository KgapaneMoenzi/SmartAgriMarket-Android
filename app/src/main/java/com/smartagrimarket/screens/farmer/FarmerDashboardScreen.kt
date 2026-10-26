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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.FarmerOrder
import com.smartagrimarket.model.FarmerStatsExtended
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.FarmerRepository
import com.smartagrimarket.model.network.TokenStore
import com.smartagrimarket.ui.theme.*

@Composable
fun FarmerDashboardScreen(
    onNotificationsClick: () -> Unit = {},
    onOrderClick: (FarmerOrder) -> Unit = {},
    onUploadProductClick: () -> Unit = {},
    onMyProductsClick: () -> Unit = {},
    onSeeAllOrdersClick: () -> Unit = {},
    onSeeAllRequestsClick: () -> Unit = {},
    onOpenQuotes: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    var stats by remember { mutableStateOf<FarmerStatsExtended?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var orders by remember { mutableStateOf<List<FarmerOrder>>(emptyList()) }

    val firstName = TokenStore.currentUser?.fullName?.split(" ")?.firstOrNull() ?: "Farmer"

    LaunchedEffect(Unit) {
        isLoading = true
        when (val result = FarmerRepository.fetchExtendedStats()) {
            is ApiResult.Success -> stats = result.data
            is ApiResult.Error -> { }
        }
        when (val result = FarmerRepository.fetchOrders()) {
            is ApiResult.Success -> orders = result.data
            is ApiResult.Error -> { }
        }
        isLoading = false
    }

    Scaffold(
        containerColor = AgriCreamBg,
        bottomBar = bottomBar
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Hi, $firstName 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = AgriTextPrimary
                        )
                        Text(
                            "Here's your farm today",
                            style = MaterialTheme.typography.bodySmall,
                            color = AgriTextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(AgriCardBg, CircleShape)
                            .clickable(onClick = onNotificationsClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔔", fontSize = 20.sp)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Hero Stats Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${stats?.ordersThisWeek ?: 0}",
                                color = AgriGreen,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "orders this week",
                                fontWeight = FontWeight.Bold,
                                color = AgriTextPrimary,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "You're all caught up. New orders will appear here.",
                                color = AgriTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(AgriMintLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌾", fontSize = 32.sp)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Mini Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(label = "Listings", value = "${stats?.activeListings ?: 0}", modifier = Modifier.weight(1f))
                    StatCard(label = "This week", value = "${stats?.ordersThisWeek ?: 0}", modifier = Modifier.weight(1f))
                    StatCard(label = "Pending", value = "R${"%.0f".format(stats?.pendingPaymentTotal ?: 0.0)}", modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(24.dp))
            }

            // Quick Actions
            item {
                Text("Quick actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AgriTextPrimary)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onUploadProductClick,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("+  Upload", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onMyProductsClick,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AgriTextPrimary.copy(alpha = 0.2f))
                    ) {
                        Text("📦  My products", fontWeight = FontWeight.Bold, color = AgriTextPrimary)
                    }
                }
                Spacer(Modifier.height(12.dp))

                // BULK QUOTE BUTTON (GREEN)
                Button(
                    onClick = onOpenQuotes,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreen, contentColor = Color.White)
                ) {
                    Text("📦 Bulk Quote Requests", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))
            }

            // Recent Orders Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent orders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AgriTextPrimary)
                    Text(
                        "See all",
                        style = MaterialTheme.typography.bodySmall,
                        color = AgriGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onSeeAllOrdersClick)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Orders List
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AgriGreen)
                    }
                }
            } else if (orders.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No orders yet.", color = AgriTextSecondary)
                    }
                }
            } else {
                items(orders.take(3)) { order ->
                    // SHOWS "PACK NOW" INSTEAD OF "CONFIRMED"
                    val displayStatus = when (order.trackingStatus) {
                        "order_placed" -> "Pack now"
                        "processing" -> "Packed"
                        "dispatched" -> "Dispatched"
                        "out_for_delivery" -> "Out for delivery"
                        "delivered" -> "Delivered"
                        else -> order.status.replaceFirstChar { it.uppercase() }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { onOrderClick(order) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(AgriMintLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📦", fontSize = 24.sp)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Order ${order.trackingId} · ${order.address.city ?: "Local"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AgriTextPrimary
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "${order.buyerName} · ${order.items.size} items · R${"%.2f".format(order.total)}",
                                    fontSize = 12.sp,
                                    color = AgriTextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(AgriMintLight, RoundedCornerShape(50))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    displayStatus,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriGreenDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AgriTextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, color = AgriTextSecondary)
        }
    }
}