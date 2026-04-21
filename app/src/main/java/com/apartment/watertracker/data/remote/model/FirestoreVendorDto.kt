package com.apartment.watertracker.data.remote.model

data class FirestoreVendorDto(
    val apartmentId: String = "",
    val supplierName: String = "",
    val contactPerson: String? = null,
    val phoneNumber: String = "",
    val alternatePhoneNumber: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true,
    val qrValue: String = "",
    val defaultCapacityLiters: Int = 5000,
)
