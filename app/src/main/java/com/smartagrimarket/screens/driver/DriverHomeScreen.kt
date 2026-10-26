package com.smartagrimarket.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.DriverRepository
import com.smartagrimarket.model.network.TokenStore
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DriverHomeScreen(
    onBrowseJobs: () -> Unit,
    onOpenVerification: () -> Unit,
    onOpenWallet: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var isAvailable by remember { mutableStateOf(false) }
    var activeCount by remember { mutableStateOf(0) }
    var completedCount by remember { mutableStateOf(0) }
    var todayCount by remember { mutableStateOf(0) }
    var maxBatch by remember { mutableStateOf(10) }
    var batchState by remember { mutableStateOf("idle") }
    var batchCount by remember { mutableStateOf(0) }
    var nextStopName by remember { mutableStateOf<String?>(null) }
    var nextStopKm by remember { mutableStateOf<Double?>(null) }
    var verificationStatus by remember { mutableStateOf("not_submitted") }
    var hasFridge by remember { mutableStateOf(false) }
    var isTogglingFridge by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isToggling by remember { mutableStateOf(false) }
    var isStartingBatch by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val firstName = TokenStore.currentUser?.fullName?.substringBefore(" ") ?: "there"

    fun load() {
        scope.launch {
            isLoading = true
            errorMessage = null
            when (val stats = DriverRepository.fetchStats()) {
                is ApiResult.Success -> {
                    activeCount = stats.data.activeCount
                    completedCount = stats.data.completedCount
                    todayCount = stats.data.todayCount
                    maxBatch = stats.data.maxBatch
                    verificationStatus = stats.data.verificationStatus
                    batchState = stats.data.batchState
                    hasFridge = stats.data.hasFridge
                }
                is ApiResult.Error -> errorMessage = stats.message
            }
            when (val jobs = DriverRepository.fetchJobs()) {
                is ApiResult.Success -> {
                    isAvailable = jobs.data.isAvailable
                    verificationStatus = jobs.data.verificationStatus
                    batchState = jobs.data.batchState
                    batchCount = jobs.data.batch.count
                    nextStopName = jobs.data.batch.nextStop?.buyerName
                    nextStopKm = jobs.data.batch.nextStop?.distanceFromHubKm
                    hasFridge = jobs.data.hasFridge
                }
                is ApiResult.Error -> { }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    val isVerified = verificationStatus == "approved"
    val hasBatch = batchCount > 0

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

            // ── Greeting ──
            Text(
                "Hi, $firstName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AgriTextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                if (isVerified) "Ready to deliver?" else "Finish setup to start delivering",
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary
            )

            Spacer(Modifier.height(20.dp))

            // ── Verification banner ──
            when (verificationStatus) {
                "not_submitted" -> DriverBanner(
                    emoji = "🛡️",
                    title = "Complete verification",
                    body = "Upload your licence, permit and vehicle photos.",
                    bg = AgriMintLight,
                    fg = AgriGreenDark,
                    ctaLabel = "Start now →",
                    onCtaClick = onOpenVerification
                )
                "pending" -> DriverBanner(
                    emoji = "⏳",
                    title = "Verification under review",
                    body = "We'll notify you when it's approved.",
                    bg = AgriMintLight,
                    fg = AgriGreenDark,
                    ctaLabel = "View submission →",
                    onCtaClick = onOpenVerification
                )
                "rejected" -> DriverBanner(
                    emoji = "⚠️",
                    title = "Verification rejected",
                    body = "Review your documents and resubmit.",
                    bg = AgriRedAlert.copy(alpha = 0.10f),
                    fg = AgriRedAlert,
                    ctaLabel = "Resubmit →",
                    onCtaClick = onOpenVerification
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Availability card ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAvailable && isVerified) AgriGreen else AgriCardBg
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            when {
                                !isVerified -> "Off duty"
                                isAvailable -> "Available"
                                else -> "Off duty"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isAvailable && isVerified) Color.White else AgriTextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            when {
                                !isVerified -> "Verify your account to go online."
                                batchState != "idle" -> "You're on a batch — finish it before going offline."
                                isAvailable -> "Dispatch can assign you jobs."
                                else -> "Turn on to receive assignments."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isAvailable && isVerified) Color.White.copy(alpha = 0.85f)
                            else AgriTextSecondary
                        )
                    }
                    Switch(
                        checked = isAvailable && isVerified,
                        enabled = !isToggling && isVerified && batchState == "idle",
                        onCheckedChange = { newValue ->
                            isToggling = true
                            scope.launch {
                                when (val r = DriverRepository.setAvailability(newValue)) {
                                    is ApiResult.Success -> isAvailable = r.data.isAvailable
                                    is ApiResult.Error -> errorMessage = r.message
                                }
                                isToggling = false
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AgriGreenDark,
                            checkedTrackColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── CANOPY FRIDGE CARD ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AgriCardBg),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (hasFridge) AgriGreen.copy(alpha = 0.15f)
                                else AgriMintLight
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🧊", fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Canopy fridge",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AgriTextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            if (hasFridge)
                                "On — meat & poultry deliveries unlocked"
                            else
                                "Turn on to unlock meat & poultry deliveries",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (hasFridge) AgriGreenDark else AgriTextSecondary
                        )
                    }
                    Switch(
                        checked = hasFridge,
                        enabled = !isTogglingFridge && isVerified,
                        onCheckedChange = { newValue ->
                            isTogglingFridge = true
                            errorMessage = null
                            scope.launch {
                                when (val r = DriverRepository.updateVehicleDetails(newValue)) {
                                    is ApiResult.Success -> hasFridge = r.data
                                    is ApiResult.Error -> errorMessage = r.message
                                }
                                isTogglingFridge = false
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AgriGreen
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Batch card (4 states) ──
            when {
                // State 1: idle, no jobs
                batchState == "idle" && !hasBatch -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AgriCardBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("—", fontSize = 20.sp)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "No jobs assigned yet",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = AgriTextPrimary
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "Dispatch will assign a batch when ready.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AgriTextSecondary
                                )
                            }
                        }
                    }
                }

                // State 2: idle with jobs queued — Start batch
                batchState == "idle" && hasBatch -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AgriMintLight),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(AgriGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📦", fontSize = 20.sp)
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "$batchCount deliver${if (batchCount == 1) "y" else "ies"} ready",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = AgriGreenDark
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "Start the batch to head to the hub.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AgriGreenDark.copy(alpha = 0.75f)
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    isStartingBatch = true
                                    errorMessage = null
                                    scope.launch {
                                        when (val r = DriverRepository.startBatch()) {
                                            is ApiResult.Success -> {
                                                batchState = r.data.batchState
                                                onBrowseJobs()
                                            }
                                            is ApiResult.Error -> errorMessage = r.message
                                        }
                                        isStartingBatch = false
                                    }
                                },
                                enabled = !isStartingBatch,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AgriGreen,
                                    contentColor = Color.White
                                )
                            ) {
                                if (isStartingBatch) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Starting…", fontWeight = FontWeight.Bold)
                                } else {
                                    Text("🚚  Start batch", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // State 3: at_hub — collect
                batchState == "at_hub" -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onBrowseJobs),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AgriOrange.copy(alpha = 0.12f)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AgriOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🏬", fontSize = 20.sp)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Head to the hub",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = AgriOrange
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "Tap Collect once you've picked up all $batchCount orders.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AgriOrange.copy(alpha = 0.85f)
                                )
                            }
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = AgriOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // State 4: collected — delivering
                batchState == "collected" -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onBrowseJobs),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AgriGreen),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🛵", fontSize = 20.sp)
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "$batchCount stop${if (batchCount == 1) "" else "s"} left",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    if (nextStopName != null) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            "Next: $nextStopName" +
                                                    (nextStopKm?.let { " · ${"%.1f".format(it)} km" } ?: ""),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Section title ──
            Text(
                "Today's activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AgriTextPrimary
            )

            Spacer(Modifier.height(12.dp))

            // ── Stats grid ──
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DriverStatCard(
                    value = activeCount.toString(),
                    label = "Active",
                    accentColor = AgriGreen,
                    modifier = Modifier.weight(1f)
                )
                DriverStatCard(
                    value = todayCount.toString(),
                    label = "Today",
                    accentColor = AgriOrange,
                    modifier = Modifier.weight(1f)
                )
                DriverStatCard(
                    value = completedCount.toString(),
                    label = "Total",
                    accentColor = AgriGreenDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Wallet card ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenWallet),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AgriCardBg),
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
                        Text("💰", fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "My Wallet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = AgriTextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "View balance and withdraw earnings",
                            style = MaterialTheme.typography.bodySmall,
                            color = AgriTextSecondary
                        )
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = AgriTextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (isLoading) {
                Spacer(Modifier.height(20.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AgriGreen)
                }
            }

            errorMessage?.let { msg ->
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AgriRedAlert.copy(alpha = 0.12f))
                        .padding(12.dp)
                ) {
                    Text(msg, color = AgriRedAlert, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DriverBanner(
    emoji: String,
    title: String,
    body: String,
    bg: Color,
    fg: Color,
    ctaLabel: String,
    onCtaClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable(onClick = onCtaClick)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = fg, fontSize = 15.sp)
            Spacer(Modifier.height(2.dp))
            Text(body, fontSize = 12.sp, color = fg.copy(alpha = 0.8f))
            Spacer(Modifier.height(8.dp))
            Text(
                ctaLabel,
                color = fg,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DriverStatCard(
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = AgriTextSecondary
            )
        }
    }
}