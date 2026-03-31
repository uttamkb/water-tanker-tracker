package com.apartment.watertracker.feature.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.model.ApartmentProfile
import com.apartment.watertracker.domain.repository.ApartmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ApartmentSwitchUiState(
    val apartments: List<ApartmentProfile> = emptyList(),
    val newApartmentName: String = "",
    val isSaving: Boolean = false,
    val subscriptionStatus: String = "ACTIVE",
    val subscriptionExpiryMillis: Long? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class ApartmentSwitchViewModel @Inject constructor(
    private val apartmentRepository: ApartmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApartmentSwitchUiState())
    val uiState: StateFlow<ApartmentSwitchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            apartmentRepository.observeMyApartments().collectLatest { list ->
                _uiState.update { it.copy(apartments = list) }
            }
        }
    }

    fun updateNewApartmentName(value: String) {
        _uiState.update { it.copy(newApartmentName = value) }
    }

    fun updateSubscriptionStatus(value: String) {
        _uiState.update { it.copy(subscriptionStatus = value) }
    }

    fun updateSubscriptionExpiryMillis(value: Long?) {
        _uiState.update { it.copy(subscriptionExpiryMillis = value) }
    }

    fun createApartment() {
        val name = _uiState.value.newApartmentName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                apartmentRepository.createApartment(name)
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, newApartmentName = "") }
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, errorMessage = error.message ?: "Could not create apartment") }
            }
        }
    }

    fun switchApartment(apartmentId: String) {
        viewModelScope.launch {
            runCatching {
                apartmentRepository.switchApartment(apartmentId)
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Could not switch apartment") }
            }
        }
    }

    fun updateSubscription(apartmentId: String) {
        val status = _uiState.value.subscriptionStatus.trim().uppercase()
        val expiryMillis = _uiState.value.subscriptionExpiryMillis
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                apartmentRepository.updateSubscription(
                    apartmentId = apartmentId,
                    status = status.ifBlank { "ACTIVE" },
                    expiresAtEpochMillis = expiryMillis,
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, errorMessage = error.message ?: "Could not update subscription") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
