/*
 * ---------------------------------------------------------------------------
 * NATIVE MAP — OSMDroid (OpenStreetMap tiles, no API key)
 * ---------------------------------------------------------------------------
 * Replaces the earlier WebView + Leaflet implementation. Renders OSM tiles
 * directly into a native Android MapView, avoiding the HTML asset bundle
 * and JavaScript bridge entirely.
 *
 * Routing uses OSRM's free public demo server (router.project-osrm.org) —
 * no API key, no card, no account.
 * ---------------------------------------------------------------------------
 */

package com.smartagrimarket.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

data class MapMarker(
    val id: String,
    val lat: Double,
    val lng: Double,
    val emoji: String = "📍",
)

typealias MapPolyline = List<Pair<Double, Double>>

/**
 * Fetches a real driving route between two points from OSRM's free
 * public API. Returns (lat, lng) pairs ready for the polyline — empty
 * list if no route could be found.
 */
suspend fun fetchOsrmRoute(
    origin: Pair<Double, Double>,
    destination: Pair<Double, Double>
): MapPolyline = withContext(Dispatchers.IO) {
    val url = "https://router.project-osrm.org/route/v1/driving/" +
            "${origin.second},${origin.first};${destination.second},${destination.first}" +
            "?overview=full&geometries=geojson"

    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    try {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)
            if (json.optString("code") != "Ok") return@withContext emptyList()

            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) return@withContext emptyList()

            val coords = routes.getJSONObject(0)
                .getJSONObject("geometry")
                .getJSONArray("coordinates")

            (0 until coords.length()).map { i ->
                val pair = coords.getJSONArray(i)
                pair.getDouble(1) to pair.getDouble(0)
            }
        }
    } catch (e: Exception) {
        emptyList()
    }
}

@Composable
fun LeafletMapView(
    markers: List<MapMarker>,
    polyline: MapPolyline? = null,
    origin: Pair<Double, Double>? = null,
    destination: Pair<Double, Double>? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val mapView = remember {
        // Required by OSMDroid once per process
        Configuration.getInstance().userAgentValue = context.packageName

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(false)
            controller.setZoom(13.0)
            controller.setCenter(GeoPoint(-25.7479, 28.2293)) // Pretoria default
        }
    }

    var fetchedRoutePoints by remember { mutableStateOf<MapPolyline>(emptyList()) }
    val routePoints: MapPolyline = polyline ?: fetchedRoutePoints

    // Fetch a route only when no explicit polyline was given.
    LaunchedEffect(polyline, origin, destination) {
        fetchedRoutePoints = if (polyline == null && origin != null && destination != null) {
            fetchOsrmRoute(origin, destination)
        } else {
            emptyList()
        }
    }

    // Rebuild overlays whenever markers or route change.
    LaunchedEffect(markers, routePoints) {
        mapView.overlays.clear()

        // Route first, so markers sit on top.
        if (routePoints.size >= 2) {
            val pts = routePoints.map { GeoPoint(it.first, it.second) }
            val line = Polyline().apply {
                setPoints(pts)
                outlinePaint.color = 0xFF6FA85C.toInt()
                outlinePaint.strokeWidth = 8f
                outlinePaint.pathEffect = DashPathEffect(floatArrayOf(20f, 15f), 0f)
            }
            mapView.overlays.add(line)
        }

        // Markers
        markers.forEach { m ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(m.lat, m.lng)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon = emojiDrawable(context, m.emoji)
            }
            mapView.overlays.add(marker)
        }

        // Fit the viewport to all markers.
        if (markers.isNotEmpty()) {
            if (markers.size == 1) {
                mapView.controller.setZoom(15.0)
                mapView.controller.setCenter(GeoPoint(markers[0].lat, markers[0].lng))
            } else {
                var minLat = markers[0].lat
                var maxLat = markers[0].lat
                var minLng = markers[0].lng
                var maxLng = markers[0].lng
                markers.forEach { m ->
                    if (m.lat < minLat) minLat = m.lat
                    if (m.lat > maxLat) maxLat = m.lat
                    if (m.lng < minLng) minLng = m.lng
                    if (m.lng > maxLng) maxLng = m.lng
                }
                val box = BoundingBox(maxLat, maxLng, minLat, minLng)
                mapView.zoomToBoundingBox(box, true, 80)
            }
        }

        mapView.invalidate()
    }

    // MapView lifecycle — OSMDroid wants these calls to pause/resume tiles.
    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose { mapView.onPause() }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Renders an emoji onto a Bitmap and wraps it in a Drawable that OSMDroid's
 * Marker accepts. Emojis are drawn by the system font, so any emoji works.
 */
private fun emojiDrawable(context: Context, emoji: String, sizePx: Int = 96): Drawable {
    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sizePx * 0.7f
        textAlign = Paint.Align.CENTER
    }
    val y = sizePx / 2f - (paint.descent() + paint.ascent()) / 2f
    canvas.drawText(emoji, sizePx / 2f, y, paint)
    return BitmapDrawable(context.resources, bmp)
}