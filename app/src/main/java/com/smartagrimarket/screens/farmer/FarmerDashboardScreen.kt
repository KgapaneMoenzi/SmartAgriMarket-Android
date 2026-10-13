package com.smartagrimarket.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
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
import com.smartagrimarket.model.FarmerStats
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.FarmerRepository
import com.smartagrimarket.model.network.NotificationsRepository
import com.smartagrimarket.model.network.ProductRequestDto
import com.smartagrimarket.model.network.RequestsRepository
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
    bottomBar: @Composable () -> Unit = {}
) {
    var stats by remember { mutableStateOf<FarmerStats?>(null) }
    var recentOrders by remember { mutableStateOf<List<FarmerOrder>>(emptyList()) }
    var recentRequests by remember { mutableStateOf<List<ProductRequestDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var unreadCount by remember { mutableStateOf(0) }

    val firstName = TokenStore.currentUser?.fullName?.substringBefore(" ") ?: "there"

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        when (val statsResult = FarmerRepository.fetchStats()) {
            is ApiResult.Success -> stats = statsResult.data
            is ApiResult.Error -> errorMessage = statsResult.message
        }
        when (val ordersResult = FarmerRepository.fetchOrders()) {
            is ApiResult.Success -> recentOrders = ordersResult.data.take(3)
            is ApiResult.Error -> if (errorMessage == null) errorMessage = ordersResult.message
        }
        when (val reqResult = RequestsRepository.fetchFarmerRequests()) {
            is ApiResult.Success -> recentRequests = reqResult.data
                .filter { it.status == "open" }
                .take(3)
            is ApiResult.Error -> { /* silent */ }
        }
        when (val countResult = NotificationsRepository.fetchUnreadCount()) {
            is ApiResult.Success -> unreadCount = countResult.data
            is ApiResult.Error -> { /* silent */ }
        }
        isLoading = false
    }

    val activeListings = stats?.activeListings ?: 0
    val ordersThisWeek = stats?.ordersThisWeek ?: 0
    val pendingPaymentTotal = stats?.pendingPaymentTotal ?: 0.0
    val ordersToPack = stats?.ordersToPack ?: 0

    val hasAttentionItems = ordersToPack > 0 || recentRequests.isNotEmpty()

    Scaffold(
        containerColor = AgriCreamBg,
        bottomBar = bottomBar
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(20.dp))

            // ── Header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hi, $firstName 👋",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Here's your farm today",
                        style = MaterialTheme.typography.bodySmall,
                        color = AgriTextSecondary
                    )
                }
                Box {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = AgriTextPrimary
                        )
                    }
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 4.dp, end = 4.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(AgriRedAlert),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (isLoading && stats == null) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AgriTextPrimary)
                }
            } else {
                // ── Hero card ──
                HeroCard(
                    ordersToPack = ordersToPack,
                    ordersThisWeek = ordersThisWeek
                )

                Spacer(Modifier.height(24.dp))

                // ── Needs attention ──
                if (hasAttentionItems) {
                    Text(
                        "Needs your attention",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    Spacer(Modifier.height(12.dp))

                    if (ordersToPack > 0) {
                        AttentionRow(
                            emoji = "📦",
                            title = "$ordersToPack order${if (ordersToPack == 1) "" else "s"} to pack",
                            body = "Buyers are waiting — pack and drop at hub.",
                            badgeColor = AgriRedAlert,
                            onClick = onSeeAllOrdersClick
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    if (recentRequests.isNotEmpty()) {
                        AttentionRow(
                            emoji = "🌱",
                            title = "${recentRequests.size} new buyer request${if (recentRequests.size == 1) "" else "s"}",
                            body = "Buyers are looking for products you might sell.",
                            badgeColor = AgriGreen,
                            onClick = onSeeAllRequestsClick
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                }

                // ── Compact stats ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CompactStat(
                        value = activeListings.toString(),
                        label = "Listings",
                        modifier = Modifier.weight(1f)
                    )
                    CompactStat(
                        value = ordersThisWeek.toString(),
                        label = "This week",
                        modifier = Modifier.weight(1f)
                    )
                    CompactStat(
                        value = "R${"%.0f".format(pendingPaymentTotal)}",
                        label = "Pending",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── Quick actions ──
                Text(
                    "Quick actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onUploadProductClick,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AgriYellow,
                            contentColor = AgriTextPrimary
                        ),
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Upload",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onMyProductsClick,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).height(56.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            AgriTextPrimary.copy(alpha = 0.15f)
                        )
                    ) {
                        Text("📦", fontSize = 15.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "My products",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AgriTextPrimary
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Recent orders ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent orders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    if (recentOrders.isNotEmpty()) {
                        Text(
                            "See all",
                            style = MaterialTheme.typography.bodySmall,
                            color = AgriGreenDark,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable(onClick = onSeeAllOrdersClick)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                if (recentOrders.isEmpty()) {
                    EmptyState(
                        emoji = "📦",
                        title = "No orders yet",
                        hint = "They'll show up here as buyers purchase your products."
                    )
                } else {
                    recentOrders.forEach { order ->
                        OrderRow(order = order, onClick = { onOrderClick(order) })
                        Spacer(Modifier.height(10.dp))
                    }
                }

                // ── Buyer requests ──
                if (recentRequests.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Buyer requests",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AgriTextPrimary
                        )
                        Text(
                            "See all",
                            style = MaterialTheme.typography.bodySmall,
                            color = AgriGreenDark,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable(onClick = onSeeAllRequestsClick)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    recentRequests.forEach { req ->
                        RequestRow(request = req, onClick = onSeeAllRequestsClick)
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            errorMessage?.let { msg ->
                Spacer(Modifier.height(16.dp))
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

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Hero card — the one number that matters right now.
// ─────────────────────────────────────────────────────────────
@Composable
private fun HeroCard(
    ordersToPack: Int,
    ordersThisWeek: Int
) {
    val hasPackWork = ordersToPack > 0
    val heroValue = if (hasPackWork) ordersToPack.toString() else ordersThisWeek.toString()
    val heroLabel = if (hasPackWork) {
        if (ordersToPack == 1) "order to pack" else "orders to pack"
    } else {
        if (ordersThisWeek == 1) "order this week" else "orders this week"
    }
    val heroHint = if (hasPackWork) {
        "Pack them and drop at the hub so the driver can collect."
    } else {
        "You're all caught up. New orders will appear here."
    }

    val accent = if (hasPackWork) AgriRedAlert else AgriGreen

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AgriCardBg)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = heroValue,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = heroLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AgriTextPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = heroHint,
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary
            )
        }
        Spacer(Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (hasPackWork) "📦" else "🌾",
                fontSize = 26.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Attention row — a tappable banner for anything that needs
// the farmer to act.
// ─────────────────────────────────────────────────────────────
@Composable
private fun AttentionRow(
    emoji: String,
    title: String,
    body: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AgriCardBg)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(badgeColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 20.sp)
        }
        Spacer(Modifier.width(14.dp))
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
        Text(
            "›",
            fontSize = 22.sp,
            color = AgriTextTertiary,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Compact stat — 1/3 width, big number, small label.
// ─────────────────────────────────────────────────────────────
@Composable
private fun CompactStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AgriCardBg)
            .padding(vertical = 16.dp, horizontal = 12.dp)
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AgriTextPrimary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = AgriTextSecondary
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Shared components — kept internal so other farmer screens
// (FarmerOrdersScreen, etc.) can keep importing them.
// ─────────────────────────────────────────────────────────────

@Composable
private fun RequestRow(request: ProductRequestDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AgriCardBg)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AgriMintLight),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🌱", fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = request.productName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AgriTextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "From ${request.buyerName}",
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary
            )
        }
        Text(
            "›",
            fontSize = 22.sp,
            color = AgriTextTertiary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyState(
    emoji: String,
    title: String,
    hint: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AgriCardBg)
            .padding(vertical = 28.dp, horizontal = 20.dp),
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

@Composable
internal fun OrderRow(order: FarmerOrder, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AgriCardBg)
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AgriMintLight),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📦", fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Order ${order.displayId} · ${order.area}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AgriTextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${order.buyerName} · ${order.itemCount} item${if (order.itemCount == 1) "" else "s"} · R${"%.2f".format(order.total)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        StatusBadge(order.trackingStatus, order.statusLabel)
    }
}

@Composable
internal fun StatusBadge(trackingStatus: String, label: String) {
    val color = when (trackingStatus) {
        "delivered" -> AgriGreen
        "shipped" -> AgriGreen
        "processing" -> AgriGreen
        else -> AgriYellow
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = AgriTextPrimary
        )
    }
}