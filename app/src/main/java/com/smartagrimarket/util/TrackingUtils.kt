package com.smartagrimarket.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object TrackingUtils {

    /**
     * Haversine distance between two coordinates, in meters.
     * Accurate enough for display purposes — no need for a routing API.
     */
    fun metersBetween(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double,
    ): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a =
            sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Rough ETA in whole minutes, assuming a given average driving speed.
     * This is straight-line distance, not road distance — real driving
     * distance is usually 20–30% longer. The default 35 km/h bakes in
     * that correction plus some urban traffic.
     */
    fun etaMinutes(meters: Double, speedKmh: Double = 35.0): Int {
        if (meters <= 0) return 0
        val hours = meters / 1000.0 / speedKmh
        return Math.ceil(hours * 60).toInt().coerceAtLeast(1)
    }

    /**
     * Human-friendly distance: 850 m, 1.4 km, 12 km.
     */
    fun formatDistance(meters: Double): String {
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else if (meters < 10_000) {
            String.format("%.1f km", meters / 1000.0)
        } else {
            "${(meters / 1000).toInt()} km"
        }
    }
}