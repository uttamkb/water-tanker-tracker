package com.apartment.watertracker.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.repository.ApartmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubscriptionGuardState(
    val isActive: Boolean = true,
    val apartmentName: String = "",
)

@HiltViewModel
class SubscriptionGuardViewModel @Inject constructor(
    private val apartmentRepository: ApartmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionGuardState())
    val uiState: StateFlow<SubscriptionGuardState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            apartmentRepository.observeCurrentApartment().collectLatest { apartment ->
                _uiState.update {
                    it.copy(
                        isActive = apartment?.isSubscriptionActive ?: true,
                        apartmentName = apartment?.name.orEmpty(),
                    )
                }
            }
        }
    }
}
