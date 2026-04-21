package com.apartment.watertracker.data.local.dao

import androidx.room.*
import com.apartment.watertracker.data.local.entity.DeliveryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryDao {
    @Query("SELECT * FROM deliveries WHERE apartmentId = :apartmentId ORDER BY timestamp DESC")
    fun observeDeliveriesByApartment(apartmentId: String): Flow<List<DeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE id = :deliveryId")
    suspend fun getById(deliveryId: String): DeliveryEntity?

    @Query("SELECT * FROM deliveries WHERE vendorId = :vendorId AND apartmentId = :apartmentId")
    suspend fun getDeliveriesByVendor(vendorId: String, apartmentId: String): List<DeliveryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(delivery: DeliveryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(deliveries: List<DeliveryEntity>)

    @Query("DELETE FROM deliveries WHERE id = :deliveryId")
    suspend fun deleteById(deliveryId: String)

    @Query("DELETE FROM deliveries WHERE apartmentId = :apartmentId")
    suspend fun deleteAllByApartment(apartmentId: String)
}
