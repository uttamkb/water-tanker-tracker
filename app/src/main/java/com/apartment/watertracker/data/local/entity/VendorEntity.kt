package com.apartment.watertracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vendors")
data class VendorEntity(
    @PrimaryKey val id: String,
    val apartmentId: String,
    val supplierName: String,
    val contactPerson: String?,
    val phoneNumber: String,
    val alternatePhoneNumber: String?,
    val address: String?,
    val notes: String?,
    val isActive: Boolean,
    val qrValue: String,
)
