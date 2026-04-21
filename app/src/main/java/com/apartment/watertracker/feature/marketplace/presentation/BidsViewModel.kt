package com.apartment.watertracker.feature.marketplace.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.model.Bid
import com.apartment.watertracker.domain.repository.BidRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BidsUiState(
    val bids: List<Bid> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class BidsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bidRepository: BidRepository
) : ViewModel() {

    private val requestId: String = checkNotNull(savedStateHandle["requestId"])

    private val _uiState = MutableStateFlow(BidsUiState())
    val uiState: StateFlow<BidsUiState> = _uiState.asStateFlow()

    init {
        loadBids()
    }

    private fun loadBids() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            bidRepository.observeBidsForRequest(requestId).collect { bids ->
                _uiState.update { it.copy(bids = bids, isLoading = false) }
            }
        }
    }

    fun acceptBid(bidId: String) {
        viewModelScope.launch {
            bidRepository.acceptBid(requestId, bidId)
        }
    }

    fun rejectBid(bidId: String) {
        viewModelScope.launch {
            bidRepository.rejectBid(bidId)
        }
    }
}
