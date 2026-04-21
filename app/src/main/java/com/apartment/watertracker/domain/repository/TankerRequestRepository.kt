package com.apartment.watertracker.domain.repository

import com.apartment.watertracker.domain.model.TankerRequest
import kotlinx.coroutines.flow.Flow

interface TankerRequestRepository {
    fun observeMyRequests(): Flow<List<TankerRequest>>
    fun observeOpenRequests(): Flow<List<TankerRequest>>
    suspend fun createRequest(request: TankerRequest)
    suspend fun cancelRequest(requestId: String)
    suspend fun markAsFulfilled(requestId: String)
}
