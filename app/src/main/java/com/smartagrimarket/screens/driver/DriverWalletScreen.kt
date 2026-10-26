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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

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
    var isWithdrawing by remember { mutableStateOf(false) }

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

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(5000)
            errorMessage = null
        }
    }
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            delay(5000)
            successMessage = null
        }
    }

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
                            "R ${String.format(Locale.US, "%.2f", balance)}",
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

                // ── Banners ──
                successMessage?.let { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriGreen.copy(alpha = 0.15f))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✅", fontSize = 16.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            msg,
                            color = AgriGreenDark,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                errorMessage?.let { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriRedAlert.copy(alpha = 0.12f))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 16.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            msg,
                            color = AgriRedAlert,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

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
                                "R ${String.format(Locale.US, "%.2f", pending)}",
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

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showWithdrawDialog) {
        WithdrawBottomSheet(
            availableBalance = wallet?.balance ?: 0.0,
            pendingBalance = wallet?.pending ?: 0.0,
            isSubmitting = isWithdrawing,
            onDismiss = {
                if (!isWithdrawing) showWithdrawDialog = false
            },
            onSubmit = { amount, method, details ->
                if (isWithdrawing) return@WithdrawBottomSheet
                isWithdrawing = true
                scope.launch {
                    val result = DriverRepository.requestWithdrawal(
                        amount = amount,
                        payoutMethod = method,
                        details = details
                    )

                    when (result) {
                        is ApiResult.Success -> {
                            showWithdrawDialog = false
                            successMessage = "Withdrawal requested! Admin will process it shortly."
                            refresh()
                        }
                        is ApiResult.Error -> {
                            showWithdrawDialog = false
                            errorMessage = result.message
                            refresh()
                        }
                    }
                    isWithdrawing = false
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
                    "R ${String.format(Locale.US, "%.2f", w.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AgriTextPrimary
                )
                Spacer(Modifier.height(3.dp))

                // Show payout method in the history
                val methodLabel = when (w.payoutMethod) {
                    "BANK" -> "Bank Transfer (${w.bankName ?: "Unknown"})"
                    "EWALLET" -> "E-Wallet"
                    else -> "Unknown Method"
                }
                Text(
                    methodLabel,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WithdrawBottomSheet(
    availableBalance: Double,
    pendingBalance: Double,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (amount: Double, method: String, details: Map<String, String>) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("EWALLET") }

    var walletNumber by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }

    var validationError by remember { mutableStateOf<String?>(null) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    // Drivers don't pay the 15% fee
    val platformFee = 0.0
    val netAmount = amount - platformFee

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AgriCardBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = AgriBorderLight) },
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Withdraw funds", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AgriTextPrimary)

            Spacer(Modifier.height(4.dp))
            Text(
                "Available: R ${String.format(Locale.US, "%.2f", availableBalance)}",
                fontSize = 13.sp,
                color = AgriTextSecondary
            )
            if (pendingBalance > 0) {
                Text(
                    "Pending (locked): R ${String.format(Locale.US, "%.2f", pendingBalance)}",
                    fontSize = 12.sp,
                    color = Color(0xFF8A6100)
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        amountText = newValue
                        validationError = null
                    }
                },
                label = { Text("Amount (R)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                enabled = !isSubmitting,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (amount > availableBalance) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Amount exceeds your available balance.",
                    color = AgriRedAlert,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(20.dp))
            Text("Payout Method", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AgriTextPrimary)
            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { method = "EWALLET" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (method == "EWALLET") AgriGreen else AgriMintLight,
                        contentColor = if (method == "EWALLET") Color.White else AgriGreenDark
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("E-Wallet") }

                Button(
                    onClick = { method = "BANK" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (method == "BANK") AgriGreen else AgriMintLight,
                        contentColor = if (method == "BANK") Color.White else AgriGreenDark
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Bank Transfer") }
            }

            Spacer(Modifier.height(16.dp))

            if (method == "EWALLET") {
                OutlinedTextField(
                    value = walletNumber,
                    onValueChange = { walletNumber = it; validationError = null },
                    label = { Text("E-Wallet Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it; validationError = null },
                    label = { Text("Bank Name (e.g. FNB, Capitec)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it; validationError = null },
                    label = { Text("Account Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it; validationError = null },
                    label = { Text("Account Holder Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (amount > 0) {
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AgriCreamBg)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Platform fee", fontSize = 12.sp, color = AgriTextSecondary)
                        Text(
                            "R 0.00 (Drivers pay no fee)",
                            fontSize = 12.sp,
                            color = AgriGreenDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "You will receive",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AgriTextPrimary
                        )
                        Text(
                            "R ${String.format(Locale.US, "%.2f", netAmount)}",
                            fontSize = 14.sp,
                            color = AgriGreenDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            validationError?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    it,
                    color = AgriRedAlert,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (amount <= 0) {
                        validationError = "Please enter an amount greater than 0."
                        return@Button
                    }
                    if (amount > availableBalance) {
                        validationError = "Amount exceeds your available balance."
                        return@Button
                    }
                    if (method == "EWALLET" && walletNumber.isBlank()) {
                        validationError = "Please enter your E-Wallet phone number."
                        return@Button
                    }
                    if (method == "BANK" && (bankName.isBlank() || accountNumber.isBlank() || accountName.isBlank())) {
                        validationError = "Please fill in all bank details."
                        return@Button
                    }

                    val details = if (method == "EWALLET") {
                        mapOf("walletNumber" to walletNumber)
                    } else {
                        mapOf("bankName" to bankName, "accountNumber" to accountNumber, "accountName" to accountName)
                    }
                    onSubmit(amount, method, details)
                },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Confirm Withdrawal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

private fun formatTimestamp(iso: String): String {
    return try {
        val cleaned = iso.substringBefore(".").replace("T", " ")
        if (cleaned.length >= 16) cleaned.substring(0, 16) else cleaned
    } catch (_: Exception) {
        iso
    }
}