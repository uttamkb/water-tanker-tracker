package com.apartment.watertracker.feature.vendors.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.core.qr.VendorQrPayload
import com.apartment.watertracker.core.tenant.TenantDefaults
import com.apartment.watertracker.domain.repository.AuthRepository
import com.apartment.watertracker.domain.model.Vendor
import com.apartment.watertracker.domain.repository.VendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VendorsUiState(
    val vendors: List<Vendor> = emptyList(),
    val supplierName: String = "",
    val phoneNumber: String = "",
    val contactPerson: String = "",
    val saveMessage: String? = null,
)

@HiltViewModel
class VendorsViewModel @Inject constructor(
    private val vendorRepository: VendorRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VendorsUiState())
    val uiState: StateFlow<VendorsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            vendorRepository.refreshVendors()
            vendorRepository.observeVendors().collect { vendors ->
                _uiState.update { it.copy(vendors = vendors) }
            }
        }
    }

    fun updateSupplierName(value: String) {
        _uiState.update { it.copy(supplierName = value) }
    }

    fun updatePhoneNumber(value: String) {
        _uiState.update { it.copy(phoneNumber = value) }
    }

    fun updateContactPerson(value: String) {
        _uiState.update { it.copy(contactPerson = value) }
    }

    fun saveVendor() {
        val current = _uiState.value
        if (current.supplierName.isBlank() || current.phoneNumber.isBlank()) return

        viewModelScope.launch {
            val currentUser = authRepository.currentUser.first()
            val apartmentId = currentUser?.apartmentId ?: TenantDefaults.DEFAULT_APARTMENT_ID
            val vendorId = UUID.randomUUID().toString()

            vendorRepository.saveVendor(
                Vendor(
                    id = vendorId,
                    apartmentId = apartmentId,
                    supplierName = current.supplierName.trim(),
                    contactPerson = current.contactPerson.trim().ifBlank { null },
                    phoneNumber = current.phoneNumber.trim(),
                    alternatePhoneNumber = null,
                    address = null,
                    notes = null,
                    isActive = true,
                    qrValue = VendorQrPayload.build(apartmentId, vendorId),
                ),
            )

            _uiState.update {
                it.copy(
                    supplierName = "",
                    phoneNumber = "",
                    contactPerson = "",
                    saveMessage = "Vendor saved",
                )
            }
        }
    }

    fun deleteVendor(vendorId: String) {
        viewModelScope.launch {
            vendorRepository.deleteVendor(vendorId)
            _uiState.update { it.copy(saveMessage = "Vendor deleted") }
        }
    }

    fun clearSaveMessage() {
        _uiState.update { it.copy(saveMessage = null) }
    }
}
