package com.apartment.watertracker.domain.usecase

import com.apartment.watertracker.domain.repository.ApartmentRepository
import com.apartment.watertracker.domain.repository.SupplyEntryRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class WaterForecast(
    val nextTankerDate: LocalDate?,
    val averageDailyUsageLiters: Long,
    val predictedCurrentLevelLiters: Long,
    val confidenceScore: Float // 0.0 to 1.0
)

class GetSmartForecastUseCase @Inject constructor(
    private val supplyEntryRepository: SupplyEntryRepository,
    private val apartmentRepository: ApartmentRepository
) {
    suspend fun execute(): WaterForecast {
        val thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli()
        val dailyVolumes = supplyEntryRepository.getDailyVolumes(thirtyDaysAgo)
        val apartment = apartmentRepository.observeCurrentApartment().firstOrNull() ?: return WaterForecast(null, 0, 0, 0f)

        if (dailyVolumes.size < 3) {
            return WaterForecast(null, 0, 0, 0f)
        }

        val totalVolume = dailyVolumes.sumOf { it.second }
        val avgUsage = totalVolume / dailyVolumes.size
        
        // Find last delivery and calculate estimated current level
        // For MVP: level = last delivery volume - (days since last delivery * avg usage)
        val lastDateStr = dailyVolumes.last().first
        val lastDate = LocalDate.parse(lastDateStr)
        val daysSinceLast = ChronoUnit.DAYS.between(lastDate, LocalDate.now())
        
        val lastDeliveryTotal = dailyVolumes.last().second
        val predictedLevel = (lastDeliveryTotal - (daysSinceLast * avgUsage)).coerceAtLeast(0)
        
        // Predict next tanker when level drops below 20%
        val threshold = apartment.totalStorageCapacityLiters * 0.2
        val daysUntilThreshold = if (avgUsage > 0) ((predictedLevel - threshold) / avgUsage).toLong() else 0L
        val predictedDate = LocalDate.now().plusDays(daysUntilThreshold.coerceAtLeast(1))

        return WaterForecast(
            nextTankerDate = predictedDate,
            averageDailyUsageLiters = avgUsage,
            predictedCurrentLevelLiters = predictedLevel,
            confidenceScore = (dailyVolumes.size / 20f).coerceAtMost(1.0f)
        )
    }
}
