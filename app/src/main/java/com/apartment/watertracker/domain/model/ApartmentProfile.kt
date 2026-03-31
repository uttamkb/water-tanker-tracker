package com.apartment.watertracker.domain.model

data class ApartmentProfile(
    val id: String,
    val name: String,
    val createdByUserId: String,
    val subscriptionStatus: String,
    val subscriptionExpiresAt: java.time.Instant?,
) {
    val isSubscriptionActive: Boolean
        get() = subscriptionStatus.uppercase() == "ACTIVE" &&
            (subscriptionExpiresAt == null || subscriptionExpiresAt.isAfter(java.time.Instant.now()))
}
