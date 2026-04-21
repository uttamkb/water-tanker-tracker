package com.apartment.watertracker.feature.vendors.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.core.qr.VendorQrPayload
import com.apartment.watertracker.core.tenant.TenantDefaults
import com.apartment.watertracker.domain.repository.AuthRepository
import com.apartment.watertracker.domain.model.Vendor
import com.apartment.watertracker.domain.model.VendorRating
import com.apartment.watertracker.domain.repository.SupplyEntryRepository
import com.apartment.watertracker.domain.repository.VendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VendorUiModel(
    val vendor: Vendor,
    val rating: VendorRating?
)

data class VendorsUiState(
    val vendors: List<VendorUiModel> = emptyList(),
    val supplierName: String = "",
    val phoneNumber: String = "",
    val contactPerson: String = "",
    val defaultCapacity: String = "5000",
    val saveMessage: String? = null,
)

@HiltViewModel
class VendorsViewModel @Inject constructor(
    private val vendorRepository: VendorRepository,
    private val authRepository: AuthRepository,
    private val supplyEntryRepository: SupplyEntryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VendorsUiState())
    val uiState: StateFlow<VendorsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            vendorRepository.refreshVendors()
            
            combine(
                vendorRepository.observeVendors(),
                supplyEntryRepository.observeVendorRatings()
            ) { vendors, ratingsMap ->
                vendors.map { vendor ->
                    VendorUiModel(
                        vendor = vendor,
                        rating = ratingsMap[vendor.id]
                    )
                }
            }.collect { vendorModels ->
                _uiState.update { it.copy(vendors = vendorModels) }
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

    fun updateDefaultCapacity(value: String) {
        _uiState.update { it.copy(defaultCapacity = value) }
    }

    fun saveVendor() {
        val current = _uiState.value
        if (current.supplierName.isBlank() || current.phoneNumber.isBlank()) return

        viewModelScope.launch {
            val currentUser = authRepository.currentUser.first()
            val apartmentId = currentUser?.apartmentId ?: TenantDefaults.DEFAULT_APARTMENT_ID
            val vendorId = UUID.randomUUID().toString()
            val capacity = current.defaultCapacity.toIntOrNull() ?: 5000

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
                    defaultCapacityLiters = capacity,
                ),
            )

            _uiState.update {
                it.copy(
                    supplierName = "",
                    phoneNumber = "",
                    contactPerson = "",
                    defaultCapacity = "5000",
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
