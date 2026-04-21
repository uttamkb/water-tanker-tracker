package com.apartment.watertracker.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apartment.watertracker.domain.repository.DeliveryRepository
import com.apartment.watertracker.domain.repository.VendorRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncWorkerEntryPoint {
        fun vendorRepository(): VendorRepository
        fun deliveryRepository(): DeliveryRepository
        fun supplyEntryRepository(): com.apartment.watertracker.domain.repository.SupplyEntryRepository
    }

    override suspend fun doWork(): Result {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                SyncWorkerEntryPoint::class.java
            )
            
            val vendorRepo = entryPoint.vendorRepository()
            val deliveryRepo = entryPoint.deliveryRepository()
            val supplyRepo = entryPoint.supplyEntryRepository()

            // Perform synchronization
            vendorRepo.refreshVendors()
            deliveryRepo.refreshDeliveries()
            supplyRepo.syncPendingEntries()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "WaterTrackerSyncWorker"
    }
}
