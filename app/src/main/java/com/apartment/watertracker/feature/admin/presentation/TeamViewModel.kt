package com.apartment.watertracker.feature.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.model.AppUser
import com.apartment.watertracker.domain.model.OperatorInvite
import com.apartment.watertracker.domain.repository.ApartmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeamUiState(
    val inviteEmail: String = "",
    val users: List<AppUser> = emptyList(),
    val invites: List<OperatorInvite> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val apartmentRepository: ApartmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                apartmentRepository.observeApartmentUsers(),
                apartmentRepository.observeOperatorInvites(),
            ) { users, invites ->
                users to invites
            }.collect { (users, invites) ->
                _uiState.update { it.copy(users = users, invites = invites) }
            }
        }
    }

    fun updateInviteEmail(value: String) {
        _uiState.update { it.copy(inviteEmail = value, message = null) }
    }

    fun createInvite() {
        val email = _uiState.value.inviteEmail.trim()
        if (email.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null) }
            runCatching {
                apartmentRepository.createOperatorInvite(email)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        inviteEmail = "",
                        isSaving = false,
                        message = "Operator invite saved",
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = error.message ?: "Could not create invite",
                    )
                }
            }
        }
    }
}
