package com.apartment.watertracker.domain.model

data class ApartmentProfile(
    val id: String,
    val name: String,
    val createdByUserId: String,
    val subscriptionStatus: String,
    val subscriptionExpiresAt: java.time.Instant?,
    val totalStorageCapacityLiters: Int = 100000, // Default 100k Liters
) {
    val isSubscriptionActive: Boolean
        get() = subscriptionStatus.uppercase() == "ACTIVE" &&
            (subscriptionExpiresAt == null || subscriptionExpiresAt.isAfter(java.time.Instant.now()))
}
