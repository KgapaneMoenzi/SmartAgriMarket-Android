package com.smartagrimarket.screens.driver

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
import com.smartagrimarket.components.LeafletMapView
import com.smartagrimarket.components.MapMarker
import com.smartagrimarket.components.MapPolyline
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.DriverJobDto
import com.smartagrimarket.model.network.DriverRepository
import com.smartagrimarket.model.network.PickupStopDto
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// ─────────────────────────────────────────────────────────────
// Pickup display helpers — the backend sends hub data through
// the same `farm*` fields when in HUB mode, but `pickupType`
// tells us which label to use in the UI.
// ─────────────────────────────────────────────────────────────

private fun pickupIsHub(p: PickupStopDto): Boolean = p.pickupType == "HUB"

private fun pickupDisplayName(p: PickupStopDto): String =
    if (pickupIsHub(p)) (p.hubName ?: p.farmName ?: "Central Hub")
    else (p.farmName ?: "Farm")

private fun pickupDisplaySubtitle(p: PickupStopDto): String? =
    if (pickupIsHub(p)) (p.hubAddress ?: p.farmTown)
    else p.farmTown

private fun pickupNavigateLabel(p: PickupStopDto): String =
    if (pickupIsHub(p)) "🗺️  Navigate to hub"
    else "🗺️  Navigate to farm"

private fun pickupSectionLabel(pickups: List<PickupStopDto>): String = when {
    pickups.isEmpty() -> "Pick up from"
    pickups.size == 1 && pickupIsHub(pickups.first()) -> "Pick up from hub"
    pickups.size == 1 -> "Pick up from"
    else -> "Pick up from (${pickups.size} stops)"
}

/**
 * Launches the system maps app pointed at either:
 *   1. Precise coordinates when available — `geo:lat,lng?q=lat,lng(label)`
 *   2. A text address as a fallback — `geo:0,0?q=<address>`
 *
 * Coordinates always win when present because they're unambiguous —
 * the driver's map app drops the pin exactly where the buyer placed it.
 */
private fun openNavigationTo(
    context: android.content.Context,
    lat: Double?,
    lng: Double?,
    fallbackAddress: String?,
    label: String,
) {
    val uri = when {
        lat != null && lng != null ->
            Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
        !fallbackAddress.isNullOrBlank() ->
            Uri.parse("geo:0,0?q=${Uri.encode(fallbackAddress)}")
        else -> return
    }
    try {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) { }
}

