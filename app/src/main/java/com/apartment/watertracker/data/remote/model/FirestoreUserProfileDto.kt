package com.apartment.watertracker.data.remote.model

data class FirestoreUserProfileDto(
    val name: String = "",
    val email: String = "",
    val role: String = "ADMIN",
    val apartmentId: String = "",
    val apartmentName: String? = null,
    val fcmTokens: List<String> = emptyList(),
)
