package com.apartment.watertracker.data.repository

import com.apartment.watertracker.data.local.dao.DeliveryDao
import com.apartment.watertracker.data.local.entity.DeliveryEntity
import com.apartment.watertracker.data.local.mapper.toDomain
import com.apartment.watertracker.data.local.mapper.toEntity as toLocalEntity
import com.apartment.watertracker.data.local.mapper.toFirestoreDto
import com.apartment.watertracker.data.local.mapper.toLocalEntity as remoteToLocalEntity
import com.apartment.watertracker.data.remote.model.FirestoreDeliveryDto
import com.apartment.watertracker.data.remote.util.awaitResult
import com.apartment.watertracker.data.tenant.ApartmentScopeProvider
import com.apartment.watertracker.domain.model.Delivery
import com.apartment.watertracker.domain.repository.DeliveryRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstDeliveryRepository @Inject constructor(
    private val deliveryDao: DeliveryDao,
    private val vendorDao: com.apartment.watertracker.data.local.dao.VendorDao,
    private val firestore: FirebaseFirestore,
    private val apartmentScopeProvider: ApartmentScopeProvider,
) : DeliveryRepository {

    override fun observeDeliveries(): Flow<List<Delivery>> = kotlinx.coroutines.flow.flow {
        val apartmentId = apartmentScopeProvider.getApartmentId()
        deliveryDao.observeDeliveriesByApartment(apartmentId)
            .map { deliveries -> deliveries.map(DeliveryEntity::toDomain) }
            .collect { emit(it) }
    }

    override suspend fun getDeliveryById(deliveryId: String): Delivery? =
        deliveryDao.getById(deliveryId)?.toDomain()

    override suspend fun getDeliveriesByVendor(vendorId: String): List<Delivery> =
        deliveryDao.getDeliveriesByVendor(vendorId, apartmentScopeProvider.getApartmentId())
            .map(DeliveryEntity::toDomain)

    override suspend fun findVendorByBarcode(barcode: String): com.apartment.watertracker.domain.model.Vendor? {
        val entity = vendorDao.getByQrValue(barcode) ?: return null
        return com.apartment.watertracker.domain.model.Vendor(
            id = entity.id,
            apartmentId = entity.apartmentId,
            supplierName = entity.supplierName,
            contactPerson = entity.contactPerson,
            phoneNumber = entity.phoneNumber,
            alternatePhoneNumber = entity.alternatePhoneNumber,
            address = entity.address,
            notes = entity.notes,
            isActive = entity.isActive,
            qrValue = entity.qrValue,
            defaultCapacityLiters = entity.defaultCapacityLiters
        )
    }

    override suspend fun saveDelivery(delivery: Delivery) {
        // Enforce Apartment ID security on save
        val scopedDelivery = if (delivery.apartmentId != apartmentScopeProvider.getApartmentId()) {
            delivery.copy(apartmentId = apartmentScopeProvider.getApartmentId())
        } else {
            delivery
        }
        
        deliveryDao.upsert(scopedDelivery.toLocalEntity())
        
        val deliveriesCollection = deliveriesCollection()
        runCatching {
            deliveriesCollection.document(scopedDelivery.id)
                .set(scopedDelivery.toLocalEntity().toFirestoreDto())
                .awaitResult()
        }
    }

    override suspend fun deleteDelivery(deliveryId: String) {
        deliveryDao.deleteById(deliveryId)
        val deliveriesCollection = deliveriesCollection()
        runCatching {
            deliveriesCollection.document(deliveryId).delete().awaitResult()
        }
    }

    override suspend fun refreshDeliveries() {
        val deliveriesCollection = deliveriesCollection()
        runCatching {
            val snapshot = deliveriesCollection.get().awaitResult()
            val remoteDeliveries = snapshot.documents.mapNotNull { document ->
                document.toObject(FirestoreDeliveryDto::class.java)?.remoteToLocalEntity(document.id)
            }

            if (remoteDeliveries.isNotEmpty()) {
                deliveryDao.upsertAll(remoteDeliveries)
            }
        }
    }

    private suspend fun deliveriesCollection() = firestore.collection("apartments")
        .document(apartmentScopeProvider.getApartmentId())
        .collection("deliveries")
}
