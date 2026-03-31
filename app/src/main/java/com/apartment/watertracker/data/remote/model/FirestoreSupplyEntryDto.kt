package com.apartment.watertracker.data.remote.model

data class FirestoreSupplyEntryDto(
    val apartmentId: String = "",
    val vendorId: String = "",
    val hardnessPpm: Int = 0,
    val capturedAtEpochMillis: Long = 0L,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val gpsAccuracyMeters: Float = 0f,
    val vehicleNumber: String? = null,
    val remarks: String? = null,
    val photoUrl: String? = null,
    val duplicateFlag: Boolean = false,
    val duplicateReferenceId: String? = null,
    val createdByUserId: String = "",
)
