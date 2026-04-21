package com.apartment.watertracker.domain.model

data class VendorRating(
    val avgQuality: Float,
    val avgTimeliness: Float,
    val avgHygiene: Float,
    val totalRatings: Int
) {
    val overallRating: Float
        get() = (avgQuality + avgTimeliness + avgHygiene) / 3f
}

data class Vendor(
    val id: String,
    val apartmentId: String,
    val supplierName: String,
    val contactPerson: String?,
    val phoneNumber: String,
    val alternatePhoneNumber: String?,
    val address: String?,
    val notes: String?,
    val isActive: Boolean,
    val qrValue: String,
    val defaultCapacityLiters: Int = 5000,
)
