package com.smartagrimarket.model

/**
 * Role selected at sign-up. Drives copy and navigation to either the
 * buyer Home screen, the Farmer dashboard, or the Driver job board.
 */
enum class UserRole(val label: String, val emoji: String) {
    BUYER("Buyer", "🛒"),
    FARMER("Farmer", "🧑‍🌾"),
    DRIVER("Driver", "🚚"),
    ADMIN("Admin", "🛡️")
}

data class UserDto(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String?,
    val role: String,
    val emailVerified: Boolean,
    val photoUrl: String? = null
)