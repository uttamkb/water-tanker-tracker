package com.apartment.watertracker.feature.auth.presentation

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.model.AppUser
import com.apartment.watertracker.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isSigningIn: Boolean = false,
    val currentUser: AppUser? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(currentUser = user, isSigningIn = false) }
            }
        }
    }

    fun signIn(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningIn = true, errorMessage = null) }
            runCatching {
                authRepository.signInWithGoogle(activity)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSigningIn = false,
                        errorMessage = error.message ?: "Google sign-in failed",
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
