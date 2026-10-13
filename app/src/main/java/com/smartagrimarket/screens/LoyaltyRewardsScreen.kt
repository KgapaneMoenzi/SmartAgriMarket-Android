package com.smartagrimarket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.CouponDto
import com.smartagrimarket.model.network.LoyaltyMeResponse
import com.smartagrimarket.model.network.LoyaltyRepository
import com.smartagrimarket.model.network.PointsHistoryDto
import com.smartagrimarket.model.network.RedeemOptionDto
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoyaltyRewardsScreen(
    onBack: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf<LoyaltyMeResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var isRedeeming by remember { mutableStateOf(false) }

    fun load() {
        scope.launch {
            isLoading = true
            errorMessage = null
            when (val result = LoyaltyRepository.fetchMe()) {
                is ApiResult.Success -> data = result.data
                is ApiResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(containerColor = AgriCreamBg, bottomBar = bottomBar) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onBack)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = AgriTextSecondary)
                Spacer(Modifier.width(4.dp))
                Text("AgriMarket Rewards", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AgriTextPrimary)
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriRedAlert.copy(alpha = 0.12f))
                            .padding(14.dp)
                    ) {
                        Text(errorMessage!!, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
                    }
                }
                data != null -> {
                    val d = data!!

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AgriYellow)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Your balance", style = MaterialTheme.typography.bodyMedium, color = AgriTextPrimary.copy(alpha = 0.8f))
                        Spacer(Modifier.height(4.dp))
                        Text("${d.balance} pts", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "≈ R${"%.2f".format(d.randsEquivalent)} in rewards",
                            style = MaterialTheme.typography.bodySmall,
                            color = AgriTextPrimary.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriCardBg)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(d.tierLabel, fontWeight = FontWeight.Bold)
                            if (d.nextTierLabel != null && d.pointsToNextTier > 0) {
                                Text("${d.pointsToNextTier} pts to ${d.nextTierLabel}",
                                    style = MaterialTheme.typography.bodySmall, color = AgriTextSecondary)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AgriCreamBg)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(d.tierProgressFraction.coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AgriYellow)
                            )
                        }
                    }

                    message?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = AgriGreen, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("Redeem points", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriCardBg)
                            .padding(vertical = 4.dp)
                    ) {
                        d.redeemOptions.forEachIndexed { i, option ->
                            RedeemRow(
                                option = option,
                                canRedeem = d.balance >= option.pointsCost && !isRedeeming,
                                onClick = {
                                    scope.launch {
                                        isRedeeming = true
                                        message = null
                                        when (val r = LoyaltyRepository.redeem(option.kind)) {
                                            is ApiResult.Success -> {
                                                message = "Coupon created: ${r.data.coupon.code}"
                                                load() // refresh
                                            }
                                            is ApiResult.Error -> message = r.message
                                        }
                                        isRedeeming = false
                                    }
                                }
                            )
                            if (i != d.redeemOptions.lastIndex) Divider(color = AgriCreamBg, thickness = 1.dp)
                        }
                    }

                    if (d.coupons.isNotEmpty()) {
                        Spacer(Modifier.height(20.dp))
                        Text("Your coupons", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AgriCardBg)
                                .padding(vertical = 4.dp)
                        ) {
                            d.coupons.forEachIndexed { i, c ->
                                CouponRow(c)
                                if (i != d.coupons.lastIndex) Divider(color = AgriCreamBg, thickness = 1.dp)
                            }
                        }
                    }

                    if (d.history.isNotEmpty()) {
                        Spacer(Modifier.height(20.dp))
                        Text("Recent activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AgriCardBg)
                                .padding(vertical = 4.dp)
                        ) {
                            d.history.take(10).forEachIndexed { i, h ->
                                HistoryRow(h)
                                if (i != d.history.take(10).lastIndex) Divider(color = AgriCreamBg, thickness = 1.dp)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("How you earn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriCardBg)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "🛒" to "1 point per R1 spent",
                            "⭐" to "20 points for leaving a review",
                            "👥" to "100 points per successful referral"
                        ).forEach { (emoji, label) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(emoji)
                                Spacer(Modifier.width(8.dp))
                                Text(label, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RedeemRow(option: RedeemOptionDto, canRedeem: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canRedeem, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            option.label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (canRedeem) AgriTextPrimary else AgriTextSecondary
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AgriYellow.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("${option.pointsCost} pts", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CouponRow(c: CouponDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(c.code, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(
                if (c.amountOff > 0) "R${"%.2f".format(c.amountOff)} off" else "Free delivery",
                style = MaterialTheme.typography.labelSmall,
                color = AgriTextSecondary
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AgriGreen.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("Ready", style = MaterialTheme.typography.labelSmall, color = AgriGreen)
        }
    }
}

@Composable
private fun HistoryRow(h: PointsHistoryDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(humanReason(h.reason), style = MaterialTheme.typography.bodySmall)
            Text(h.createdAt.take(10), style = MaterialTheme.typography.labelSmall, color = AgriTextSecondary)
        }
        Text(
            (if (h.delta >= 0) "+" else "") + "${h.delta} pts",
            style = MaterialTheme.typography.bodySmall,
            color = if (h.delta >= 0) AgriGreen else AgriRedAlert,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun humanReason(r: String): String = when (r) {
    "order_confirmed" -> "Order placed"
    "review" -> "Left a review"
    "referral" -> "Referral reward"
    "redeem" -> "Redeemed a coupon"
    else -> r
}