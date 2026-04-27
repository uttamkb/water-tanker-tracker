package com.apartment.watertracker.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.model.UserRole
import com.apartment.watertracker.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NavigationUserState(
    val role: UserRole? = null,
)

@HiltViewModel
class NavigationUserViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NavigationUserState())
    val uiState: StateFlow<NavigationUserState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                _uiState.update {
                    it.copy(role = user?.role)
                }
            }
        }
    }
}
