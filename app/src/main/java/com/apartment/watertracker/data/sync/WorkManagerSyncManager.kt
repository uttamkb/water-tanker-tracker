package com.apartment.watertracker.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.apartment.watertracker.domain.sync.SyncManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerSyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) : SyncManager {

    private val workManager = WorkManager.getInstance(context)
    
    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    override fun startPeriodicSync() {
        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
        
        // Smart Forecasting Low-Water Alert Worker (Runs once a day)
        val periodicLevelCheckRequest = PeriodicWorkRequestBuilder<WaterLevelWorker>(24, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().build()) // No network required
            .build()
            
        workManager.enqueueUniquePeriodicWork(
            WaterLevelWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicLevelCheckRequest
        )
    }

    override fun stopPeriodicSync() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME)
        workManager.cancelUniqueWork(WaterLevelWorker.WORK_NAME)
    }

    override fun triggerImmediateSync() {
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "${SyncWorker.WORK_NAME}_immediate",
            ExistingWorkPolicy.REPLACE,
            oneTimeWorkRequest
        )
    }
}
