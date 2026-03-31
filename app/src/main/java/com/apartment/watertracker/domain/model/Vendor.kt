package com.apartment.watertracker.domain.model

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
)
