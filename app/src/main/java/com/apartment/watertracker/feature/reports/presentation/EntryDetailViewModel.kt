package com.apartment.watertracker.feature.reports.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.model.SupplyEntry
import com.apartment.watertracker.domain.model.Vendor
import com.apartment.watertracker.domain.repository.SupplyEntryRepository
import com.apartment.watertracker.domain.repository.VendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EntryDetailUiState(
    val isLoading: Boolean = true,
    val entry: SupplyEntry? = null,
    val vendor: Vendor? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class EntryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val supplyEntryRepository: SupplyEntryRepository,
    private val vendorRepository: VendorRepository,
) : ViewModel() {

    private val entryId: String = checkNotNull(savedStateHandle["entryId"])
    private val _uiState = MutableStateFlow(EntryDetailUiState())
    val uiState: StateFlow<EntryDetailUiState> = _uiState.asStateFlow()

    init {
        loadEntryDetails()
    }

    private fun loadEntryDetails() {
        viewModelScope.launch {
            supplyEntryRepository.observeEntryById(entryId).collect { entry ->
                if (entry != null) {
                    // Once we have the entry, load its associated vendor
                    vendorRepository.observeVendor(entry.vendorId).collect { vendor ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                entry = entry, 
                                vendor = vendor
                            ) 
                        }
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = "Entry not found"
                        ) 
                    }
                }
            }
        }
    }
}
