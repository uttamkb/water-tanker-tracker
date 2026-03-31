package com.apartment.watertracker.feature.entries.presentation

import androidx.lifecycle.SavedStateHandle
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DuplicateWarningState(
    val previousCapturedAt: String,
    val previousVehicleNumber: String?,
)

data class SupplyEntryUiState(
    val vendor: Vendor? = null,
    val capturedAt: Instant = Instant.now(),
    val location: LocationSample? = null,
    val isCapturingLocation: Boolean = false,
    val locationError: String? = null,
    val hardnessInput: String = "",
    val vehicleNumber: String = "",
    val remarks: String = "",
    val isSaving: Boolean = false,
    val duplicateWarning: DuplicateWarningState? = null,
    val entrySaved: Boolean = false,
)

@HiltViewModel
class SupplyEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vendorRepository: VendorRepository,
    private val supplyEntryRepository: SupplyEntryRepository,
    private val locationRepository: LocationRepository,
    private val authRepository: AuthRepository,
    private val checkDuplicateEntryUseCase: CheckDuplicateEntryUseCase,
) : ViewModel() {

    private val vendorId: String = checkNotNull(savedStateHandle["vendorId"])
    private val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
    private val _uiState = MutableStateFlow(SupplyEntryUiState())
    val uiState: StateFlow<SupplyEntryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            vendorRepository.refreshVendors()
            vendorRepository.observeVendor(vendorId).collect { vendor ->
                _uiState.update { it.copy(vendor = vendor) }
            }
        }

        captureLocation()
    }

    fun updateHardness(value: String) {
        _uiState.update { it.copy(hardnessInput = value) }
    }

    fun updateVehicleNumber(value: String) {
        _uiState.update { it.copy(vehicleNumber = value) }
    }

    fun updateRemarks(value: String) {
        _uiState.update { it.copy(remarks = value) }
    }

    fun dismissDuplicateWarning() {
        _uiState.update { it.copy(duplicateWarning = null, isSaving = false) }
    }

    fun saveEntry(forceSave: Boolean = false) {
        viewModelScope.launch {
            val state = _uiState.value
            val vendor = state.vendor ?: return@launch
            val location = state.location ?: return@launch
            val hardness = state.hardnessInput.toIntOrNull() ?: return@launch

            _uiState.update { it.copy(isSaving = true) }

            val latestEntry = supplyEntryRepository.getLatestEntryForVendor(vendor.id)
            val isDuplicate = checkDuplicateEntryUseCase.execute(
                previousEntry = latestEntry,
                candidateCapturedAt = state.capturedAt,
            )

            if (isDuplicate && !forceSave) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        duplicateWarning = DuplicateWarningState(
                            previousCapturedAt = latestEntry?.capturedAt
                                ?.atZone(ZoneId.systemDefault())
                                ?.format(formatter)
                                ?: "Unknown",
                            previousVehicleNumber = latestEntry?.vehicleNumber,
                        ),
                    )
                }
                return@launch
            }

            val currentUser = authRepository.currentUser.first()
            supplyEntryRepository.saveEntry(
                SupplyEntry(
                    id = UUID.randomUUID().toString(),
                    apartmentId = vendor.apartmentId,
                    vendorId = vendor.id,
                    hardnessPpm = hardness,
                    capturedAt = state.capturedAt,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    gpsAccuracyMeters = location.accuracyMeters,
                    vehicleNumber = state.vehicleNumber.trim().ifBlank { null },
                    remarks = state.remarks.trim().ifBlank { null },
                    photoUrl = null,
                    duplicateFlag = isDuplicate,
                    duplicateReferenceId = latestEntry?.id.takeIf { isDuplicate },
                    createdByUserId = currentUser?.id ?: "local-admin",
                ),
            )

            _uiState.update { it.copy(isSaving = false, duplicateWarning = null, entrySaved = true) }
        }
    }

    fun consumeEntrySaved() {
        _uiState.update { it.copy(entrySaved = false) }
    }

    fun onLocationPermissionDenied() {
        _uiState.update {
            it.copy(
                location = null,
                isCapturingLocation = false,
                locationError = "Location permission is required",
            )
        }
    }

    fun recaptureLocation() {
        captureLocation()
    }

    private fun captureLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCapturingLocation = true, locationError = null) }
            runCatching {
                locationRepository.captureCurrentLocation()
            }.onSuccess { location ->
                _uiState.update {
                    it.copy(
                        capturedAt = Instant.now(),
                        location = location,
                        isCapturingLocation = false,
                        locationError = null,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        location = null,
                        isCapturingLocation = false,
                        locationError = error.message ?: "Could not capture location",
                    )
                }
            }
        }
    }
}
