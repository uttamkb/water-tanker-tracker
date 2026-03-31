package com.apartment.watertracker.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.apartment.watertracker.data.remote.util.awaitResult
import com.apartment.watertracker.domain.model.LocationSample
import com.apartment.watertracker.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidLocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
) : LocationRepository {

    override suspend fun captureCurrentLocation(): LocationSample {
        if (!hasLocationPermission()) {
            throw IllegalStateException("Location permission is required")
        }

        if (!isLocationEnabled()) {
            throw IllegalStateException("Turn on GPS/location services")
        }

        val cancellationTokenSource = CancellationTokenSource()
        val location = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token,
        ).awaitResult()

        if (location == null) {
            throw IllegalStateException("Could not capture current location")
        }

        return LocationSample(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = location.accuracy,
            label = "Live device location",
        )
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}
