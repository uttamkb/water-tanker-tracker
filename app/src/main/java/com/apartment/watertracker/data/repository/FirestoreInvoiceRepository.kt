package com.apartment.watertracker.data.repository

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
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreInvoiceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val apartmentScopeProvider: ApartmentScopeProvider
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
        
        // This is a stub for the client side. The backend should ideally calculate this 
        // to prevent client-side tampering, but we'll implement it here for the MVP.
        val invoiceId = UUID.randomUUID().toString()
        val mockLiters = 50000 // In a real app, query SupplyEntryDao for this vendor and month
        val mockDeliveries = 10
        val mockRate = 600.0
        
        val invoice = Invoice(
            id = invoiceId,
            apartmentId = apartmentId,
            vendorId = vendorId,
            vendorName = vendorName,
            billingMonth = month,
            totalLiters = mockLiters,
            deliveryCount = mockDeliveries,
            totalAmount = mockDeliveries * mockRate,
            status = InvoiceStatus.PENDING,
            dueDate = Instant.now().plus(7, ChronoUnit.DAYS),
            createdAt = Instant.now()
        )
        
        collection.document(invoiceId).set(invoice.toFirestoreDto()).await()
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
