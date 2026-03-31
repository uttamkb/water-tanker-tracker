package com.apartment.watertracker.domain.repository

import com.apartment.watertracker.domain.model.LocationSample

interface LocationRepository {
    suspend fun captureCurrentLocation(): LocationSample
}
