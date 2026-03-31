package com.apartment.watertracker.domain.usecase

import com.apartment.watertracker.domain.model.SupplyEntry
import java.time.Duration
import java.time.Instant

class CheckDuplicateEntryUseCase {

    fun execute(
        previousEntry: SupplyEntry?,
        candidateCapturedAt: Instant,
        duplicateWindowMinutes: Long = 60,
    ): Boolean {
        if (previousEntry == null) return false

        val delta = Duration.between(previousEntry.capturedAt, candidateCapturedAt).abs()
        return delta.toMinutes() <= duplicateWindowMinutes
    }
}
