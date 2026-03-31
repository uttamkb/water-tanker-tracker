package com.apartment.watertracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supply_entries")
data class SupplyEntryEntity(
    @PrimaryKey val id: String,
    val apartmentId: String,
    val vendorId: String,
    val hardnessPpm: Int,
    val capturedAtEpochMillis: Long,
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
