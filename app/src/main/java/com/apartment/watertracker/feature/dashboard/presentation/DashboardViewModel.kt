package com.apartment.watertracker.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.model.SupplyEntry
import com.apartment.watertracker.domain.model.UserRole
import com.apartment.watertracker.domain.repository.AuthRepository
import com.apartment.watertracker.domain.repository.ApartmentRepository
import com.apartment.watertracker.domain.repository.SupplyEntryRepository
import com.apartment.watertracker.domain.repository.VendorRepository
import com.apartment.watertracker.core.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DailyChartData(
    val label: String,
    val count: Int,
)

data class RecentDeliveryUiModel(
    val id: String,
    val vendorName: String,
    val timeAgo: String,
    val volumeLiters: Int,
    val isDuplicate: Boolean,
    val isSynced: Boolean,
    val vehicleNumber: String?,
    val hardnessPpm: Int
)

data class DashboardUiState(
    val userName: String = "Apartment Staff",
    val apartmentName: String = "Apartment",
    val userRole: UserRole = UserRole.OPERATOR,
    val todayTankers: Int = 0,
    val monthTankers: Int = 0,
    val monthVolumeLiters: Long = 0,
    val monthSpend: Double = 0.0,
    val avgPricePerLitre: Double = 0.0,
    val activeVendors: Int = 0,
    val subscriptionActive: Boolean = true,
    val subscriptionLabel: String = "ACTIVE",
    val last7DaysData: List<DailyChartData> = emptyList(),
    val recentDeliveries: List<RecentDeliveryUiModel> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val apartmentRepository: ApartmentRepository,
    private val vendorRepository: VendorRepository,
    private val supplyEntryRepository: SupplyEntryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<DashboardUiState>>(UiState.Loading)
    val uiState: StateFlow<UiState<DashboardUiState>> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val now = LocalDate.now()
        viewModelScope.launch {
            vendorRepository.refreshVendors()
            supplyEntryRepository.refreshTodayEntries()
            supplyEntryRepository.refreshEntriesForMonth(now.year, now.monthValue)
            supplyEntryRepository.refreshRecentEntries(100)
        }

        val todayEntries = supplyEntryRepository.observeTodayEntries()
        val monthEntries = supplyEntryRepository.observeEntriesForMonth(now.year, now.monthValue)
        val vendors = vendorRepository.observeVendors()
        val apartment = apartmentRepository.observeCurrentApartment()
        val recentEntries = supplyEntryRepository.observeRecentEntries(100)

        viewModelScope.launch {
            combine(
                authRepository.currentUser,
                apartment,
                todayEntries,
                monthEntries,
                vendors,
                recentEntries
            ) { args: Array<Any?> ->
                val user = args[0] as? com.apartment.watertracker.domain.model.AppUser
                val currentApartment = args[1] as? com.apartment.watertracker.domain.model.ApartmentProfile
                val today = args[2] as List<SupplyEntry>
                val month = args[3] as List<SupplyEntry>
                val vendorList = args[4] as List<com.apartment.watertracker.domain.model.Vendor>
                val recent = args[5] as List<SupplyEntry>

                val totalVolume = month.sumOf { it.volumeLiters.toLong() }
                // NOTE: Hardcoding rate for now until Payment/Bidding Phase adds actual contract rates
                val mockRatePerTanker = 600.0
                val estimatedSpend = month.size * mockRatePerTanker
                val avgPrice = if (totalVolume > 0) estimatedSpend / totalVolume else 0.0

                val state = DashboardUiState(
                    userName = user?.name ?: "Apartment Staff",
                    apartmentName = currentApartment?.name ?: user?.apartmentName ?: "Apartment",
                    userRole = user?.role ?: UserRole.OPERATOR,
                    todayTankers = today.size,
                    monthTankers = month.size,
                    monthVolumeLiters = totalVolume,
                    monthSpend = estimatedSpend,
                    avgPricePerLitre = avgPrice,
                    activeVendors = vendorList.count { it.isActive },
                    subscriptionActive = currentApartment?.isSubscriptionActive ?: true,
                    subscriptionLabel = currentApartment?.subscriptionStatus ?: "ACTIVE",
                    last7DaysData = calculateLast7Days(recent),
                    recentDeliveries = mapToRecentDeliveries(recent, vendorList)
                )
                UiState.Success(state)
            }
            .catch { e -> _uiState.value = UiState.Error(e.message ?: "Unknown Error", e) }
            .collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun mapToRecentDeliveries(entries: List<SupplyEntry>, vendors: List<com.apartment.watertracker.domain.model.Vendor>): List<RecentDeliveryUiModel> {
        return entries.take(5).map { entry ->
            val vendor = vendors.find { it.id == entry.vendorId }
            val vendorName = vendor?.supplierName ?: "Vendor ${entry.vendorId.take(4)}..."
            val timeAgo = calculateTimeAgo(entry.capturedAt)
            RecentDeliveryUiModel(
                id = entry.id,
                vendorName = vendorName,
                timeAgo = timeAgo,
                volumeLiters = entry.volumeLiters,
                isDuplicate = entry.duplicateFlag,
                isSynced = entry.isSynced,
                vehicleNumber = entry.vehicleNumber,
                hardnessPpm = entry.hardnessPpm
            )
        }
    }

    private fun calculateTimeAgo(instant: java.time.Instant): String {
        val duration = java.time.Duration.between(instant, java.time.Instant.now())
        return when {
            duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
            duration.toHours() < 24 -> "${duration.toHours()}h ago"
            else -> "${duration.toDays()}d ago"
        }
    }

    private fun calculateLast7Days(entries: List<SupplyEntry>): List<DailyChartData> {
        val formatter = DateTimeFormatter.ofPattern("E")
        val zone = ZoneId.systemDefault()
        
        return (6 downTo 0).map { daysAgo ->
            val date = LocalDate.now().minusDays(daysAgo.toLong())
            val count = entries.count { 
                it.capturedAt.atZone(zone).toLocalDate() == date 
            }
            DailyChartData(
                label = if (daysAgo == 0) "Today" else date.format(formatter),
                count = count
            )
        }
    }
}
