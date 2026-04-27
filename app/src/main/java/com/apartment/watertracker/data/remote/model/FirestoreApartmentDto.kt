package com.apartment.watertracker.data.remote.model

data class FirestoreApartmentDto(
    val apartmentId: String = "",
    val name: String = "",
    val createdByUserId: String = "",
    val subscriptionStatus: String = "ACTIVE",
    val subscriptionExpiresAtEpochMillis: Long? = null,
    val totalStorageCapacityLiters: Int = 100000,
)
