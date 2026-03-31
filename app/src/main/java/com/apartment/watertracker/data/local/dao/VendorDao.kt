package com.apartment.watertracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apartment.watertracker.data.local.entity.VendorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VendorDao {
    @Query("SELECT * FROM vendors ORDER BY supplierName ASC")
    fun observeAll(): Flow<List<VendorEntity>>

    @Query("SELECT * FROM vendors WHERE id = :vendorId LIMIT 1")
    fun observeById(vendorId: String): Flow<VendorEntity?>

    @Query("SELECT * FROM vendors WHERE qrValue = :qrValue LIMIT 1")
    suspend fun getByQrValue(qrValue: String): VendorEntity?

    @Query("SELECT COUNT(*) FROM vendors")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vendor: VendorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(vendors: List<VendorEntity>)

    @Query("DELETE FROM vendors WHERE id = :vendorId")
    suspend fun deleteById(vendorId: String)

    @Query("DELETE FROM vendors")
    suspend fun deleteAll()
}
