package com.apartment.watertracker.feature.marketplace.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.model.RequestStatus
import com.apartment.watertracker.domain.model.RequestUrgency
import com.apartment.watertracker.domain.model.TankerRequest
import com.apartment.watertracker.domain.repository.AuthRepository
import com.apartment.watertracker.domain.repository.TankerRequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class RequestTankerUiState(
    val myRequests: List<TankerRequest> = emptyList(),
    val quantityLiters: String = "5000",
    val urgency: RequestUrgency = RequestUrgency.NORMAL,
    val notes: String = "",
    val isSaving: Boolean = false,
    val showSuccess: Boolean = false
)

@HiltViewModel
class RequestTankerViewModel @Inject constructor(
    private val tankerRequestRepository: TankerRequestRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestTankerUiState())
    val uiState: StateFlow<RequestTankerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tankerRequestRepository.observeMyRequests().collect { requests ->
                _uiState.update { it.copy(myRequests = requests) }
            }
        }
    }

    fun updateQuantity(value: String) {
        _uiState.update { it.copy(quantityLiters = value) }
    }

    fun updateUrgency(value: RequestUrgency) {
        _uiState.update { it.copy(urgency = value) }
    }

    fun updateNotes(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun submitRequest() {
        viewModelScope.launch {
            val state = _uiState.value
            val user = authRepository.currentUser.first() ?: return@launch
            
            _uiState.update { it.copy(isSaving = true) }
            
            val request = TankerRequest(
                id = UUID.randomUUID().toString(),
                apartmentId = user.apartmentId,
                apartmentName = user.apartmentName ?: "My Building",
                requestedByUserId = user.id,
                quantityLiters = state.quantityLiters.toIntOrNull() ?: 5000,
                urgency = state.urgency,
                createdAt = Instant.now(),
                status = RequestStatus.OPEN,
                notes = state.notes.trim().ifBlank { null }
            )
            
            tankerRequestRepository.createRequest(request)
            
            _uiState.update { 
                it.copy(
                    isSaving = false, 
                    showSuccess = true,
                    quantityLiters = "5000",
                    notes = ""
                ) 
            }
        }
    }

    fun dismissSuccess() {
        _uiState.update { it.copy(showSuccess = false) }
    }
}
