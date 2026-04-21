package com.apartment.watertracker.feature.scan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.model.Vendor
import com.apartment.watertracker.domain.repository.VendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanUiState(
    val vendors: List<Vendor> = emptyList(),
    val isResolvingScan: Boolean = false,
    val pendingVendorId: String? = null,
    val vendorToConfirm: Vendor? = null,
    val scanError: String? = null,
    val lastScannedQrValue: String? = null,
    val manualQrInput: String = "",
)

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val vendorRepository: VendorRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()
    private val recentScans: MutableMap<String, Instant> = mutableMapOf()
    private val duplicateWindow: Duration = Duration.ofMinutes(10)

    init {
        viewModelScope.launch {
            vendorRepository.refreshVendors()
            vendorRepository.observeVendors().collect { vendors ->
                _uiState.update { it.copy(vendors = vendors.filter { vendor -> vendor.isActive }) }
            }
        }
    }

    fun onQrScanned(qrValue: String) {
        val normalizedQr = qrValue.trim()
        if (normalizedQr.isBlank()) return
        if (_uiState.value.isResolvingScan) return

        // Prevent immediate re-use within the duplicate window.
        val now = Instant.now()
        cleanRecent(now)
        recentScans[normalizedQr]?.let { lastTime ->
            if (Duration.between(lastTime, now) < duplicateWindow) {
                _uiState.update {
                    it.copy(
                        scanError = "QR just used. Try again after ${duplicateWindow.toMinutes()} minutes.",
                        lastScannedQrValue = normalizedQr,
                    )
                }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isResolvingScan = true,
                    scanError = null,
                    lastScannedQrValue = normalizedQr,
                )
            }

            val vendor = vendorRepository.getVendorByQrValue(normalizedQr)
                ?: run {
                    vendorRepository.refreshVendors()
                    vendorRepository.getVendorByQrValue(normalizedQr)
                }

            _uiState.update {
                if (vendor != null) {
                    it.copy(
                        isResolvingScan = false,
                        vendorToConfirm = vendor,
                        scanError = null,
                    )
                } else {
                    it.copy(
                        isResolvingScan = false,
                        pendingVendorId = null,
                        scanError = "No vendor found for QR: $normalizedQr",
                    )
                }
            }
        }
    }

    fun confirmVendorSelection() {
        val vendor = _uiState.value.vendorToConfirm ?: return
        val qrValue = _uiState.value.lastScannedQrValue ?: return
        
        recentScans[qrValue] = Instant.now()
        _uiState.update { 
            it.copy(
                vendorToConfirm = null,
                pendingVendorId = vendor.id
            ) 
        }
    }

    fun cancelVendorSelection() {
        _uiState.update { 
            it.copy(
                vendorToConfirm = null,
                lastScannedQrValue = null
            )
        }
    }

    fun clearScanError() {
        _uiState.update { it.copy(scanError = null) }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(pendingVendorId = null) }
    }

    fun updateManualQrInput(value: String) {
        _uiState.update { it.copy(manualQrInput = value) }
    }

    fun submitManualQr() {
        val value = _uiState.value.manualQrInput.trim()
        if (value.isNotBlank()) {
            onQrScanned(value)
        }
    }

    private fun cleanRecent(now: Instant) {
        val cutoff = now.minus(duplicateWindow)
        recentScans.entries.removeIf { it.value.isBefore(cutoff) }
    }
}
