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