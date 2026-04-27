package com.apartment.watertracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apartment.watertracker.data.local.entity.SupplyEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplyEntryDao {
    @Query(
        """
        SELECT * FROM supply_entries
        WHERE capturedAtEpochMillis BETWEEN :startInclusive AND :endInclusive
        ORDER BY capturedAtEpochMillis DESC
        """,
    )
    fun observeEntriesInRange(startInclusive: Long, endInclusive: Long): Flow<List<SupplyEntryEntity>>

    @Query(
        """
        SELECT * FROM supply_entries
        ORDER BY capturedAtEpochMillis DESC
        LIMIT :limit
        """
    )
    fun observeRecentEntries(limit: Int): Flow<List<SupplyEntryEntity>>
    
    @Query("SELECT * FROM supply_entries WHERE id = :entryId LIMIT 1")
    fun observeById(entryId: String): Flow<SupplyEntryEntity?>

    @Query(
        """
        SELECT * FROM supply_entries
        WHERE vendorId = :vendorId
        ORDER BY capturedAtEpochMillis DESC
        LIMIT 1
        """,
    )
    suspend fun getLatestForVendor(vendorId: String): SupplyEntryEntity?

    @Query("SELECT * FROM supply_entries WHERE isSynced = 0")
    suspend fun getPendingEntries(): List<SupplyEntryEntity>

    data class DeliveryAggregate(
        val count: Int,
        val totalVolumeLiters: Long
    )

    @Query(
        """
        SELECT COUNT(*) as count, COALESCE(SUM(volumeLiters), 0) as totalVolumeLiters
        FROM supply_entries
        WHERE vendorId = :vendorId AND capturedAtEpochMillis BETWEEN :startInclusive AND :endInclusive
        """
    )
    suspend fun getAggregateForVendorInRange(vendorId: String, startInclusive: Long, endInclusive: Long): DeliveryAggregate

    data class VendorRatingAggregate(
        val vendorId: String,
        val avgQuality: Float,
        val avgTimeliness: Float,
        val avgHygiene: Float,
        val count: Int
    )

    @Query(
        """
        SELECT vendorId, 
               AVG(qualityRating) as avgQuality, 
               AVG(timelinessRating) as avgTimeliness, 
               AVG(hygieneRating) as avgHygiene, 
               COUNT(*) as count
        FROM supply_entries 
        WHERE qualityRating IS NOT NULL OR timelinessRating IS NOT NULL OR hygieneRating IS NOT NULL
        GROUP BY vendorId
        """
    )
    fun observeVendorRatings(): Flow<List<VendorRatingAggregate>>

    @Query(
        """
        SELECT DATE(capturedAtEpochMillis / 1000, 'unixepoch') as date, SUM(volumeLiters) as volume
        FROM supply_entries
        WHERE capturedAtEpochMillis >= :sinceMillis
        GROUP BY date
        ORDER BY date ASC
        """
    )
    suspend fun getDailyVolumes(sinceMillis: Long): List<DailyVolume>

    data class DailyVolume(
        val date: String,
        val volume: Long
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: SupplyEntryEntity)
}
