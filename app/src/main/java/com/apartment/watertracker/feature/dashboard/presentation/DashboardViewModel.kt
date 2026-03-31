package com.apartment.watertracker.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.model.UserRole
import com.apartment.watertracker.domain.repository.AuthRepository
import com.apartment.watertracker.domain.repository.ApartmentRepository
import com.apartment.watertracker.domain.repository.SupplyEntryRepository
import com.apartment.watertracker.domain.repository.VendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DashboardUiState(
    val userName: String = "Apartment Staff",
    val apartmentName: String = "Apartment",
    val userRole: UserRole = UserRole.OPERATOR,
    val todayTankers: Int = 0,
    val monthTankers: Int = 0,
    val activeVendors: Int = 0,
    val subscriptionActive: Boolean = true,
    val subscriptionLabel: String = "ACTIVE",
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    authRepository: AuthRepository,
    apartmentRepository: ApartmentRepository,
    private val vendorRepository: VendorRepository,
    private val supplyEntryRepository: SupplyEntryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            vendorRepository.refreshVendors()
            supplyEntryRepository.refreshTodayEntries()
            val now = LocalDate.now()
            supplyEntryRepository.refreshEntriesForMonth(now.year, now.monthValue)
        }

        val todayEntries = supplyEntryRepository.observeTodayEntries()
        val now = LocalDate.now()
        val monthEntries = supplyEntryRepository.observeEntriesForMonth(now.year, now.monthValue)
        val vendors = vendorRepository.observeVendors()
        val apartment = apartmentRepository.observeCurrentApartment()

        viewModelScope.launch {
            combine(authRepository.currentUser, apartment, todayEntries, monthEntries, vendors) { user, currentApartment, today, month, vendorList ->
                DashboardUiState(
                    userName = user?.name ?: "Apartment Staff",
                    apartmentName = currentApartment?.name ?: user?.apartmentName ?: "Apartment",
                    userRole = user?.role ?: UserRole.OPERATOR,
                    todayTankers = today.size,
                    monthTankers = month.size,
                    activeVendors = vendorList.count { it.isActive },
                    subscriptionActive = currentApartment?.isSubscriptionActive ?: true,
                    subscriptionLabel = currentApartment?.subscriptionStatus ?: "ACTIVE",
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
