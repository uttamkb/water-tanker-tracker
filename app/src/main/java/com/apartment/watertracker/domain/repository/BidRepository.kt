package com.apartment.watertracker.domain.repository

import com.apartment.watertracker.domain.model.Bid
import kotlinx.coroutines.flow.Flow

interface BidRepository {
    fun observeBidsForRequest(requestId: String): Flow<List<Bid>>
    suspend fun placeBid(bid: Bid)
    suspend fun acceptBid(requestId: String, bidId: String)
    suspend fun rejectBid(bidId: String)
}
