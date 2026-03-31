package com.apartment.watertracker.domain.repository

import com.apartment.watertracker.domain.model.SupplyEntry
import kotlinx.coroutines.flow.Flow

interface SupplyEntryRepository {
    fun observeEntriesForMonth(year: Int, month: Int): Flow<List<SupplyEntry>>
    fun observeTodayEntries(): Flow<List<SupplyEntry>>
    suspend fun getLatestEntryForVendor(vendorId: String): SupplyEntry?
    suspend fun refreshEntriesForMonth(year: Int, month: Int)
    suspend fun refreshTodayEntries()
    suspend fun saveEntry(entry: SupplyEntry)
}
