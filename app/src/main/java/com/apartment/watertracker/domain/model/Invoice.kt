package com.apartment.watertracker.domain.model

import java.time.Instant

data class Invoice(
    val id: String,
    val apartmentId: String,
    val vendorId: String,
    val vendorName: String,
    val billingMonth: String, // Format: "YYYY-MM"
    val totalLiters: Int,
    val deliveryCount: Int,
    val totalAmount: Double,
    val status: InvoiceStatus,
    val dueDate: Instant,
    val createdAt: Instant,
    val paymentReference: String? = null
)

enum class InvoiceStatus {
    DRAFT,
    PENDING,
    PAID,
    OVERDUE,
    CANCELLED
}
