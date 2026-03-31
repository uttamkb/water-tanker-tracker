package com.apartment.watertracker.core.qr

object VendorQrPayload {
    fun build(apartmentId: String, vendorId: String): String = "WT|$apartmentId|$vendorId"
}
