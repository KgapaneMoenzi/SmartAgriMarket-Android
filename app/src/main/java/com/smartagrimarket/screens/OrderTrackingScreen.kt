package com.smartagrimarket.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.network.DriverLocationResponse
import com.smartagrimarket.model.network.OrderDto
import com.smartagrimarket.model.network.RetrofitClient
import com.smartagrimarket.model.network.RouteResponse
import com.smartagrimarket.model.network.TokenStore
import com.smartagrimarket.screens.driver.LocationTracker
import com.smartagrimarket.ui.theme.*
import com.smartagrimarket.util.TrackingUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private data class TimelineStage(
    val label: String,
    val detail: String,
    val state: StageState
)

private enum class StageState { DONE, CURRENT, PENDING }

@Composable
fun OrderTrackingScreen(
    order: OrderDto,
    onBack: () -> Unit = {},
    onNeedHelp: (OrderDto) -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val stages = buildStages(order)

    var driverLocation by remember { mutableStateOf<DriverLocationResponse?>(null) }
    var route by remember { mutableStateOf<RouteResponse?>(null) }

    val tracker = remember { LocationTracker(context) }
    var hasBuyerPermission by remember { mutableStateOf(tracker.hasPermission()) }
    var buyerLat by remember { mutableStateOf<Double?>(null) }
    var buyerLng by remember { mutableStateOf<Double?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasBuyerPermission =
            grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Live tracking covers the whole active journey:
    //   dispatched / shipped   → driver collecting at hub
    //   out_for_delivery       → driver en route to buyer
    val activeStatuses = setOf("dispatched", "shipped", "out_for_delivery")
    val shouldTrack = order.trackingStatus in activeStatuses

    LaunchedEffect(shouldTrack) {
        if (shouldTrack &&
            order.deliveryLatitude == null &&
            order.deliveryLongitude == null &&
            !hasBuyerPermission
        ) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
    }

    LaunchedEffect(order.id, shouldTrack) {
        if (!shouldTrack) return@LaunchedEffect
        while (true) {
            val token = TokenStore.token
            if (token != null) {
                try {
                    val resp = RetrofitClient.api.getDriverLocation("Bearer $token", order.id)
                    if (resp.isSuccessful) driverLocation = resp.body()
                } catch (_: Exception) { }
            }
            delay(10_000L)
        }
    }

    LaunchedEffect(order.id, shouldTrack, driverLocation?.updatedAt) {
        if (!shouldTrack) return@LaunchedEffect
        val token = TokenStore.token ?: return@LaunchedEffect
        try {
            val resp = RetrofitClient.api.getOrderRoute("Bearer $token", order.id)
            if (resp.isSuccessful) route = resp.body()
        } catch (_: Exception) { }
    }

    LaunchedEffect(shouldTrack, hasBuyerPermission, order.deliveryLatitude) {
        val hasSaved = order.deliveryLatitude != null && order.deliveryLongitude != null
        if (shouldTrack && hasBuyerPermission && !hasSaved) {
            tracker.start { lat, lng ->
                buyerLat = lat
                buyerLng = lng
            }
        } else {
            tracker.stop()
        }
    }

    DisposableEffect(Unit) {
        onDispose { tracker.stop() }
    }

    val destLat = order.deliveryLatitude ?: buyerLat
    val destLng = order.deliveryLongitude ?: buyerLng
    val hasDestination = destLat != null && destLng != null

    val dl = driverLocation
    val currentRoute = route

    val straightLineMeters: Double? = if (dl?.latitude != null && dl.longitude != null &&
        destLat != null && destLng != null
    ) {
        TrackingUtils.metersBetween(destLat, destLng, dl.latitude, dl.longitude)
    } else null

    val distanceMeters: Double? = currentRoute?.distanceMeters ?: straightLineMeters
    val etaMinutes: Int? = when {
        currentRoute?.durationSeconds != null ->
            Math.ceil(currentRoute.durationSeconds / 60.0).toInt().coerceAtLeast(1)
        straightLineMeters != null ->
            TrackingUtils.etaMinutes(straightLineMeters)
        else -> null
    }
    val distanceLabel: String? = distanceMeters?.let { TrackingUtils.formatDistance(it) }

    Scaffold(containerColor = AgriCreamBg, bottomBar = bottomBar) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Track Order",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    Text(
                        order.trackingId,
                        style = MaterialTheme.typography.bodySmall,
                        color = AgriTextSecondary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // ── Distance / ETA strip (no map) ──
                if (shouldTrack) {
                    if (hasDestination && distanceLabel != null && etaMinutes != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(AgriCardBg)
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Destination",
                                    fontSize = 10.sp,
                                    color = AgriTextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "You",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriTextPrimary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(32.dp)
                                    .background(AgriBorderLight)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Distance",
                                    fontSize = 10.sp,
                                    color = AgriTextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    distanceLabel,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriTextPrimary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(32.dp)
                                    .background(AgriBorderLight)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "ETA",
                                    fontSize = 10.sp,
                                    color = AgriTextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    if (etaMinutes < 60) "$etaMinutes min"
                                    else "${etaMinutes / 60}h ${etaMinutes % 60}m",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriGreen
                                )
                            }
                        }
                    } else if (dl?.latitude == null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AgriMintLight)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AgriGreen)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Driver is on the way — waiting for their GPS.",
                                color = AgriGreenDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else if (!hasDestination) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AgriOrange.copy(alpha = 0.15f))
                                .padding(14.dp)
                        ) {
                            Text(
                                if (!hasBuyerPermission)
                                    "Enable location to see distance and ETA."
                                else
                                    "This order doesn't have a saved delivery location.",
                                color = AgriTextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }

                // ── Driver card ──
                if (dl?.driverName != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AgriCardBg)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(AgriMintLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                dl.driverName
                                    .split(" ")
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .take(2)
                                    .joinToString(""),
                                fontWeight = FontWeight.Bold,
                                color = AgriGreenDark,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Your driver",
                                fontSize = 10.sp,
                                color = AgriTextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                dl.driverName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AgriTextPrimary
                            )
                            dl.driverPlate?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AgriTextSecondary
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(AgriGreen)
                                .clickable(enabled = !dl.driverPhone.isNullOrBlank()) {
                                    try {
                                        val uri = Uri.parse("tel:${dl.driverPhone}")
                                        context.startActivity(Intent(Intent.ACTION_DIAL, uri))
                                    } catch (_: Exception) { }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📞", fontSize = 18.sp)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }

                // Order progress timeline
                Text(
                    "Order progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                Spacer(Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = AgriCardBg),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
                        stages.forEachIndexed { index, stage ->
                            TimelineRow(
                                stage = stage,
                                isLast = index == stages.lastIndex
                            )
                        }
                    }
                }

                order.trackingNote?.takeIf { it.isNotBlank() }?.let { note ->
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AgriMintLight)
                            .padding(16.dp)
                    ) {
                        Text(text = "📝", fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Note from AgriMarket",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AgriGreenDark
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                note,
                                style = MaterialTheme.typography.bodySmall,
                                color = AgriGreenDark.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { onNeedHelp(order) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriGreen,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Text("Need help with this order?", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TimelineRow(stage: TimelineStage, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        when (stage.state) {
                            StageState.DONE -> AgriGreen
                            StageState.CURRENT -> AgriMintLight
                            StageState.PENDING -> AgriCardBg
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (stage.state) {
                    StageState.DONE -> Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    StageState.CURRENT -> Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(AgriGreen)
                    )
                    StageState.PENDING -> Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AgriBorderLight)
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(44.dp)
                        .background(
                            if (stage.state == StageState.DONE) AgriGreen.copy(alpha = 0.4f)
                            else AgriBorderLight
                        )
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)) {
            Text(
                text = stage.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = when (stage.state) {
                    StageState.PENDING -> FontWeight.Medium
                    else -> FontWeight.Bold
                },
                color = when (stage.state) {
                    StageState.PENDING -> AgriTextSecondary
                    else -> AgriTextPrimary
                }
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stage.detail,
                style = MaterialTheme.typography.bodySmall,
                color = if (stage.state == StageState.CURRENT) AgriGreen else AgriTextSecondary
            )
        }
    }
}

private fun buildStages(orderDto: OrderDto): List<TimelineStage> {
    val currentStage = when {
        orderDto.status == "pending_payment" -> 0
        orderDto.status == "cancelled" -> -1
        orderDto.trackingStatus == "order_placed" -> 1
        orderDto.trackingStatus == "processing" -> 2
        orderDto.trackingStatus == "dispatched" || orderDto.trackingStatus == "shipped" -> 3
        orderDto.trackingStatus == "out_for_delivery" -> 4
        orderDto.trackingStatus == "delivered" -> 5
        else -> 1
    }

    if (currentStage == -1) {
        return listOf(
            TimelineStage("Order Cancelled", "This order was cancelled.", StageState.CURRENT)
        )
    }

    fun stageAt(
        stageNumber: Int,
        label: String,
        detailDone: String,
        detailPending: String,
        detailCurrent: String
    ): TimelineStage {
        val state = when {
            currentStage > stageNumber -> StageState.DONE
            currentStage == stageNumber -> StageState.CURRENT
            else -> StageState.PENDING
        }
        val detail = when (state) {
            StageState.DONE -> detailDone
            StageState.CURRENT -> detailCurrent
            StageState.PENDING -> detailPending
        }
        return TimelineStage(label, detail, state)
    }

    val placedDate = formatShortDate(orderDto.createdAt)

    return listOf(
        stageAt(1, "Order Placed", placedDate, "Pending", placedDate),
        stageAt(2, "Processing", "Package has been received at the AgriMarket Hub", "Pending", "Package has been received at the AgriMarket Hub"),
        stageAt(3, "Dispatched", "Handed over to courier", "Pending", "Being handed to courier"),
        stageAt(4, "Out for Delivery", "Arrived in your area", "Pending", "On the way to you"),
        stageAt(5, "Delivered", "Enjoy your order!", "Pending", "Marked as Delivered")
    )
}

private fun formatShortDate(iso: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(iso) ?: return ""
        val formatter = SimpleDateFormat("d MMM, h:mm a", Locale.US)
        formatter.format(date)
    } catch (_: Exception) { "" }
}