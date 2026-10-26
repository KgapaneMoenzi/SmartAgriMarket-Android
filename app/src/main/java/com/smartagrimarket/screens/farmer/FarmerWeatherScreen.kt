@file:Suppress("DEPRECATION")

package com.smartagrimarket.screens.farmer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
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
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.CropRecommendationDto
import com.smartagrimarket.model.network.DailyForecastDto
import com.smartagrimarket.model.network.FarmerRepository
import com.smartagrimarket.model.network.FarmerWeatherAdvice
import com.smartagrimarket.model.network.TokenStore
import com.smartagrimarket.model.network.WeatherRepository
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerWeatherScreen(bottomBar: @Composable () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var locationInput by remember { mutableStateOf("Mpumalanga") }
    var weather by remember { mutableStateOf<FarmerWeatherAdvice?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isFetchingLocation by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    var permissionPermanentlyDenied by remember { mutableStateOf(false) }

    var recommendations by remember { mutableStateOf<List<CropRecommendationDto>>(emptyList()) }
    var isLoadingRecs by remember { mutableStateOf(true) }
    var cropToDismiss by remember { mutableStateOf<CropRecommendationDto?>(null) }
    var isDismissing by remember { mutableStateOf(false) }

    fun loadWeather(locationStr: String? = null, lat: Double? = null, lon: Double? = null) {
        scope.launch {
            isLoading = true
            errorMessage = null
            val token = TokenStore.token
            if (token == null) {
                errorMessage = "Please sign in again."
                isLoading = false
                return@launch
            }
            when (val result = WeatherRepository.getAdvice("Bearer $token", locationStr, lat, lon)) {
                is ApiResult.Success -> weather = result.data
                is ApiResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    fun loadRecommendations() {
        scope.launch {
            isLoadingRecs = true
            when (val r = FarmerRepository.fetchCropRecommendations()) {
                is ApiResult.Success -> recommendations = r.data.recommendations
                is ApiResult.Error -> { /* silent */ }
            }
            isLoadingRecs = false
        }
    }

    fun fetchFromDeviceLocation() {
        isFetchingLocation = true
        errorMessage = null

        val client = LocationServices.getFusedLocationProviderClient(context)
        try {
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc == null) {
                        isFetchingLocation = false
                        errorMessage = "Couldn't get your location. Try typing a town name."
                        return@addOnSuccessListener
                    }
                    isFetchingLocation = false
                    locationInput = "Your Location"
                    loadWeather(lat = loc.latitude, lon = loc.longitude)
                }
                .addOnFailureListener { e ->
                    isFetchingLocation = false
                    errorMessage = e.message ?: "Couldn't get your location."
                }
        } catch (_: SecurityException) {
            isFetchingLocation = false
            errorMessage = "Location permission missing."
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true || result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            permissionPermanentlyDenied = false
            fetchFromDeviceLocation()
        } else {
            val activity = context as? android.app.Activity
            val shouldShowRationale = activity?.let {
                androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION)
            } ?: false
            permissionPermanentlyDenied = !shouldShowRationale
            showPermissionRationale = true
        }
    }

    fun onUseCurrentLocationClick() {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            fetchFromDeviceLocation()
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    LaunchedEffect(Unit) {
        loadWeather(locationStr = locationInput)
        loadRecommendations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather & Farming", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAFAF5))
            )
        },
        bottomBar = bottomBar,
        containerColor = Color(0xFFFAFAF5)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            if (weather?.fallback == true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF3CD))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📍", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "We couldn't find '${weather?.requestedLocation ?: "your location"}'. Showing weather for Mbombela instead.",
                        fontSize = 12.sp,
                        color = Color(0xFF856404)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = locationInput,
                    onValueChange = { locationInput = it },
                    placeholder = { Text("e.g. Mbombela, Nelspruit") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFFFFFFF),
                        focusedContainerColor = Color(0xFFFFFFFF),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF1B7A3D),
                        cursorColor = Color(0xFF1B7A3D)
                    )
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1B7A3D)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { loadWeather(locationStr = locationInput) }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE8F3DC))
                    .clickable(enabled = !isFetchingLocation) { onUseCurrentLocationClick() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B7A3D)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFetchingLocation) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isFetchingLocation) "Getting your location…" else "Use current location",
                        fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1B7A3D)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text("Shows exact weather for your GPS coordinates", fontSize = 11.sp, color = Color(0xFF4A6B3D))
                }
            }

            Spacer(Modifier.height(20.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1B7A3D))
                }
            } else if (errorMessage != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFF6DEDE)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠️", fontSize = 16.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(errorMessage!!, color = Color(0xFFA13232), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            } else if (weather != null) {
                WeatherContent(
                    w = weather!!,
                    recommendations = recommendations,
                    isLoadingRecs = isLoadingRecs,
                    onDismissCrop = { cropToDismiss = it }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text(if (permissionPermanentlyDenied) "Enable location in Settings" else "Location permission needed") },
            text = {
                Text(if (permissionPermanentlyDenied) "You've denied location access. To use your current location, open the app settings and allow location access." else "AgriMarket needs your location to show weather for your area. You can also type a town name in the search box.")
            },
            confirmButton = {
                TextButton(onClick = {
                    if (permissionPermanentlyDenied) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                        context.startActivity(intent)
                    } else {
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }
                    showPermissionRationale = false
                }) { Text(if (permissionPermanentlyDenied) "Open settings" else "Allow", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showPermissionRationale = false }) { Text("Not now") } }
        )
    }

    cropToDismiss?.let { rec ->
        AlertDialog(
            onDismissRequest = { if (!isDismissing) cropToDismiss = null },
            title = { Text("Not interested in ${rec.crop}?") },
            text = { Text("We'll remember you passed on this suggestion. This helps us recommend crops you're more likely to grow.") },
            confirmButton = {
                TextButton(enabled = !isDismissing, onClick = {
                    isDismissing = true
                    scope.launch {
                        FarmerRepository.dismissCropRecommendation(rec.crop)
                        recommendations = recommendations.filterNot { it.crop == rec.crop }
                        isDismissing = false
                        cropToDismiss = null
                    }
                }) { Text("Skip this crop", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(enabled = !isDismissing, onClick = { cropToDismiss = null }) { Text("Keep it") } }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// THE MISSING COMPOSABLES ARE BELOW
// ─────────────────────────────────────────────────────────────

@Composable
private fun WeatherContent(
    w: FarmerWeatherAdvice,
    recommendations: List<CropRecommendationDto>,
    isLoadingRecs: Boolean,
    onDismissCrop: (CropRecommendationDto) -> Unit,
) {
    // ── Hero: current weather ──
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFFFFF))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${w.temp}°C",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    w.condition,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B7A3D)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    w.city,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${w.climateZone.replaceFirstChar { it.uppercase() }} · ${w.season}",
                    fontSize = 11.sp,
                    color = Color(0xFF666666)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WeatherStat("💧", "${w.humidity}%", "Humidity")
            WeatherStat("💨", String.format(Locale.US, "%.1f", w.wind), "m/s Wind")
            WeatherStat("🌡", "${w.feelsLike}°C", "Feels like")
        }
    }

    Spacer(Modifier.height(20.dp))

    // ── 3-day outlook ──
    if (w.forecast.isNotEmpty()) {
        Text(
            "3-day outlook",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            w.forecast.forEach { day ->
                ForecastCard(day, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(20.dp))
    }

    // ── Recommended to plant now ──
    if (isLoadingRecs) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE8F3DC))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = Color(0xFF1B7A3D)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Analysing what to plant…",
                fontSize = 13.sp,
                color = Color(0xFF1B7A3D),
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(20.dp))
    } else if (recommendations.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE8F3DC))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B7A3D)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌱", fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Recommended to plant now",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B7A3D)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Ranked by seasonality and buyer demand.",
                        fontSize = 11.sp,
                        color = Color(0xFF4A6B3D)
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            recommendations.forEachIndexed { index, rec ->
                CropRecommendationRow(
                    rec = rec,
                    rank = index + 1,
                    onDismiss = { onDismissCrop(rec) }
                )
                if (index != recommendations.lastIndex) Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Long-press a crop to skip it",
                fontSize = 10.sp,
                color = Color(0xFF4A6B3D).copy(alpha = 0.75f),
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(20.dp))
    }

    // ── Seasonal advice ──
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE8F3DC))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1B7A3D)),
                contentAlignment = Alignment.Center
            ) {
                Text("🌿", fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Seasonal advice",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B7A3D)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            w.advice,
            fontSize = 14.sp,
            color = Color(0xFF1A1A1A),
            lineHeight = 20.sp
        )
    }

    Spacer(Modifier.height(20.dp))

    // ── Crops for your region + planting schedule ──
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFFFFF))
            .padding(20.dp)
    ) {
        Text(
            "Crops for your region",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            w.crops.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { crop ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF1B7A3D))
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                crop,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // ── Planting schedule ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFFFF1DF))
                .padding(16.dp)
        ) {
            Text("📅", fontSize = 20.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Planting schedule",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B5A00)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    w.schedule,
                    fontSize = 13.sp,
                    color = Color(0xFF1A1A1A),
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CropRecommendationRow(
    rec: CropRecommendationDto,
    rank: Int,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .combinedClickable(
                onClick = { },
                onLongClick = onDismiss
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(
                    when (rank) {
                        1 -> Color(0xFFFFD54A)
                        2 -> Color(0xFFE0E0E0)
                        3 -> Color(0xFFE3B87A)
                        else -> Color(0xFFE8F3DC)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                rank.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF1A1A1A)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                rec.crop,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF1A1A1A)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                rec.reason,
                fontSize = 11.sp,
                color = Color(0xFF4A6B3D),
                lineHeight = 15.sp
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1B7A3D))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                "${rec.score}%",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WeatherStat(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF1A1A1A)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            fontSize = 11.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
private fun ForecastCard(day: DailyForecastDto, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFEFEF7))
            .padding(vertical = 14.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Day ${day.day}",
            fontSize = 11.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            when {
                day.condition.contains("Rain") -> "🌧"
                day.condition.contains("Snow") -> "❄️"
                day.condition.contains("Cloud") -> "☁️"
                else -> "☀️"
            },
            fontSize = 28.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "${day.tempHigh}°",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF1A1A1A)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "${day.tempLow}°",
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
    }
}