@Composable
fun DriverJobDetailScreen(
    job: DriverJobDto,
    onBack: () -> Unit,
    onStatusChanged: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tracker = remember { LocationTracker(context) }

    var current by remember { mutableStateOf(job) }
    var isWorking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    var hasPermission by remember { mutableStateOf(tracker.hasPermission()) }
    var currentLat by remember { mutableStateOf<Double?>(null) }
    var currentLng by remember { mutableStateOf<Double?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasPermission =
            grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
    }

    val ACTIVE_STATUSES = setOf(
        "accepted", "going_to_farm", "picked_up", "going_to_customer"
    )
    val isActive = current.status in ACTIVE_STATUSES

    LaunchedEffect(current.status, hasPermission) {
        if (hasPermission && isActive) {
            tracker.start { lat, lng ->
                currentLat = lat
                currentLng = lng
                scope.launch {
                    DriverRepository.sendLocation(current.id, lat, lng)
                }
            }
        } else {
            tracker.stop()
        }
    }

    DisposableEffect(Unit) {
        onDispose { tracker.stop() }
    }

    val pickups: List<PickupStopDto> = remember(current) {
        if (current.pickups.isNotEmpty()) current.pickups
        else if (current.pickupFarmName != null || current.pickupFarmerName != null) {
            listOf(
                PickupStopDto(
                    pickupType = current.pickupMode,
                    farmerEmail = "",
                    farmerName = current.pickupFarmerName,
                    farmName = current.pickupFarmName,
                    farmTown = current.pickupFarmTown,
                    farmLatitude = current.pickupLatitude,
                    farmLongitude = current.pickupLongitude,
                    items = current.items,
                    hasPacked = true,
                )
            )
        } else emptyList()
    }

    val isHubMode = current.pickupMode == "HUB" || pickups.any { pickupIsHub(it) }

    val unpackedCount = pickups.count { !it.hasPacked }
    val allPacked = pickups.isNotEmpty() && unpackedCount == 0

    val goingToPickup = current.status == "accepted" || current.status == "going_to_farm"
    val goingToBuyer = current.status == "picked_up" || current.status == "going_to_customer"

    val activePickup: PickupStopDto? = remember(current, pickups) {
        pickups.firstOrNull { !it.hasPacked } ?: pickups.firstOrNull()
    }

    val destinationLat: Double? = when {
        goingToPickup -> activePickup?.farmLatitude
        goingToBuyer -> current.deliveryLatitude
        else -> null
    }
    val destinationLng: Double? = when {
        goingToPickup -> activePickup?.farmLongitude
        goingToBuyer -> current.deliveryLongitude
        else -> null
    }
    val destinationLabel: String = when {
        goingToPickup -> activePickup?.let { pickupDisplayName(it) } ?: "Pickup"
        goingToBuyer -> "Buyer"
        else -> "Destination"
    }

    val markers: List<MapMarker> = buildList {
        val lat = currentLat
        val lng = currentLng
        if (lat != null && lng != null) {
            add(MapMarker(id = "me", lat = lat, lng = lng, emoji = "🚚"))
        }
        if (destinationLat != null && destinationLng != null) {
            add(MapMarker(
                id = "dest",
                lat = destinationLat,
                lng = destinationLng,
                emoji = if (goingToPickup && isHubMode) "🏬" else "🏠"
            ))
        }
    }

    val polyline: MapPolyline = buildList {
        val lat = currentLat
        val lng = currentLng
        if (lat != null && lng != null && destinationLat != null && destinationLng != null) {
            add(lat to lng)
            add(destinationLat to destinationLng)
        }
    }

    val distanceKm: Double? = if (currentLat != null && currentLng != null &&
        destinationLat != null && destinationLng != null
    ) {
        haversineKm(currentLat!!, currentLng!!, destinationLat, destinationLng)
    } else null

    val etaMinutes: Int? = distanceKm?.let {
        ((it / 40.0) * 60.0).toInt().coerceAtLeast(1)
    }

    val address = current.deliveryAddress
    val addressLine = listOfNotNull(
        (address?.get("street") as? String),
        (address?.get("suburb") as? String),
        (address?.get("city") as? String),
        (address?.get("postalCode") as? String)
    ).filter { it.isNotBlank() }.joinToString(", ")

    // For the standalone Navigate button
    fun navigateToCurrentDestination() {
        when (current.status) {
            "going_to_farm" -> {
                val p = activePickup
                if (p != null) {
                    openNavigationTo(
                        context = context,
                        lat = p.farmLatitude,
                        lng = p.farmLongitude,
                        fallbackAddress = listOfNotNull(
                            pickupDisplayName(p),
                            pickupDisplaySubtitle(p)
                        ).filter { it.isNotBlank() }.joinToString(", ").ifBlank { null },
                        label = pickupDisplayName(p)
                    )
                }
            }
            "picked_up", "going_to_customer" -> {
                openNavigationTo(
                    context = context,
                    lat = current.deliveryLatitude,
                    lng = current.deliveryLongitude,
                    fallbackAddress = addressLine.ifBlank { null },
                    label = "Customer"
                )
            }
        }
    }

    val nextAction: Pair<String, String>? = when (current.status) {
        "unassigned" -> "Accept job" to "accepted"
        "accepted" -> {
            if (isHubMode) "Head to hub" to "going_to_farm"
            else "Head to farm" to "going_to_farm"
        }
        "going_to_farm" -> if (allPacked) {
            if (isHubMode) "Picked up from hub" to "picked_up"
            else "Picked up order" to "picked_up"
        } else null
        "picked_up" -> "Head to customer" to "going_to_customer"
        "going_to_customer" -> "Mark as delivered" to "delivered"
        else -> null
    }

    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Header ──
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
                        "Order #${current.trackingId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    Text(
                        current.status.replace('_', ' ').replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = AgriTextSecondary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                if (isActive) {
                    // ── Map ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(AgriMintSoft)
                    ) {
                        LeafletMapView(
                            markers = markers,
                            polyline = polyline,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (markers.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Waiting for GPS signal…",
                                    color = AgriTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // ── Distance / ETA strip ──
                    if (distanceKm != null && etaMinutes != null) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(AgriCardBg)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Destination",
                                    fontSize = 10.sp,
                                    color = AgriTextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    destinationLabel,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriTextPrimary,
                                    maxLines = 1
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
                                    "${"%.1f".format(distanceKm)} km",
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
                    } else if (currentLat != null && currentLng != null &&
                        destinationLat == null
                    ) {
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AgriOrange.copy(alpha = 0.15f))
                                .padding(14.dp)
                        ) {
                            Text(
                                when {
                                    goingToPickup && isHubMode -> "Hub doesn't have coordinates set."
                                    goingToPickup -> "Farm hasn't pinned its location yet."
                                    else -> "Buyer didn't share location at checkout."
                                },
                                color = AgriOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ── Location sharing banner ──
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
                            "Sharing your live location with the buyer",
                            color = AgriGreenDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // ── Navigate button ──
                    if (current.status in setOf("going_to_farm", "picked_up", "going_to_customer")) {
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { navigateToCurrentDestination() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AgriCardBg,
                                contentColor = AgriTextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorderLight)
                        ) {
                            val label = when (current.status) {
                                "going_to_farm" -> {
                                    val p = activePickup
                                    val name = p?.let { pickupDisplayName(it) } ?: "pickup"
                                    if (isHubMode) "🗺️  Navigate to $name"
                                    else "🗺️  Navigate to $name"
                                }
                                "picked_up", "going_to_customer" -> "🗺️  Navigate to customer"
                                else -> "🗺️  Navigate"
                            }
                            Text(label, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (isActive && !hasPermission) {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriRedAlert.copy(alpha = 0.12f))
                            .padding(14.dp)
                    ) {
                        Text(
                            "Location permission denied — the buyer can't see where you are.",
                            color = AgriRedAlert,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Pickups ──
                SectionLabel(pickupSectionLabel(pickups))
                Spacer(Modifier.height(10.dp))

                if (pickups.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "No pickup information available.",
                                color = AgriTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    pickups.forEachIndexed { index, pickup ->
                        PickupCard(
                            pickup = pickup,
                            onNavigate = {
                                val name = pickupDisplayName(pickup)
                                val sub = pickupDisplaySubtitle(pickup)
                                openNavigationTo(
                                    context = context,
                                    lat = pickup.farmLatitude,
                                    lng = pickup.farmLongitude,
                                    fallbackAddress = listOfNotNull(name, sub)
                                        .filter { it.isNotBlank() }
                                        .joinToString(", ")
                                        .ifBlank { null },
                                    label = name
                                )
                            }
                        )
                        if (index != pickups.lastIndex) Spacer(Modifier.height(10.dp))
                    }
                }

                if (current.status == "going_to_farm" && !allPacked) {
                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriOrange.copy(alpha = 0.15f))
                            .padding(14.dp)
                    ) {
                        Text(
                            if (isHubMode)
                                "Waiting for farmers to drop off their items at the hub. You can only mark this order as picked up once every farmer has dropped off."
                            else if (unpackedCount == 1)
                                "1 farm hasn't packed yet. You can only mark this order as picked up once every farm has packed."
                            else
                                "$unpackedCount farms haven't packed yet. You can only mark this order as picked up once every farm has packed.",
                            color = AgriOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                SectionLabel("Deliver to")
                Spacer(Modifier.height(10.dp))

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
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(AgriMintLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                current.buyerName.firstOrNull()?.uppercase() ?: "?",
                                fontWeight = FontWeight.Bold,
                                color = AgriGreenDark
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                current.buyerName,
                                fontWeight = FontWeight.Bold,
                                color = AgriTextPrimary,
                                fontSize = 15.sp
                            )
                            if (addressLine.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    addressLine,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AgriTextSecondary
                                )
                            }
                            val hasPin = current.deliveryLatitude != null && current.deliveryLongitude != null
                            if (hasPin) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "📍 Pinned location available",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AgriGreenDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                SectionLabel("All items (${current.items.size})")
                Spacer(Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AgriCardBg),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        current.items.forEach { item ->
                            val name = item["name"] as? String ?: "Item"
                            val qty = (item["quantity"] as? Number)?.toInt() ?: 1
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(name, style = MaterialTheme.typography.bodySmall, color = AgriTextPrimary)
                                Text("× $qty", style = MaterialTheme.typography.bodySmall, color = AgriTextSecondary)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = AgriBorderLight)
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", fontWeight = FontWeight.Bold, color = AgriTextPrimary)
                            Text(
                                "R${String.format("%.2f", current.total)}",
                                fontWeight = FontWeight.Bold,
                                color = AgriGreenDark
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // ── Bottom action ──
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                nextAction?.let { (label, newStatus) ->
                    Button(
                        onClick = {
                            isWorking = true
                            errorMessage = null
                            successMessage = null
                            scope.launch {
                                val result = if (current.status == "unassigned") {
                                    DriverRepository.acceptJob(current.id)
                                } else {
                                    DriverRepository.updateStatus(current.id, newStatus)
                                }
                                when (result) {
                                    is ApiResult.Success -> {
                                        current = result.data
                                        successMessage = "Updated."
                                        onStatusChanged()

                                        // When the driver taps "Head to customer",
                                        // fire the system navigation intent using the
                                        // buyer's pinned coordinates. If for some
                                        // reason the pin is missing, fall back to
                                        // the address text.
                                        if (newStatus == "going_to_customer") {
                                            openNavigationTo(
                                                context = context,
                                                lat = current.deliveryLatitude,
                                                lng = current.deliveryLongitude,
                                                fallbackAddress = addressLine.ifBlank { null },
                                                label = "Customer"
                                            )
                                        }
                                    }
                                    is ApiResult.Error -> errorMessage = result.message
                                }
                                isWorking = false
                            }
                        },
                        enabled = !isWorking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AgriGreen,
                            contentColor = Color.White
                        )
                    ) {
                        if (isWorking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Working…", fontWeight = FontWeight.Bold)
                        } else {
                            Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                if (nextAction == null && current.status == "delivered") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AgriMintLight)
                            .padding(16.dp)
                    ) {
                        Text(
                            "✓ Delivered. Great job!",
                            color = AgriGreenDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                errorMessage?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AgriRedAlert.copy(alpha = 0.12f))
                            .padding(12.dp)
                    ) {
                        Text(it, color = AgriRedAlert, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PickupCard(
    pickup: PickupStopDto,
    onNavigate: () -> Unit,
) {
    val isHub = pickupIsHub(pickup)
    val title = pickupDisplayName(pickup)
    val subtitle = pickupDisplaySubtitle(pickup)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (pickup.hasPacked) AgriMintLight else AgriCardBg
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isHub) {
                            Text("🏬 ", fontSize = 15.sp)
                        }
                        Text(
                            title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = AgriTextPrimary
                        )
                    }
                    if (isHub && pickup.farmerEmails.isNotEmpty()) {
                        Text(
                            "${pickup.farmerEmails.size} farmer${if (pickup.farmerEmails.size == 1) "" else "s"} dropped off here",
                            style = MaterialTheme.typography.bodySmall,
                            color = AgriTextSecondary
                        )
                    } else {
                        pickup.farmerName?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = AgriTextSecondary
                            )
                        }
                    }
                    subtitle?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = AgriTextSecondary
                        )
                    }
                }
                PackingBadge(hasPacked = pickup.hasPacked, isHub = isHub)
            }

            if (pickup.items.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AgriMintSoft)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    pickup.items.forEach { item ->
                        val name = item["name"] as? String ?: "Item"
                        val qty = (item["quantity"] as? Number)?.toInt() ?: 1
                        Text(
                            "• $name × $qty",
                            style = MaterialTheme.typography.bodySmall,
                            color = AgriTextPrimary
                        )
                    }
                }
            }

            val target = listOfNotNull(title, subtitle)
                .filter { it.isNotBlank() }
                .joinToString(", ")
            if (target.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onNavigate,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AgriGreenDark
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgriGreen.copy(alpha = 0.4f))
                ) {
                    Text(pickupNavigateLabel(pickup), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PackingBadge(hasPacked: Boolean, isHub: Boolean) {
    val bg = if (hasPacked) AgriGreen.copy(alpha = 0.18f) else AgriOrange.copy(alpha = 0.20f)
    val fg = if (hasPacked) AgriGreenDark else AgriOrange
    val label = when {
        hasPacked && isHub -> "✓ Ready"
        hasPacked -> "✓ Packed"
        isHub -> "⏳ Waiting"
        else -> "⏳ Not packed"
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
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = AgriTextPrimary
    )
}

private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val R = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}