package com.apartment.watertracker.feature.reports.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.model.Vendor
import com.apartment.watertracker.domain.repository.SupplyEntryRepository
import com.apartment.watertracker.domain.repository.VendorRepository
import com.apartment.watertracker.domain.usecase.GenerateAuditLogCsvUseCase
import com.apartment.watertracker.domain.usecase.GeneratePdfReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VendorMonthlySummary(
    val vendorId: String,
    val vendorName: String,
    val tankerCount: Int,
    val totalVolumeLiters: Long,
    val totalSpend: Double,
    val avgPricePerLitre: Double
)

data class ReportEntryDetail(
    val id: String,
    val timeString: String,
    val dateString: String,
    val vendorName: String,
    val vendorId: String,
    val vehicleNumber: String,
    val hardness: Int,
    val volume: Int,
    val isDuplicate: Boolean,
)

data class ReportsUiState(
    val monthLabel: String = "",
    val totalTankers: Int = 0,
    val totalVolumeLiters: Long = 0,
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
    private val generatePdfReportUseCase: GeneratePdfReportUseCase,
    private val generateAuditLogCsvUseCase: GenerateAuditLogCsvUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private val _shareFileEvent = MutableSharedFlow<File>()
    val shareFileEvent = _shareFileEvent.asSharedFlow()

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
                        volume = entry.volumeLiters,
                        isDuplicate = entry.duplicateFlag
                    )
                }

                ReportsUiState(
                    monthLabel = "${now.month.name.lowercase().replaceFirstChar(Char::uppercase)} ${now.year}",
                    totalTankers = entries.size,
                    totalVolumeLiters = entries.sumOf { it.volumeLiters.toLong() },
                    duplicateFlags = entries.count { it.duplicateFlag },
                    vendorSummaries = buildSummaries(vendors, entries),
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

    private fun buildSummaries(vendors: List<Vendor>, entries: List<com.apartment.watertracker.domain.model.SupplyEntry>): List<VendorMonthlySummary> {
        return vendors.map { vendor ->
            val vendorEntries = entries.filter { it.vendorId == vendor.id }
            val totalVolume = vendorEntries.sumOf { it.volumeLiters.toLong() }
            
            // NOTE: Using the same mock pricing from Dashboard (₹600 per tanker) until Contract Pricing is implemented.
            val estimatedSpend = vendorEntries.size * 600.0
            val avgPrice = if (totalVolume > 0) estimatedSpend / totalVolume else 0.0
            
            VendorMonthlySummary(
                vendorId = vendor.id,
                vendorName = vendor.supplierName,
                tankerCount = vendorEntries.size,
                totalVolumeLiters = totalVolume,
                totalSpend = estimatedSpend,
                avgPricePerLitre = avgPrice
            )
        }.filter { it.tankerCount > 0 }
    }

    fun exportCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportMessage = null) }
            try {
                val now = LocalDate.now()
                val entries = supplyEntryRepository.observeEntriesForMonth(now.year, now.monthValue).first()
                val file = generateAuditLogCsvUseCase.execute(entries, "${now.year}-${now.monthValue}")
                _shareFileEvent.emit(file)
                _uiState.update { it.copy(isExporting = false, exportMessage = "CSV Generated") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, exportMessage = "Export failed: ${e.message}") }
            }
        }
    }
    
    fun exportPdf() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportMessage = null) }
            try {
                val state = _uiState.value
                val file = generatePdfReportUseCase.execute(
                    monthLabel = state.monthLabel,
                    totalTankers = state.totalTankers,
                    totalVolumeLiters = state.totalVolumeLiters,
                    vendorSummaries = state.vendorSummaries,
                    dailyEntries = state.dailyEntries
                )
                if (file != null) {
                    _shareFileEvent.emit(file)
                    _uiState.update { it.copy(isExporting = false, exportMessage = "PDF Generated") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, exportMessage = "Export failed: ${e.message}") }
            }
        }
    }
    
    fun clearExportMessage() {
        _uiState.update { it.copy(exportMessage = null) }
    }
}
