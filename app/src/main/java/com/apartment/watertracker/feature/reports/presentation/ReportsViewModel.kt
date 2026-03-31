package com.apartment.watertracker.feature.reports.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.model.Vendor
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

data class VendorMonthlySummary(
    val vendorName: String,
    val tankerCount: Int,
)

data class ReportsUiState(
    val monthLabel: String = "",
    val totalTankers: Int = 0,
    val duplicateFlags: Int = 0,
    val vendorSummaries: List<VendorMonthlySummary> = emptyList(),
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val vendorRepository: VendorRepository,
    private val supplyEntryRepository: SupplyEntryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        val now = LocalDate.now()

        viewModelScope.launch {
            vendorRepository.refreshVendors()
            supplyEntryRepository.refreshEntriesForMonth(now.year, now.monthValue)
        }

        viewModelScope.launch {
            combine(
                vendorRepository.observeVendors(),
                supplyEntryRepository.observeEntriesForMonth(now.year, now.monthValue),
            ) { vendors, entries ->
                ReportsUiState(
                    monthLabel = "${now.month.name.lowercase().replaceFirstChar(Char::uppercase)} ${now.year}",
                    totalTankers = entries.size,
                    duplicateFlags = entries.count { it.duplicateFlag },
                    vendorSummaries = buildSummaries(vendors, entries.map { it.vendorId }),
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun buildSummaries(vendors: List<Vendor>, entryVendorIds: List<String>): List<VendorMonthlySummary> {
        return vendors.map { vendor ->
            VendorMonthlySummary(
                vendorName = vendor.supplierName,
                tankerCount = entryVendorIds.count { it == vendor.id },
            )
        }.filter { it.tankerCount > 0 }
    }
}
