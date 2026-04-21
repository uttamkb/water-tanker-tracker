package com.apartment.watertracker.feature.analytics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.repository.SupplyEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AnalyticsUiState(
    val phTrends: List<Float> = emptyList(),
    val tdsTrends: List<Float> = emptyList(),
    val hardnessTrends: List<Float> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val supplyEntryRepository: SupplyEntryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val now = LocalDate.now()
            supplyEntryRepository.observeEntriesForMonth(now.year, now.monthValue).collect { entries ->
                val sortedEntries = entries.sortedBy { it.capturedAt }
                _uiState.value = AnalyticsUiState(
                    phTrends = sortedEntries.mapNotNull { it.phLevel?.toFloat() },
                    tdsTrends = sortedEntries.mapNotNull { it.tdsPpm?.toFloat() },
                    hardnessTrends = sortedEntries.map { it.hardnessPpm.toFloat() },
                    isLoading = false
                )
            }
        }
    }
}
