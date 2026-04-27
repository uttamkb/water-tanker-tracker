package com.apartment.watertracker.data.repository

import com.apartment.watertracker.data.local.dao.SupplyEntryDao
import com.apartment.watertracker.data.remote.mapper.toDomain
import com.apartment.watertracker.data.remote.mapper.toFirestoreDto
import com.apartment.watertracker.data.remote.model.FirestoreInvoiceDto
import com.apartment.watertracker.data.tenant.ApartmentScopeProvider
import com.apartment.watertracker.domain.model.Invoice
import com.apartment.watertracker.domain.model.InvoiceStatus
import com.apartment.watertracker.domain.repository.InvoiceRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreInvoiceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val apartmentScopeProvider: ApartmentScopeProvider,
    private val supplyEntryDao: SupplyEntryDao
) : InvoiceRepository {

    private val collection = firestore.collection("invoices")

    override fun observeInvoicesForApartment(): Flow<List<Invoice>> = callbackFlow {
        val apartmentId = apartmentScopeProvider.getApartmentId()
        val registration = collection
            .whereEqualTo("apartmentId", apartmentId)
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val invoices = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirestoreInvoiceDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                trySend(invoices)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun generateDraftInvoice(vendorId: String, vendorName: String, month: String): Invoice {
        val apartmentId = apartmentScopeProvider.getApartmentId()
        
        // Parse month "YYYY-MM" to find range
        val yearMonth = YearMonth.parse(month)
        val zone = ZoneId.systemDefault()
        val start = yearMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        
        val aggregate = supplyEntryDao.getAggregateForVendorInRange(vendorId, start, end)
        
        val invoiceId = UUID.randomUUID().toString()
        val mockRate = 600.0 // FUTURE: Fetch from Vendor contract
        
        val invoice = Invoice(
            id = invoiceId,
            apartmentId = apartmentId,
            vendorId = vendorId,
            vendorName = vendorName,
            billingMonth = month,
            totalLiters = aggregate.totalVolumeLiters.toInt(),
            deliveryCount = aggregate.count,
            totalAmount = aggregate.count * mockRate,
            status = InvoiceStatus.PENDING,
            dueDate = Instant.now().plus(7, ChronoUnit.DAYS),
            createdAt = Instant.now()
        )
        
        if (aggregate.count > 0) {
            collection.document(invoiceId).set(invoice.toFirestoreDto()).await()
        }
        return invoice
    }

    override suspend fun markInvoiceAsPaid(invoiceId: String, paymentReference: String) {
        collection.document(invoiceId).update(
            mapOf(
                "status" to InvoiceStatus.PAID.name,
                "paymentReference" to paymentReference
            )
        ).await()
    }
}
