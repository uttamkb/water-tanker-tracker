package com.apartment.watertracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deliveries")
data class DeliveryEntity(
    @PrimaryKey val id: String,
    val apartmentId: String,
    val vendorId: String,
    val timestamp: Long,
    val quantityLiters: Int,
    val driverName: String?,
    val status: String, // Storing enum as String for simplicity in Room
    val latitude: Double?,
    val longitude: Double?
)
