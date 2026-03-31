package com.apartment.watertracker.domain.model

import java.time.Instant

data class SupplyEntry(
    val id: String,
    val apartmentId: String,
    val vendorId: String,
    val hardnessPpm: Int,
    val capturedAt: Instant,
    val latitude: Double,
    val longitude: Double,
    val gpsAccuracyMeters: Float,
    val vehicleNumber: String?,
    val remarks: String?,
    val photoUrl: String?,
    val duplicateFlag: Boolean,
    val duplicateReferenceId: String?,
    val createdByUserId: String,
)
