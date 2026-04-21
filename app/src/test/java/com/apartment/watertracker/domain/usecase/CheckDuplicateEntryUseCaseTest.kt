package com.apartment.watertracker.domain.usecase

import com.apartment.watertracker.domain.model.SupplyEntry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class CheckDuplicateEntryUseCaseTest {

    private val useCase = CheckDuplicateEntryUseCase()

    @Test
    fun `execute returns false when previousEntry is null`() {
        val isDuplicate = useCase.execute(
            previousEntry = null,
            candidateCapturedAt = Instant.now(),
            candidateVendorId = "vendor_1"
        )
        assertFalse(isDuplicate)
    }

    @Test
    fun `execute returns false when previousEntry is a different vendor`() {
        val now = Instant.now()
        val previousEntry = createDummyEntry(capturedAt = now.minus(10, ChronoUnit.MINUTES), vendorId = "vendor_2")

        val isDuplicate = useCase.execute(
            previousEntry = previousEntry,
            candidateCapturedAt = now,
            candidateVendorId = "vendor_1", // Scanning a completely different vendor
            duplicateWindowMinutes = 30
        )
        assertFalse(isDuplicate)
    }

    @Test
    fun `execute returns true when within duplicate window and same vendor`() {
        val now = Instant.now()
        val previousEntry = createDummyEntry(capturedAt = now.minus(15, ChronoUnit.MINUTES), vendorId = "vendor_1")

        val isDuplicate = useCase.execute(
            previousEntry = previousEntry,
            candidateCapturedAt = now,
            candidateVendorId = "vendor_1",
            duplicateWindowMinutes = 30
        )
        assertTrue(isDuplicate)
    }

    @Test
    fun `execute returns false when outside duplicate window but same vendor`() {
        val now = Instant.now()
        val previousEntry = createDummyEntry(capturedAt = now.minus(45, ChronoUnit.MINUTES), vendorId = "vendor_1")

        val isDuplicate = useCase.execute(
            previousEntry = previousEntry,
            candidateCapturedAt = now,
            candidateVendorId = "vendor_1",
            duplicateWindowMinutes = 30
        )
        assertFalse(isDuplicate)
    }

    private fun createDummyEntry(capturedAt: Instant, vendorId: String = "vendor_1"): SupplyEntry {
        return SupplyEntry(
            id = "test_id",
            apartmentId = "apt_1",
            vendorId = vendorId,
            capturedAt = capturedAt,
            volumeLiters = 5000,
            vehicleNumber = "KA-01-1234",
            hardnessPpm = 300,
            phLevel = 7.0,
            tdsPpm = 150,
            qualityRating = null,
            timelinessRating = null,
            hygieneRating = null,
            remarks = null,
            latitude = 0.0,
            longitude = 0.0,
            gpsAccuracyMeters = 5.0f,
            photoUrl = null,
            duplicateFlag = false,
            duplicateReferenceId = null,
            createdByUserId = "user_1"
        )
    }
}
