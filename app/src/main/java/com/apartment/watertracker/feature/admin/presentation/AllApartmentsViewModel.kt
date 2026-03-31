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

data class AllApartmentsUiState(
    val apartments: List<ApartmentProfile> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class AllApartmentsViewModel @Inject constructor(
    private val apartmentRepository: ApartmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllApartmentsUiState())
    val uiState: StateFlow<AllApartmentsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            apartmentRepository.observeAllApartments().collectLatest { list ->
                _uiState.update { it.copy(apartments = list) }
            }
        }
    }

    fun updateSubscription(apartmentId: String, status: String, expiresMillis: Long?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                apartmentRepository.updateSubscription(apartmentId, status, expiresMillis)
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, errorMessage = error.message ?: "Could not update") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
