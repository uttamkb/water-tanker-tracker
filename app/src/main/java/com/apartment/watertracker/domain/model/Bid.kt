package com.apartment.watertracker.domain.model

import java.time.Instant

data class Bid(
    val id: String,
    val requestId: String,
    val vendorId: String,
    val vendorName: String,
    val price: Double,
    val estimatedArrival: Instant,
    val createdAt: Instant,
    val status: BidStatus,
    val notes: String?
)

enum class BidStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    EXPIRED
}
