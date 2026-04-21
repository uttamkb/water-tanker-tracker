package com.apartment.watertracker.domain.model

data class Delivery(
    val id: String,
    val apartmentId: String,
    val vendorId: String,
    val timestamp: Long,
    val quantityLiters: Int,
    val driverName: String?,
    val status: DeliveryStatus,
    val latitude: Double?,
    val longitude: Double?
)

enum class DeliveryStatus {
    COMPLETED,
    CANCELLED
}
