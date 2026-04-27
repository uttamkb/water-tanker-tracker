package com.apartment.watertracker.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apartment.watertracker.core.notifications.NotificationHelper
import com.apartment.watertracker.domain.repository.ApartmentRepository
import com.apartment.watertracker.domain.usecase.GetSmartForecastUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@HiltWorker
class WaterLevelWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val getSmartForecastUseCase: GetSmartForecastUseCase,
    private val apartmentRepository: ApartmentRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val apartment = apartmentRepository.observeCurrentApartment().firstOrNull() ?: return Result.success()
            val forecast = getSmartForecastUseCase.execute()
            
            // Only proceed if we have enough confidence in the data
            if (forecast.confidenceScore < 0.2f || forecast.nextTankerDate == null) {
                return Result.success()
            }

            val threshold = apartment.totalStorageCapacityLiters * 0.2
            
            // If the predicted current level is already below threshold, or the next tanker date is within 2 days
            val daysUntilEmpty = ChronoUnit.DAYS.between(LocalDate.now(), forecast.nextTankerDate)
            
            if (forecast.predictedCurrentLevelLiters <= threshold || daysUntilEmpty <= 2) {
                val message = if (forecast.predictedCurrentLevelLiters <= threshold) {
                    "Water levels are critically low (Est. ${forecast.predictedCurrentLevelLiters}L remaining). Please request a tanker immediately."
                } else {
                    "Water levels are dropping. Based on usage, you will need a tanker by ${forecast.nextTankerDate.dayOfWeek}."
                }
                
                NotificationHelper.showLowWaterAlert(
                    context = context,
                    title = "Low Water Level Alert",
                    message = message
                )
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "WaterLevelWorker"
    }
}
