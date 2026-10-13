package com.smartagrimarket.model.network

// ═══════════════════════════════════════════════════════════
// AUTH
// ═══════════════════════════════════════════════════════════

data class SignUpRequest(
    val fullName: String,
    val email: String,
    val phone: String?,
    val password: String,
    val role: String
)

data class SignInRequest(
    val email: String,
    val password: String
)

data class VerifyEmailRequest(
    val email: String,
    val code: String
)

data class ResendCodeRequest(
    val email: String
)

data class UserDto(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String?,
    val role: String,
    val emailVerified: Boolean,
    val photoUrl: String? = null       // ← ADDED
)

data class SignUpResponse(
    val user: UserDto,
    val devOnlyVerificationCode: String? = null
)

data class AuthResponse(
    val token: String,
    val user: UserDto
)

data class ResendCodeResponse(
    val devOnlyVerificationCode: String? = null
)

data class ApiErrorResponse(
    val error: String,
    val needsVerification: Boolean? = null
)

// ═══════════════════════════════════════════════════════════
// WEATHER
// ═══════════════════════════════════════════════════════════

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
)

data class DailyForecastDto(
    val day: Int,
    val condition: String,
    val tempHigh: Int,
    val tempLow: Int,
    val description: String,
)