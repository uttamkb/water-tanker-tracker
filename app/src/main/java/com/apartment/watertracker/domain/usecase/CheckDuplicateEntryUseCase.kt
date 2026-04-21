package com.apartment.watertracker.domain.usecase

import com.apartment.watertracker.domain.model.SupplyEntry
import java.time.Duration
import java.time.Instant

class CheckDuplicateEntryUseCase {

    /**
     * Checks if a newly scanned delivery is likely a duplicate of a very recent scan.
     * This prevents operators from scanning the same QR code twice by accident
     * or scanning a vendor who hasn't left the premises yet.
     */
    fun execute(
        previousEntry: SupplyEntry?,
        candidateCapturedAt: Instant,
        candidateVendorId: String,
        duplicateWindowMinutes: Long = 30, // Reduced to 30 mins to allow valid quick turnarounds
    ): Boolean {
        if (previousEntry == null) return false
        
        // Only flag as duplicate if it's the exact same vendor
        if (previousEntry.vendorId != candidateVendorId) return false

        val delta = Duration.between(previousEntry.capturedAt, candidateCapturedAt).abs()
        return delta.toMinutes() <= duplicateWindowMinutes
    }
}
