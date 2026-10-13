package com.smartagrimarket.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.DriverRepository
import com.smartagrimarket.model.network.DriverWalletDto
import com.smartagrimarket.model.network.DriverWithdrawalDto
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DriverWalletScreen(
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var wallet by remember { mutableStateOf<DriverWalletDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    suspend fun refresh() {
        loading = true
        errorMessage = null
        when (val result = DriverRepository.getWallet()) {
            is ApiResult.Success -> wallet = result.data
            is ApiResult.Error -> errorMessage = result.message
        }
        loading = false
    }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                    "My Wallet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                val balance = wallet?.balance ?: 0.0

                // ── Balance hero ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AgriGreen),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Available balance",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.20f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💰", fontSize = 20.sp)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "R ${String.format("%.2f", balance)}",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = { showWithdrawDialog = true },
                            enabled = balance > 0 && !loading,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = AgriGreenDark,
                                disabledContainerColor = Color.White.copy(alpha = 0.40f),
                                disabledContentColor = AgriGreenDark.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                if (balance > 0) "Withdraw funds" else "No funds yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── In-flight earnings ──
                val pending = wallet?.pending ?: 0.0
                val pendingCount = wallet?.pendingJobCount ?: 0
                if (pendingCount > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(AgriMintLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⏳", fontSize = 20.sp)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "In-flight earnings",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AgriTextPrimary
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    if (pendingCount == 1)
                                        "1 active delivery"
                                    else
                                        "$pendingCount active deliveries",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AgriTextSecondary
                                )
                            }
                            Text(
                                "R ${String.format("%.2f", pending)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = AgriGreenDark
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // ── History ──
                Text(
                    "Withdrawal history",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                Spacer(Modifier.height(12.dp))

                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AgriGreen)
                    }
                } else if (wallet?.withdrawals.isNullOrEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(AgriMintLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🧾", fontSize = 28.sp)
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No withdrawals yet",
                                fontWeight = FontWeight.Bold,
                                color = AgriTextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Your withdrawal requests will show up here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AgriTextSecondary
                            )
                        }
                    }
                } else {
                    wallet?.withdrawals?.forEachIndexed { index, w ->
                        WithdrawalRow(w)
                        if (index != wallet!!.withdrawals.lastIndex) Spacer(Modifier.height(10.dp))
                    }
                }

                successMessage?.let {
                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriMintLight)
                            .padding(14.dp)
                    ) {
                        Text(
                            it,
                            color = AgriGreenDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                errorMessage?.let {
                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriRedAlert.copy(alpha = 0.12f))
                            .padding(14.dp)
                    ) {
                        Text(it, color = AgriRedAlert, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showWithdrawDialog) {
        WithdrawDialog(
            balance = wallet?.balance ?: 0.0,
            onDismiss = { showWithdrawDialog = false },
            onSubmit = { amount ->
                showWithdrawDialog = false
                scope.launch {
                    errorMessage = null
                    successMessage = null
                    when (val result = DriverRepository.requestWithdrawal(amount)) {
                        is ApiResult.Success -> {
                            successMessage =
                                "Withdrawal of R${String.format("%.2f", result.data.withdrawal.amount)} requested. " +
                                        "R${String.format("%.2f", result.data.withdrawal.netAmount)} will be paid out " +
                                        "(after 15% platform fee)."
                            refresh()
                        }
                        is ApiResult.Error -> errorMessage = result.message
                    }
                }
            }
        )
    }
}

@Composable
private fun WithdrawalRow(w: DriverWithdrawalDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "R ${String.format("%.2f", w.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AgriTextPrimary
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "Net R${String.format("%.2f", w.netAmount)} · Fee R${String.format("%.2f", w.platformFee)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary
                )
                w.createdAt.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        formatTimestamp(it),
                        style = MaterialTheme.typography.labelSmall,
                        color = AgriTextTertiary
                    )
                }
                w.rejectionReason?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Reason: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = AgriRedAlert
                    )
                }
            }
            DriverStatusBadge(w.status)
        }
    }
}

@Composable
private fun DriverStatusBadge(status: String) {
    val (bg, fg, label) = when (status.lowercase()) {
        "approved" -> Triple(AgriMintLight, AgriGreenDark, "✓ Approved")
        "rejected" -> Triple(AgriRedAlert.copy(alpha = 0.15f), AgriRedAlert, "✕ Rejected")
        else -> Triple(AgriOrange.copy(alpha = 0.15f), AgriOrange, "⏳ Pending")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun WithdrawDialog(
    balance: Double,
    onDismiss: () -> Unit,
    onSubmit: (Double) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val amount = text.toDoubleOrNull() ?: 0.0
    val isValid = amount > 0 && amount <= balance

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AgriCardBg,
        title = { Text("Withdraw funds", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Available: R ${String.format("%.2f", balance)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { input ->
                        text = input.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("Amount (R)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = AgriMintSoft,
                        focusedContainerColor = AgriMintSoft,
                        focusedBorderColor = AgriGreen,
                        cursorColor = AgriGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (text.isNotBlank() && !isValid) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (amount > balance) "Amount exceeds your balance."
                        else "Enter a valid amount.",
                        color = AgriRedAlert,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "A 15% platform fee is deducted from your payout.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(amount) },
                enabled = isValid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriGreen,
                    contentColor = Color.White
                )
            ) {
                Text("Request", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AgriTextSecondary)
            }
        }
    )
}

private fun formatTimestamp(iso: String): String {
    return try {
        val cleaned = iso.substringBefore(".").replace("T", " ")
        if (cleaned.length >= 16) cleaned.substring(0, 16) else cleaned
    } catch (_: Exception) {
        iso
    }
}