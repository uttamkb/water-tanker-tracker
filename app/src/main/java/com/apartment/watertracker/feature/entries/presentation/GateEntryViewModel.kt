package com.apartment.watertracker.feature.entries.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.model.LocationSample
import com.apartment.watertracker.domain.model.SupplyEntry
import com.apartment.watertracker.domain.model.Vendor
import com.apartment.watertracker.domain.repository.AuthRepository
import com.apartment.watertracker.domain.repository.LocationRepository
import com.apartment.watertracker.domain.repository.SupplyEntryRepository
import com.apartment.watertracker.domain.repository.VendorRepository
import com.apartment.watertracker.domain.usecase.CheckDuplicateEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

data class GateEntryUiState(
    val isScanning: Boolean = false,
    val vendor: Vendor? = null,
    val tdsLevel: String = "",
    val gpsLocation: LocationSample? = null,
    val isCapturingLocation: Boolean = false,
    val showDuplicateAlert: Boolean = false,
    val isSaving: Boolean = false,
    val entrySaved: Boolean = false,
    val lastError: String? = null,
    val previousEntryTime: String? = null
)

@HiltViewModel
class GateEntryViewModel @Inject constructor(
    private val vendorRepository: VendorRepository,
    private val supplyEntryRepository: SupplyEntryRepository,
    private val locationRepository: LocationRepository,
    private val authRepository: AuthRepository,
    private val checkDuplicateEntryUseCase: CheckDuplicateEntryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GateEntryUiState())
    val uiState: StateFlow<GateEntryUiState> = _uiState.asStateFlow()
    
    private val formatter = DateTimeFormatter.ofPattern("hh:mm a")

    fun startScanning() {
        _uiState.update { it.copy(isScanning = true, vendor = null, tdsLevel = "") }
    }

    fun onQrScanned(qrValue: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = false) }
            // Simulating QR to Vendor resolution (In real app, parse qrValue for vendorId)
            val vendorId = qrValue // Assuming the QR value IS the vendor ID for now
            vendorRepository.observeVendor(vendorId).first()?.let { vendor ->
                _uiState.update { it.copy(vendor = vendor) }
                captureLocation()
            } ?: run {
                _uiState.update { it.copy(lastError = "Invalid Vendor QR") }
            }
        }
    }

    fun updateTdsLevel(value: String) {
        _uiState.update { it.copy(tdsLevel = value) }
    }

    fun saveEntry(forceSave: Boolean = false) {
        val currentState = _uiState.value
        val vendor = currentState.vendor ?: return
        val location = currentState.gpsLocation ?: return
        
        if (currentState.tdsLevel.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val latestEntry = supplyEntryRepository.getLatestEntryForVendor(vendor.id)
            val isDuplicate = checkDuplicateEntryUseCase.execute(
                previousEntry = latestEntry,
                candidateCapturedAt = Instant.now(),
                candidateVendorId = vendor.id
            )

            if (isDuplicate && !forceSave) {
                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        showDuplicateAlert = true,
                        previousEntryTime = latestEntry?.capturedAt?.atZone(ZoneId.systemDefault())?.format(formatter)
                    ) 
                }
                return@launch
            }

            val user = authRepository.currentUser.first()
            val entry = SupplyEntry(
                id = UUID.randomUUID().toString(),
                apartmentId = vendor.apartmentId,
                vendorId = vendor.id,
                hardnessPpm = 0, // Default for now
                phLevel = null,
                tdsPpm = currentState.tdsLevel.toIntOrNull() ?: 0,
                volumeLiters = vendor.defaultCapacityLiters,
                capturedAt = Instant.now(),
                latitude = location.latitude,
                longitude = location.longitude,
                gpsAccuracyMeters = location.accuracyMeters,
                vehicleNumber = null,
                remarks = "Gate Entry Scan",
                photoUrl = null,
                duplicateFlag = isDuplicate,
                duplicateReferenceId = latestEntry?.id.takeIf { isDuplicate },
                createdByUserId = user?.id ?: "security_guard",
                qualityRating = null,
                timelinessRating = null,
                hygieneRating = null,
                isSynced = false
            )

            supplyEntryRepository.saveEntry(entry)
            _uiState.update { it.copy(isSaving = false, entrySaved = true, vendor = null, tdsLevel = "") }
        }
    }

    fun dismissDuplicateAlert() {
        _uiState.update { it.copy(showDuplicateAlert = false) }
    }

    fun resetState() {
        _uiState.update { GateEntryUiState() }
    }

    private fun captureLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCapturingLocation = true) }
            runCatching {
                locationRepository.captureCurrentLocation()
            }.onSuccess { loc ->
                _uiState.update { it.copy(gpsLocation = loc, isCapturingLocation = false) }
            }.onFailure {
                _uiState.update { it.copy(isCapturingLocation = false, lastError = "GPS Failed") }
            }
        }
    }
}
