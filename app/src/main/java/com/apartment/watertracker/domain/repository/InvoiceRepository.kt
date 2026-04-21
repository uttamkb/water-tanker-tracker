package com.apartment.watertracker.domain.repository

import com.apartment.watertracker.domain.model.Invoice
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {
    fun observeInvoicesForApartment(): Flow<List<Invoice>>
    suspend fun generateDraftInvoice(vendorId: String, vendorName: String, month: String): Invoice
    suspend fun markInvoiceAsPaid(invoiceId: String, paymentReference: String)
}
