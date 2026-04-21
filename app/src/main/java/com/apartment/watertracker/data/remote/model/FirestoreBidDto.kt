package com.apartment.watertracker.data.remote.model

import com.google.firebase.firestore.PropertyName

data class FirestoreBidDto(
    @get:PropertyName("requestId") @set:PropertyName("requestId") var requestId: String = "",
    @get:PropertyName("vendorId") @set:PropertyName("vendorId") var vendorId: String = "",
    @get:PropertyName("vendorName") @set:PropertyName("vendorName") var vendorName: String = "",
    @get:PropertyName("price") @set:PropertyName("price") var price: Double = 0.0,
    @get:PropertyName("estimatedArrivalMillis") @set:PropertyName("estimatedArrivalMillis") var estimatedArrivalMillis: Long = 0,
    @get:PropertyName("createdAtMillis") @set:PropertyName("createdAtMillis") var createdAtMillis: Long = 0,
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "PENDING",
    @get:PropertyName("notes") @set:PropertyName("notes") var notes: String? = null
)
