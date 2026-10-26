package com.smartagrimarket.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.TokenStore
import com.smartagrimarket.model.network.WalletRepository
import com.smartagrimarket.model.network.WithdrawalDto
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun FarmerWalletScreen(
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    var balance by remember { mutableStateOf(0.0) }
    var pending by remember { mutableStateOf(0.0) }
    var pendingOrderCount by remember { mutableStateOf(0) }
    var withdrawals by remember { mutableStateOf<List<WithdrawalDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var isWithdrawing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadWallet(showErrors: Boolean = true) {
        scope.launch {
            isLoading = true
            if (showErrors) errorMessage = null
            val token = TokenStore.token
            if (token == null) {
                if (showErrors) errorMessage = "Please sign in again."
                isLoading = false
                return@launch
            }
            when (val result = WalletRepository.getWallet("Bearer $token")) {
                is ApiResult.Success -> {
                    balance = result.data.balance
                    pending = result.data.pending
                    pendingOrderCount = result.data.pendingOrderCount
                    withdrawals = result.data.withdrawals
                }
                is ApiResult.Error -> {
                    if (showErrors) errorMessage = result.message
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadWallet(showErrors = false) }

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

    Scaffold(
        containerColor = AgriCreamBg,
        bottomBar = bottomBar
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // ── Header ──
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
                    "My Wallet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Balance card ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AgriYellow)
                    .padding(20.dp)
            ) {
                Text(
                    "Available to withdraw",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AgriTextPrimary.copy(alpha = 0.75f)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "R ${String.format(Locale.US, "%.2f", balance)}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { showWithdrawDialog = true },
                    enabled = balance > 0 && !isWithdrawing,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriTextPrimary,
                        contentColor = AgriYellow
                    )
                ) {
                    Text("Withdraw funds", fontWeight = FontWeight.Bold)
                }
            }

            // ── Pending card ──
            if (pending > 0) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF4E0))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFBF0CD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⏳", fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Pending",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF8A6100)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "R ${String.format(Locale.US, "%.2f", pending)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6E4D00)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (pendingOrderCount == 1)
                                "From 1 order still in transit"
                            else
                                "From $pendingOrderCount orders still in transit",
                            fontSize = 11.sp,
                            color = Color(0xFF8A6100)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "This amount becomes available once the buyer receives their order.",
                    fontSize = 11.sp,
                    color = AgriTextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

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
                Spacer(Modifier.height(12.dp))
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
                Spacer(Modifier.height(12.dp))
            }

            Text(
                "Withdrawal history",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AgriTextPrimary
            )
            Spacer(Modifier.height(10.dp))

            if (isLoading && withdrawals.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = AgriTextPrimary) }
            } else if (withdrawals.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AgriCardBg)
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AgriMintLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💸", fontSize = 28.sp)
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "No withdrawals yet",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Your withdrawal requests will appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AgriTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(withdrawals) { w ->
                        WithdrawalRow(w)
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }

    if (showWithdrawDialog) {
        WithdrawBottomSheet(
            availableBalance = balance,
            pendingBalance = pending,
            isSubmitting = isWithdrawing,
            onDismiss = {
                if (!isWithdrawing) showWithdrawDialog = false
            },
            onSubmit = { amount, method, details ->
                if (isWithdrawing) return@WithdrawBottomSheet
                isWithdrawing = true
                scope.launch {
                    val token = TokenStore.token
                    if (token == null) {
                        isWithdrawing = false
                        errorMessage = "Please sign in again."
                        return@launch
                    }

                    val result = WalletRepository.requestWithdrawal(
                        bearerToken = "Bearer $token",
                        amount = amount,
                        payoutMethod = method,
                        details = details
                    )

                    when (result) {
                        is ApiResult.Success -> {
                            showWithdrawDialog = false
                            successMessage = "Withdrawal requested! Admin will process it shortly."
                            loadWallet(showErrors = true)
                        }
                        is ApiResult.Error -> {
                            showWithdrawDialog = false
                            errorMessage = result.message
                            loadWallet(showErrors = true)
                        }
                    }
                    isWithdrawing = false
                }
            }
        )
    }
}

@Composable
private fun WithdrawalRow(w: WithdrawalDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AgriCardBg)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "R ${String.format(Locale.US, "%.2f", w.amount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AgriTextPrimary
            )
            val (pillFg, pillBg, label) = when (w.status) {
                "approved" -> Triple(AgriGreenDark, AgriGreen.copy(alpha = 0.15f), "Approved")
                "rejected" -> Triple(AgriRedAlert, AgriRedAlert.copy(alpha = 0.15f), "Rejected")
                else -> Triple(Color(0xFF8A6100), Color(0xFFFBF0CD), "Pending")
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(pillBg)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    label,
                    color = pillFg,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Platform fee (15%): R ${String.format(Locale.US, "%.2f", w.platformFee)}",
            fontSize = 12.sp,
            color = AgriTextSecondary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "You receive: R ${String.format(Locale.US, "%.2f", w.netAmount)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AgriTextPrimary
        )
        if (w.status == "rejected" && !w.rejectionReason.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Reason: ${w.rejectionReason}",
                fontSize = 11.sp,
                color = AgriRedAlert
            )
        }
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
    val platformFee = amount * 0.15
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
                        Text("Platform fee (15%)", fontSize = 12.sp, color = AgriTextSecondary)
                        Text(
                            "- R ${String.format(Locale.US, "%.2f", platformFee)}",
                            fontSize = 12.sp,
                            color = AgriRedAlert,
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

            // Show validation error if user tries to submit incomplete form
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
                    // Validate and show error inside the sheet if invalid
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
                enabled = !isSubmitting, // Button is always clickable unless submitting
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