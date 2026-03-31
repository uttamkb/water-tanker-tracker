package com.apartment.watertracker.domain.model

import java.time.Instant

data class OperatorInvite(
    val id: String,
    val apartmentId: String,
    val email: String,
    val role: UserRole,
    val status: String,
    val createdAt: Instant,
    val createdByUserId: String,
)
