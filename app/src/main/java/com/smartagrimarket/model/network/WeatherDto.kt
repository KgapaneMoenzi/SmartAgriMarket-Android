package com.smartagrimarket.model.network

data class FarmerWeatherAdvice(
    val city: String,
    val lat: Double,
    val lon: Double,
    val temp: Int,
    val feelsLike: Int,
    val humidity: Int,
    val wind: Double,
    val condition: String,
    val conditionDesc: String,
    val climateZone: String,
    val season: String,
    val advice: String,
    val crops: List<String>,
    val schedule: String,
    val isWet: Boolean,
    val isHot: Boolean,
    val isCold: Boolean,
    val isFrostRisk: Boolean,
    val forecast: List<DailyForecastDto>,

    // THESE ARE THE MISSING FIELDS:
    val fallback: Boolean = false,
    val requestedLocation: String? = null
)

data class DailyForecastDto(
    val day: Int,
    val condition: String,
    val tempHigh: Int,
    val tempLow: Int,
    val description: String
)