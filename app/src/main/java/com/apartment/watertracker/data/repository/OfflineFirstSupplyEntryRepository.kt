package com.apartment.watertracker.data.repository

import com.apartment.watertracker.data.local.dao.SupplyEntryDao
import com.apartment.watertracker.data.local.mapper.toDomain
import com.apartment.watertracker.data.local.mapper.toEntity as toLocalEntity
import com.apartment.watertracker.data.remote.mapper.toEntity as toRemoteEntity
import com.apartment.watertracker.data.remote.mapper.toFirestoreDto
import com.apartment.watertracker.data.remote.model.FirestoreSupplyEntryDto
import com.apartment.watertracker.data.remote.util.awaitResult
import com.apartment.watertracker.data.tenant.ApartmentScopeProvider
import com.apartment.watertracker.domain.model.SupplyEntry
import com.apartment.watertracker.domain.model.VendorRating
import com.apartment.watertracker.domain.repository.SupplyEntryRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstSupplyEntryRepository @Inject constructor(
    private val supplyEntryDao: SupplyEntryDao,
    private val firestore: FirebaseFirestore,
    private val apartmentScopeProvider: ApartmentScopeProvider,
) : SupplyEntryRepository {

    override fun observeEntriesForMonth(year: Int, month: Int): Flow<List<SupplyEntry>> {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.of(year, month, 1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = LocalDate.of(year, month, 1)
            .plusMonths(1)
            .minusDays(1)
            .atTime(23, 59, 59)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        return supplyEntryDao.observeEntriesInRange(start, end).map { entries ->
            entries.map { it.toDomain() }
        }
    }

    override fun observeTodayEntries(): Flow<List<SupplyEntry>> {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = Instant.now().toEpochMilli()

        return supplyEntryDao.observeEntriesInRange(start, end).map { entries ->
            entries.map { it.toDomain() }
        }
    }
    
    override fun observeRecentEntries(limit: Int): Flow<List<SupplyEntry>> {
        return supplyEntryDao.observeRecentEntries(limit).map { entries ->
            entries.map { it.toDomain() }
        }
    }
    
    override fun observeEntryById(entryId: String): Flow<SupplyEntry?> {
        return supplyEntryDao.observeById(entryId).map { it?.toDomain() }
    }

    override fun observeVendorRatings(): Flow<Map<String, VendorRating>> {
        return supplyEntryDao.observeVendorRatings().map { list ->
            list.associate { 
                it.vendorId to VendorRating(
                    avgQuality = it.avgQuality,
                    avgTimeliness = it.avgTimeliness,
                    avgHygiene = it.avgHygiene,
                    totalRatings = it.count
                )
            }
        }
    }

    override suspend fun getLatestEntryForVendor(vendorId: String): SupplyEntry? =
        supplyEntryDao.getLatestForVendor(vendorId)?.toDomain()

    override suspend fun getDailyVolumes(sinceMillis: Long): List<Pair<String, Long>> =
        supplyEntryDao.getDailyVolumes(sinceMillis).map { it.date to it.volume }

    override suspend fun refreshEntriesForMonth(year: Int, month: Int) {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.of(year, month, 1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = LocalDate.of(year, month, 1)
            .plusMonths(1)
            .minusDays(1)
            .atTime(23, 59, 59)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        refreshEntriesInRange(start, end)
    }

    override suspend fun refreshTodayEntries() {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = Instant.now().toEpochMilli()
        refreshEntriesInRange(start, end)
    }
    
    override suspend fun refreshRecentEntries(limit: Int) {
        val entriesCollection = entriesCollection()
        runCatching {
            val snapshot = entriesCollection
                .orderBy("capturedAtEpochMillis", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .awaitResult()

            snapshot.documents.mapNotNull { document ->
                document.toObject(FirestoreSupplyEntryDto::class.java)?.toRemoteEntity(document.id)
            }.forEach { entity ->
                supplyEntryDao.upsert(entity)
            }
        }
    }

    override suspend fun saveEntry(entry: SupplyEntry) {
        // Initially save as 'isSynced = false'
        val pendingEntry = entry.copy(isSynced = false)
        supplyEntryDao.upsert(pendingEntry.toLocalEntity())
        
        val entriesCollection = entriesCollection()
        runCatching {
            entriesCollection.document(pendingEntry.id)
                .set(pendingEntry.toLocalEntity().toFirestoreDto())
                .awaitResult()
            
            // If successful, mark as synced
            supplyEntryDao.upsert(pendingEntry.copy(isSynced = true).toLocalEntity())
        }.onFailure {
            // Background sync worker will pick this up later since isSynced = false
            it.printStackTrace()
        }
    }

    override suspend fun syncPendingEntries() {
        val pendingEntries = supplyEntryDao.getPendingEntries()
        if (pendingEntries.isEmpty()) return

        val entriesCollection = entriesCollection()
        pendingEntries.forEach { entity ->
            runCatching {
                entriesCollection.document(entity.id)
                    .set(entity.toFirestoreDto())
                    .awaitResult()
                supplyEntryDao.upsert(entity.copy(isSynced = true))
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    private suspend fun refreshEntriesInRange(start: Long, end: Long) {
        val entriesCollection = entriesCollection()
        runCatching {
            val snapshot = entriesCollection
                .whereGreaterThanOrEqualTo("capturedAtEpochMillis", start)
                .whereLessThanOrEqualTo("capturedAtEpochMillis", end)
                .get()
                .awaitResult()

            snapshot.documents.mapNotNull { document ->
                document.toObject(FirestoreSupplyEntryDto::class.java)?.toRemoteEntity(document.id)
            }.forEach { entity ->
                supplyEntryDao.upsert(entity)
            }
        }
    }

    private suspend fun entriesCollection() = firestore.collection("apartments")
        .document(apartmentScopeProvider.getApartmentId())
        .collection("supply_entries")
}
