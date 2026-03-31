package com.apartment.watertracker.feature.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.repository.ApartmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ApartmentSetupUiState(
    val apartmentName: String = "",
    val isSaving: Boolean = false,
    val saveMessage: String? = null,
)

@HiltViewModel
class ApartmentSetupViewModel @Inject constructor(
    private val apartmentRepository: ApartmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApartmentSetupUiState())
    val uiState: StateFlow<ApartmentSetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            apartmentRepository.observeCurrentApartment().collect { apartment ->
                _uiState.update {
                    it.copy(apartmentName = apartment?.name ?: it.apartmentName)
                }
            }
        }
    }

    fun updateApartmentName(value: String) {
        _uiState.update { it.copy(apartmentName = value, saveMessage = null) }
    }

    fun saveApartmentName() {
        val currentName = _uiState.value.apartmentName.trim()
        if (currentName.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveMessage = null) }
            runCatching {
                apartmentRepository.updateApartmentName(currentName)
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, saveMessage = "Apartment updated") }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isSaving = false, saveMessage = error.message ?: "Could not save apartment")
                }
            }
        }
    }
}
