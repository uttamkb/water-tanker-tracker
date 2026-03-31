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
        WHERE vendorId = :vendorId
        ORDER BY capturedAtEpochMillis DESC
        LIMIT 1
        """,
    )
    suspend fun getLatestForVendor(vendorId: String): SupplyEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: SupplyEntryEntity)
}
