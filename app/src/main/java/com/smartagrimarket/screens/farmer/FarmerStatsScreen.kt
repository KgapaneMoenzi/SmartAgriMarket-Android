package com.smartagrimarket.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.DailyOrderCount
import com.smartagrimarket.model.FarmerStatsExtended
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.FarmerRepository
import com.smartagrimarket.ui.theme.*

@Composable
fun FarmerStatsScreen(
    bottomBar: @Composable () -> Unit = {}
) {
    var stats by remember { mutableStateOf<FarmerStatsExtended?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(reloadKey) {
        isLoading = true
        errorMessage = null
        when (val result = FarmerRepository.fetchExtendedStats()) {
            is ApiResult.Success -> stats = result.data
            is ApiResult.Error -> errorMessage = result.message
        }
        isLoading = false
    }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Stats",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Your farm's performance at a glance",
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

            Spacer(Modifier.height(20.dp))

            when {
                isLoading && stats == null -> {
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

                stats != null -> {
                    val s = stats!!

                    // ── Revenue hero ──
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(AgriYellow)
                            .padding(20.dp)
                    ) {
                        Text(
                            "Revenue this week",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AgriTextPrimary.copy(alpha = 0.75f)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "R${"%.2f".format(s.revenueThisWeek)}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = AgriTextPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "R${"%.2f".format(s.revenueTotal)} all-time",
                            style = MaterialTheme.typography.bodySmall,
                            color = AgriTextPrimary.copy(alpha = 0.65f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Metric grid ──
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SmallMetric(
                            value = s.ordersThisWeek.toString(),
                            label = "Orders (week)",
                            modifier = Modifier.weight(1f)
                        )
                        SmallMetric(
                            value = s.ordersTotal.toString(),
                            label = "Orders (total)",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SmallMetric(
                            value = "R${"%.2f".format(s.averageOrderValue)}",
                            label = "Avg. order value",
                            modifier = Modifier.weight(1f)
                        )
                        SmallMetric(
                            value = s.ordersToPack.toString(),
                            label = "To pack",
                            valueColor = if (s.ordersToPack > 0) AgriRedAlert else AgriTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SmallMetric(
                            value = "R${"%.2f".format(s.deliveryFeesThisWeek)}",
                            label = "Delivery fees (week)",
                            modifier = Modifier.weight(1f)
                        )
                        SmallMetric(
                            value = s.openRequestsCount.toString(),
                            label = "Open buyer requests",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // ── Buyer demand ──
                    if (s.topOpenRequestName != null && s.topOpenRequestCount > 0) {
                        Spacer(Modifier.height(20.dp))
                        BuyerDemandCard(
                            product = s.topOpenRequestName,
                            count = s.topOpenRequestCount
                        )
                    }

                    // ── Top seller ──
                    if (s.topSellingProduct != null) {
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "Top seller",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AgriTextPrimary
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(AgriCardBg)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AgriMintLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🏆", fontSize = 20.sp)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    s.topSellingProduct,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriTextPrimary
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "Best performing product",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AgriTextSecondary
                                )
                            }
                        }
                    }

                    // ── Last 7 days ──
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Last 7 days",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    Spacer(Modifier.height(10.dp))
                    DailyChart(s.dailyOrderCounts)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BuyerDemandCard(product: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AgriYellow.copy(alpha = 0.22f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AgriYellow),
            contentAlignment = Alignment.Center
        ) {
            Text("📢", fontSize = 20.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (count == 1) "1 buyer is asking for $product"
                else "$count buyers are asking for $product",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = AgriTextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "See what to plant in the Weather tab",
                style = MaterialTheme.typography.labelSmall,
                color = AgriTextPrimary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SmallMetric(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = AgriTextPrimary
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AgriCardBg)
            .padding(vertical = 16.dp, horizontal = 14.dp)
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = AgriTextSecondary
        )
    }
}

@Composable
private fun DailyChart(counts: List<DailyOrderCount>) {
    val max = counts.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AgriCardBg)
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        counts.forEach { day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    day.count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height((64 * (day.count.toFloat() / max)).dp.coerceAtLeast(6.dp))
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (day.count == 0) AgriTextTertiary.copy(alpha = 0.3f) else AgriYellow)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    day.dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AgriTextPrimary
                )
                Text(
                    day.dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = AgriTextSecondary,
                    fontSize = 9.sp
                )
            }
        }
    }
}