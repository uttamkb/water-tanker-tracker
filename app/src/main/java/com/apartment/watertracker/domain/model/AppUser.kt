package com.apartment.watertracker.domain.model

data class AppUser(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val apartmentId: String,
    val apartmentName: String?,
    val fcmTokens: List<String> = emptyList(),
)
