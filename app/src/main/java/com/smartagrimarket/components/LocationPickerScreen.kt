package com.smartagrimarket.components

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.viewinterop.AndroidView
import com.smartagrimarket.util.LocationHelper
import com.smartagrimarket.util.LocationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

data class PickedLocation(
    val lat: Double,
    val lng: Double,
    val label: String,
)

private val AgriCreamBg       = Color(0xFFEDE9E0)
private val AgriCardBg        = Color(0xFFF6F4EF)
private val AgriGreen         = Color(0xFF6FA85C)
private val AgriTextPrimary   = Color(0xFF1F1D1A)
private val AgriTextSecondary = Color(0xFF7A756B)
private val AgriRedAlert      = Color(0xFFD9534F)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LocationPickerScreen(
    title: String = "Pin your location",
    subtitle: String = "Drag the map so the pin sits on your exact spot.",
    initialLat: Double? = null,
    initialLng: Double? = null,
    onConfirm: (PickedLocation) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val startLat = initialLat ?: -25.7479
    val startLng = initialLng ?: 28.2293

    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(false)
            controller.setZoom(16.0)
            controller.setCenter(GeoPoint(startLat, startLng))
        }
    }

    var centerLat by remember { mutableStateOf(startLat) }
    var centerLng by remember { mutableStateOf(startLng) }
    var addressLabel by remember { mutableStateOf<String?>(null) }
    var isGeocoding by remember { mutableStateOf(false) }
    var geocodeError by remember { mutableStateOf<String?>(null) }
    var geocodeJob by remember { mutableStateOf<Job?>(null) }
    var pendingGps by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var isLocating by remember { mutableStateOf(false) }

    fun scheduleReverseGeocode(lat: Double, lng: Double) {
        geocodeJob?.cancel()
        geocodeJob = scope.launch {
            delay(700)
            isGeocoding = true
            geocodeError = null
            val label = reverseGeocode(lat, lng)
            if (label != null) {
                addressLabel = label
            } else {
                geocodeError = "Couldn't name this spot — the pin will still be saved."
            }
            isGeocoding = false
        }
    }

    DisposableEffect(Unit) {
        val listener = object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                val c = mapView.mapCenter
                centerLat = c.latitude
                centerLng = c.longitude
                addressLabel = null
                scheduleReverseGeocode(c.latitude, c.longitude)
                return true
            }
            override fun onZoom(event: ZoomEvent?): Boolean = true
        }
        mapView.addMapListener(listener)
        scheduleReverseGeocode(startLat, startLng)

        onDispose {
            mapView.removeMapListener(listener)
            mapView.onPause()
            mapView.onDetach()
        }
    }

    // ── GPS: "Use my current location" ──
    // Uses LocationHelper.getLocation() with a hard 30s timeout. Always
    // returns — either Success or Failure with a specific reason.
    fun beginGpsFetch() {
        isLocating = true
        geocodeError = null
        scope.launch {
            when (val result = LocationHelper.getLocation(context, timeoutMs = 30_000L)) {
                is LocationResult.Success -> {
                    pendingGps = result.location.lat to result.location.lng
                }
                is LocationResult.Failure -> {
                    geocodeError = result.message
                }
            }
            isLocating = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (!granted) {
            isLocating = false
            geocodeError = "Location permission denied — drag the pin manually."
            return@rememberLauncherForActivityResult
        }
        beginGpsFetch()
    }

    fun requestMyLocation() {
        geocodeError = null
        if (!LocationHelper.hasPermission(context)) {
            isLocating = true
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }
        beginGpsFetch()
    }

    // When GPS resolves, animate the map to that position
    LaunchedEffect(pendingGps) {
        val pair = pendingGps ?: return@LaunchedEffect
        val (lat, lng) = pair
        mapView.controller.animateTo(GeoPoint(lat, lng))
        mapView.controller.setZoom(17.0)
        centerLat = lat
        centerLng = lng
        addressLabel = null
        scheduleReverseGeocode(lat, lng)
        pendingGps = null
    }

    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Top bar — back button wired to onBack ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(AgriCardBg)
                        .clickable(onClick = onBack)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("←", fontSize = 20.sp, color = AgriTextPrimary)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                subtitle,
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary
            )

            Spacer(Modifier.height(12.dp))

            // ── Map with center pin overlay ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-22).dp)
                ) {
                    Text("📍", fontSize = 40.sp)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.25f))
                )
            }

            // ── Address preview card ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = AgriCardBg),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📌", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Selected location",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AgriTextSecondary
                        )
                    }
                    Spacer(Modifier.height(6.dp))

                    if (isGeocoding) {
                        Text(
                            "Finding address…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AgriTextSecondary
                        )
                    } else {
                        Text(
                            addressLabel ?: "Unknown area",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AgriTextPrimary
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${"%.5f".format(centerLat)}, ${"%.5f".format(centerLng)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AgriTextSecondary
                    )

                    if (geocodeError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            geocodeError!!,
                            fontSize = 11.sp,
                            color = AgriRedAlert
                        )
                    }
                }
            }

            // ── Action buttons ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { requestMyLocation() },
                    enabled = !isLocating,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AgriTextPrimary
                    )
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = AgriTextPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Locating…", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        Text("📍 Use my location", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick = {
                        onConfirm(
                            PickedLocation(
                                lat = centerLat,
                                lng = centerLng,
                                label = addressLabel
                                    ?: "${"%.4f".format(centerLat)}, ${"%.4f".format(centerLng)}"
                            )
                        )
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("Confirm", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Reverse geocoding via Nominatim
// ─────────────────────────────────────────────────────────────
private suspend fun reverseGeocode(lat: Double, lng: Double): String? =
    withContext(Dispatchers.IO) {
        val url = "https://nominatim.openstreetmap.org/reverse" +
                "?lat=$lat&lon=$lng&format=json&zoom=16&addressdetails=0"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "SmartAgriMarket-Student-App/1.0")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                val display = json.optString("display_name", "")
                if (display.isBlank()) return@withContext null
                display.split(",").map { it.trim() }.take(3).joinToString(", ")
            }
        } catch (_: Exception) {
            null
        }
    }