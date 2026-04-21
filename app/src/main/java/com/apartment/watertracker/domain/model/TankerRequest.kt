package com.apartment.watertracker.domain.model

import java.time.Instant

data class TankerRequest(
    val id: String,
    val apartmentId: String,
    val apartmentName: String,
    val requestedByUserId: String,
    val quantityLiters: Int,
    val urgency: RequestUrgency,
    val createdAt: Instant,
    val status: RequestStatus,
    val notes: String?,
    val bidsCount: Int = 0
)

enum class RequestUrgency {
    NORMAL,
    URGENT,
    CRITICAL
}

enum class RequestStatus {
    OPEN,
    FULFILLED,
    CANCELLED
}
