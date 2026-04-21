package com.apartment.watertracker.data.remote.model

import com.google.firebase.firestore.PropertyName

data class FirestoreTankerRequestDto(
    @get:PropertyName("apartmentId") @set:PropertyName("apartmentId") var apartmentId: String = "",
    @get:PropertyName("apartmentName") @set:PropertyName("apartmentName") var apartmentName: String = "",
    @get:PropertyName("requestedByUserId") @set:PropertyName("requestedByUserId") var requestedByUserId: String = "",
    @get:PropertyName("quantityLiters") @set:PropertyName("quantityLiters") var quantityLiters: Int = 0,
    @get:PropertyName("urgency") @set:PropertyName("urgency") var urgency: String = "NORMAL",
    @get:PropertyName("createdAtMillis") @set:PropertyName("createdAtMillis") var createdAtMillis: Long = 0,
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "OPEN",
    @get:PropertyName("notes") @set:PropertyName("notes") var notes: String? = null,
    @get:PropertyName("bidsCount") @set:PropertyName("bidsCount") var bidsCount: Int = 0
)
