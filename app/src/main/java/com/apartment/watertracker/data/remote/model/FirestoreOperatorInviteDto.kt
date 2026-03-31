package com.apartment.watertracker.data.remote.model

data class FirestoreOperatorInviteDto(
    val apartmentId: String = "",
    val email: String = "",
    val role: String = "OPERATOR",
    val status: String = "PENDING",
    val createdAtEpochMillis: Long = 0L,
    val createdByUserId: String = "",
)
