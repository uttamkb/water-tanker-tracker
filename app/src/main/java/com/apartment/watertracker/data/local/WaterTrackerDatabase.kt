package com.apartment.watertracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.apartment.watertracker.data.local.dao.SupplyEntryDao
import com.apartment.watertracker.data.local.dao.VendorDao
import com.apartment.watertracker.data.local.entity.SupplyEntryEntity
import com.apartment.watertracker.data.local.entity.VendorEntity

@Database(
    entities = [VendorEntity::class, SupplyEntryEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class WaterTrackerDatabase : RoomDatabase() {
    abstract fun vendorDao(): VendorDao
    abstract fun supplyEntryDao(): SupplyEntryDao
}
