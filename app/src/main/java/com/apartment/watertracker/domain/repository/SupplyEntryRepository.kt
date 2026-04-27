package com.apartment.watertracker.domain.repository

import com.apartment.watertracker.domain.model.SupplyEntry
import com.apartment.watertracker.domain.model.VendorRating
import kotlinx.coroutines.flow.Flow

interface SupplyEntryRepository {
    fun observeEntriesForMonth(year: Int, month: Int): Flow<List<SupplyEntry>>
    fun observeTodayEntries(): Flow<List<SupplyEntry>>
    fun observeRecentEntries(limit: Int): Flow<List<SupplyEntry>>
    fun observeEntryById(entryId: String): Flow<SupplyEntry?>
    fun observeVendorRatings(): Flow<Map<String, VendorRating>>
    suspend fun getDailyVolumes(sinceMillis: Long): List<Pair<String, Long>>
    suspend fun getLatestEntryForVendor(vendorId: String): SupplyEntry?
    suspend fun refreshEntriesForMonth(year: Int, month: Int)
    suspend fun refreshTodayEntries()
    suspend fun refreshRecentEntries(limit: Int)
    suspend fun saveEntry(entry: SupplyEntry)
    suspend fun syncPendingEntries()
}
