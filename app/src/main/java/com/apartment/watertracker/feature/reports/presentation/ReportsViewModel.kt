package com.apartment.watertracker.feature.reports.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.model.Vendor
import com.apartment.watertracker.domain.repository.SupplyEntryRepository
import com.apartment.watertracker.domain.repository.VendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VendorMonthlySummary(
    val vendorId: String,
    val vendorName: String,
    val tankerCount: Int,
)

data class ReportEntryDetail(
    val id: String,
    val timeString: String,
    val dateString: String,
    val vendorName: String,
    val vendorId: String,
    val vehicleNumber: String,
    val hardness: Int,
    val isDuplicate: Boolean,
)

data class ReportsUiState(
    val monthLabel: String = "",
    val totalTankers: Int = 0,
    val duplicateFlags: Int = 0,
    val vendorSummaries: List<VendorMonthlySummary> = emptyList(),
    val dailyEntries: Map<String, List<ReportEntryDetail>> = emptyMap(),
    val isExporting: Boolean = false,
    val exportMessage: String? = null,
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
                val vendorMap = vendors.associateBy { it.id }
                val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy").withZone(ZoneId.systemDefault())
                val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a").withZone(ZoneId.systemDefault())

                val entryDetails = entries.sortedByDescending { it.capturedAt }.map { entry ->
                    ReportEntryDetail(
                        id = entry.id,
                        timeString = timeFormatter.format(entry.capturedAt),
                        dateString = dateFormatter.format(entry.capturedAt),
                        vendorName = vendorMap[entry.vendorId]?.supplierName ?: "Unknown Vendor",
                        vendorId = entry.vendorId,
                        vehicleNumber = entry.vehicleNumber?.ifBlank { "N/A" } ?: "N/A",
                        hardness = entry.hardnessPpm,
                        isDuplicate = entry.duplicateFlag
                    )
                }

                ReportsUiState(
                    monthLabel = "${now.month.name.lowercase().replaceFirstChar(Char::uppercase)} ${now.year}",
                    totalTankers = entries.size,
                    duplicateFlags = entries.count { it.duplicateFlag },
                    vendorSummaries = buildSummaries(vendors, entries.map { it.vendorId }),
                    dailyEntries = entryDetails.groupBy { it.dateString }
                )
            }.collect { state ->
                _uiState.update { current ->
                    state.copy(
                        isExporting = current.isExporting,
                        exportMessage = current.exportMessage
                    )
                }
            }
        }
    }

    private fun buildSummaries(vendors: List<Vendor>, entryVendorIds: List<String>): List<VendorMonthlySummary> {
        return vendors.map { vendor ->
            VendorMonthlySummary(
                vendorId = vendor.id,
                vendorName = vendor.supplierName,
                tankerCount = entryVendorIds.count { it == vendor.id },
            )
        }.filter { it.tankerCount > 0 }
    }

    suspend fun generateCsvData(): String {
        _uiState.update { it.copy(isExporting = true, exportMessage = null) }
        try {
            val now = LocalDate.now()
            val entries = supplyEntryRepository.observeEntriesForMonth(now.year, now.monthValue).first()
            val vendors = vendorRepository.observeVendors().first()
            val vendorMap = vendors.associateBy { it.id }

            val sb = StringBuilder()
            // Header
            sb.append("Date,Time,Vendor,Vehicle Number,Hardness (PPM),Is Duplicate,Remarks\n")
            
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd,HH:mm:ss").withZone(ZoneId.systemDefault())

            entries.sortedBy { it.capturedAt }.forEach { entry ->
                val vendorName = vendorMap[entry.vendorId]?.supplierName?.replace(",", " ") ?: "Unknown Vendor"
                val dateTime = formatter.format(entry.capturedAt)
                val vehicle = entry.vehicleNumber?.replace(",", " ") ?: ""
                val remarks = entry.remarks?.replace(",", " ") ?: ""
                
                sb.append("$dateTime,$vendorName,$vehicle,${entry.hardnessPpm},${entry.duplicateFlag},$remarks\n")
            }
            
            _uiState.update { it.copy(isExporting = false, exportMessage = "Export generated successfully") }
            return sb.toString()
        } catch (e: Exception) {
            _uiState.update { it.copy(isExporting = false, exportMessage = "Export failed: ${e.message}") }
            return ""
        }
    }
    
    fun clearExportMessage() {
        _uiState.update { it.copy(exportMessage = null) }
    }
}
