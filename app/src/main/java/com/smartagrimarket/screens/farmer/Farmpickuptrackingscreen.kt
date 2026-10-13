/*
 * ---------------------------------------------------------------------------
 * GEOCODING NOTES
 * ---------------------------------------------------------------------------
 * Uses Nominatim (OpenStreetMap's free geocoder) to turn a farm's stored
 * text address into lat/lng when it hasn't pinned a live GPS location.
 * No API key needed, but Nominatim's usage policy requires:
 *   - A descriptive User-Agent header (set below)
 *   - Max ~1 request/second, and don't hammer it in a loop
 *   - Results should be cached where possible (geocode once, store the
 *     result on the farm record server-side, not on every screen load)
 * ---------------------------------------------------------------------------
 */

package com.smartagrimarket.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smartagrimarket.components.LeafletMapView
import com.smartagrimarket.components.MapMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

data class FarmPickup(
    val id: String,
    val name: String,
    val contactPerson: String,
    val addressLabel: String,       // e.g. "Tzaneen, Polokwane" — always required
    val pinnedLat: Double? = null,  // present only if farm shared a live pin
    val pinnedLng: Double? = null,
    val packed: Boolean = false,
    val items: List<String> = emptyList(),
)

/** Geocodes a free-text address via Nominatim. Returns null if it can't be resolved. */
suspend fun geocodeAddress(address: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
    val url = "https://nominatim.openstreetmap.org/search" +
            "?q=${java.net.URLEncoder.encode(address, "UTF-8")}&format=json&limit=1"

    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .header("User-Agent", "SmartAgriMarket-Student-App/1.0 (contact: your-email@example.com)")
        .build()

    try {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: return@withContext null
            val arr = JSONArray(body)
            if (arr.length() == 0) return@withContext null
            val first = arr.getJSONObject(0)
            first.getString("lat").toDouble() to first.getString("lon").toDouble()
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun FarmPickupTrackingScreen(
    orderId: String,
    status: String,
    currentLocation: Pair<Double, Double>, // courier's live location
    farms: List<FarmPickup>,
) {
    var activeFarm by remember { mutableStateOf(farms.firstOrNull()) }
    var activeFarmCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var geocoding by remember { mutableStateOf(false) }

    // Resolve the active farm's destination coords: live pin first, else geocode the address.
    LaunchedEffect(activeFarm) {
        val farm = activeFarm
        if (farm == null) {
            activeFarmCoords = null
            return@LaunchedEffect
        }
        if (farm.pinnedLat != null && farm.pinnedLng != null) {
            activeFarmCoords = farm.pinnedLat to farm.pinnedLng
        } else {
            geocoding = true
            activeFarmCoords = geocodeAddress(farm.addressLabel)
            geocoding = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Text(
            text = "Order #$orderId",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
        Text(
            text = "Status: $status",
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Map now lives in the space that used to be blank.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            LeafletMapView(
                markers = listOfNotNull(
                    MapMarker(
                        id = "me",
                        lat = currentLocation.first,
                        lng = currentLocation.second,
                        emoji = "🟢"
                    ),
                    activeFarmCoords?.let { (lat, lng) ->
                        MapMarker(id = "farm", lat = lat, lng = lng, emoji = "🔴")
                    }
                ),
                origin = currentLocation,
                destination = activeFarmCoords,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Only show this banner if we genuinely have nothing — no live pin AND geocoding failed.
        if (activeFarm != null && activeFarmCoords == null && !geocoding) {
            InfoBanner(
                text = "Couldn't locate ${activeFarm?.name} — no live pin or resolvable address.",
                backgroundColor = Color(0xFFF3E3B8)
            )
        } else if (activeFarm?.pinnedLat == null && activeFarmCoords != null) {
            InfoBanner(
                text = "Showing route based on ${activeFarm?.name}'s provided address (not live).",
                backgroundColor = Color(0xFFF3E3B8)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        InfoBanner(
            text = "\uD83D\uDFE2 Sharing your live location with the buyer",
            backgroundColor = Color(0xFFDCE8D2),
            textColor = Color(0xFF4C7A3B)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Pick up from",
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        farms.forEach { farm ->
            FarmCard(
                farm = farm,
                isActive = activeFarm?.id == farm.id,
                onClick = { activeFarm = farm } // tapping the card is now the only trigger
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FarmCard(
    farm: FarmPickup,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) Color(0xFFF6F2E8) else Color(0xFFF1EEE6))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(farm.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    farm.contactPerson,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    farm.addressLabel,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            AssistChip(
                onClick = {},
                label = { Text(if (farm.packed) "Packed" else "Not packed") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        farm.items.forEach { item ->
            Text("• $item", modifier = Modifier.padding(vertical = 1.dp))
        }
    }
}

@Composable
private fun InfoBanner(
    text: String,
    backgroundColor: Color,
    textColor: Color = Color(0xFF7A6A3F),
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Text(text, color = textColor)
    }
}