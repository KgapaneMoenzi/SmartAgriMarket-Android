package com.smartagrimarket.screens.driver

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationTracker(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var onLocation: ((Double, Double) -> Unit)? = null
    private var isRunning = false

    private val request: LocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 15_000L)
            .setMinUpdateIntervalMillis(10_000L)
            .setMinUpdateDistanceMeters(20f)
            .build()

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            onLocation?.invoke(loc.latitude, loc.longitude)
        }
    }

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun start(listener: (Double, Double) -> Unit) {
        if (!hasPermission() || isRunning) return
        onLocation = listener
        try {
            fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
            isRunning = true
        } catch (_: SecurityException) {
        }
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        if (!isRunning) return
        try {
            fusedClient.removeLocationUpdates(callback)
        } catch (_: SecurityException) {
        }
        onLocation = null
        isRunning = false
    }
}