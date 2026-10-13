package com.smartagrimarket.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

data class SimpleLocation(val lat: Double, val lng: Double)

enum class LocationFailure {
    PERMISSION_DENIED,
    LOCATION_SERVICES_OFF,
    TIMEOUT,
    UNKNOWN
}

sealed class LocationResult {
    data class Success(val location: SimpleLocation) : LocationResult()
    data class Failure(val reason: LocationFailure, val message: String) : LocationResult()
}

object LocationHelper {

    fun hasPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * True when the OS-level Location toggle is on and at least one
     * provider (GPS or Network) is enabled.
     */
    fun isLocationEnabled(context: Context): Boolean {
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                lm.isLocationEnabled
            } else {
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Hard-bounded location fetch. Always returns within timeoutMs.
     */
    @SuppressLint("MissingPermission")
    suspend fun getLocation(
        context: Context,
        timeoutMs: Long = 30_000L,
    ): LocationResult = withContext(Dispatchers.IO) {

        // ── 1. Permission check ──
        if (!hasPermission(context)) {
            return@withContext LocationResult.Failure(
                LocationFailure.PERMISSION_DENIED,
                "Location permission denied. Enable it in Settings → Apps → SmartAgriMarket → Permissions."
            )
        }

        // ── 2. System Location toggle check ──
        if (!isLocationEnabled(context)) {
            return@withContext LocationResult.Failure(
                LocationFailure.LOCATION_SERVICES_OFF,
                "Turn on Location in your phone's quick settings, then try again."
            )
        }

        val client = LocationServices.getFusedLocationProviderClient(context)

        // ── 3. Fast path — cached last location ──
        val cached = withTimeoutOrNull(2_500L) {
            suspendCancellableCoroutine<Location?> { cont ->
                client.lastLocation
                    .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc) }
                    .addOnFailureListener { if (cont.isActive) cont.resume(null) }
            }
        }
        if (cached != null) {
            return@withContext LocationResult.Success(
                SimpleLocation(cached.latitude, cached.longitude)
            )
        }

        // ── 4. Slow path — request a fresh fix via getCurrentLocation ──
        val fresh = withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine<Location?> { cont ->
                val token = CancellationTokenSource()
                client.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    token.token
                )
                    .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc) }
                    .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                cont.invokeOnCancellation { token.cancel() }
            }
        }

        return@withContext if (fresh != null) {
            LocationResult.Success(SimpleLocation(fresh.latitude, fresh.longitude))
        } else {
            LocationResult.Failure(
                LocationFailure.TIMEOUT,
                "Couldn't get a GPS fix in ${timeoutMs / 1000} seconds. " +
                        "Move near a window or step outside, then try again."
            )
        }
    }
}