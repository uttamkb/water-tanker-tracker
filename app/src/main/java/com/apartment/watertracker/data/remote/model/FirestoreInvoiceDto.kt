package com.apartment.watertracker.data.remote.model

import com.google.firebase.firestore.PropertyName

data class FirestoreInvoiceDto(
    @get:PropertyName("apartmentId") @set:PropertyName("apartmentId") var apartmentId: String = "",
    @get:PropertyName("vendorId") @set:PropertyName("vendorId") var vendorId: String = "",
    @get:PropertyName("vendorName") @set:PropertyName("vendorName") var vendorName: String = "",
    @get:PropertyName("billingMonth") @set:PropertyName("billingMonth") var billingMonth: String = "",
    @get:PropertyName("totalLiters") @set:PropertyName("totalLiters") var totalLiters: Int = 0,
    @get:PropertyName("deliveryCount") @set:PropertyName("deliveryCount") var deliveryCount: Int = 0,
    @get:PropertyName("totalAmount") @set:PropertyName("totalAmount") var totalAmount: Double = 0.0,
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "DRAFT",
    @get:PropertyName("dueDateMillis") @set:PropertyName("dueDateMillis") var dueDateMillis: Long = 0,
    @get:PropertyName("createdAtMillis") @set:PropertyName("createdAtMillis") var createdAtMillis: Long = 0,
    @get:PropertyName("paymentReference") @set:PropertyName("paymentReference") var paymentReference: String? = null
)
