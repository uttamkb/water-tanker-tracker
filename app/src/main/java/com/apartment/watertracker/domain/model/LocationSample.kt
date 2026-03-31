package com.apartment.watertracker.domain.model

data class LocationSample(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val label: String,
)
