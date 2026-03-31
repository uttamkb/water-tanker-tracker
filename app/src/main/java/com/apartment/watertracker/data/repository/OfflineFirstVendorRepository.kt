package com.apartment.watertracker.data.repository

import com.apartment.watertracker.core.qr.VendorQrPayload
import com.apartment.watertracker.core.tenant.TenantDefaults
import com.apartment.watertracker.data.local.dao.VendorDao
import com.apartment.watertracker.data.local.entity.VendorEntity
import com.apartment.watertracker.data.local.mapper.toDomain
import com.apartment.watertracker.data.local.mapper.toEntity as toLocalEntity
import com.apartment.watertracker.data.remote.mapper.toEntity as toRemoteEntity
import com.apartment.watertracker.data.remote.mapper.toFirestoreDto
import com.apartment.watertracker.data.remote.model.FirestoreVendorDto
import com.apartment.watertracker.data.remote.util.awaitResult
import com.apartment.watertracker.data.tenant.ApartmentScopeProvider
import com.apartment.watertracker.domain.model.Vendor
import com.apartment.watertracker.domain.repository.VendorRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstVendorRepository @Inject constructor(
    private val vendorDao: VendorDao,
    private val firestore: FirebaseFirestore,
    private val apartmentScopeProvider: ApartmentScopeProvider,
) : VendorRepository {

    override fun observeVendors(): Flow<List<Vendor>> =
        vendorDao.observeAll().map { vendors -> vendors.map(VendorEntity::toDomain) }

    override fun observeVendor(vendorId: String): Flow<Vendor?> =
        vendorDao.observeById(vendorId).map { it?.toDomain() }

    override suspend fun getVendorByQrValue(qrValue: String): Vendor? =
        vendorDao.getByQrValue(qrValue)?.toDomain()

    override suspend fun saveVendor(vendor: Vendor) {
        vendorDao.upsert(vendor.toLocalEntity())
        val vendorsCollection = vendorsCollection()
        runCatching {
            vendorsCollection.document(vendor.id)
                .set(vendor.toLocalEntity().toFirestoreDto())
                .awaitResult()
        }
    }

    override suspend fun refreshVendors() {
        val vendorsCollection = vendorsCollection()
        runCatching {
            val snapshot = vendorsCollection.get().awaitResult()
            val remoteVendors = snapshot.documents.mapNotNull { document ->
                document.toObject(FirestoreVendorDto::class.java)?.toRemoteEntity(document.id)
            }

            if (remoteVendors.isNotEmpty()) {
                vendorDao.upsertAll(remoteVendors)
            }
        }
    }

    override suspend fun seedDemoVendorsIfEmpty() {
        if (vendorDao.count() > 0) return

        refreshVendors()
        if (vendorDao.count() > 0) return

        val apartmentId = runCatching { apartmentScopeProvider.getApartmentId() }
            .getOrDefault(TenantDefaults.DEFAULT_APARTMENT_ID)
        val demoVendors = listOf(
                Vendor(
                    id = UUID.randomUUID().toString(),
                    apartmentId = apartmentId,
                    supplierName = "A1 Water Supply",
                    contactPerson = "Rakesh",
                    phoneNumber = "9876543210",
                    alternatePhoneNumber = null,
                    address = "Patia, Bhubaneswar",
                    notes = "Morning slot preferred",
                    isActive = true,
                    qrValue = VendorQrPayload.build(apartmentId, "A1-WATER"),
                ),
                Vendor(
                    id = UUID.randomUUID().toString(),
                    apartmentId = apartmentId,
                    supplierName = "Fresh Tankers",
                    contactPerson = "Mahesh",
                    phoneNumber = "9123456780",
                    alternatePhoneNumber = null,
                    address = "Chandrasekharpur, Bhubaneswar",
                    notes = null,
                    isActive = true,
                    qrValue = VendorQrPayload.build(apartmentId, "FRESH-TANKERS"),
                ),
                Vendor(
                    id = UUID.randomUUID().toString(),
                    apartmentId = apartmentId,
                    supplierName = "Metro Water Services",
                    contactPerson = "Sanjay",
                    phoneNumber = "9988776655",
                    alternatePhoneNumber = "9333333333",
                    address = "Infocity, Bhubaneswar",
                    notes = "Emergency vendor",
                    isActive = true,
                    qrValue = VendorQrPayload.build(apartmentId, "METRO-WATER"),
                ),
            )

        vendorDao.upsertAll(demoVendors.map(Vendor::toLocalEntity))
        val vendorsCollection = vendorsCollection()
        demoVendors.forEach { vendor ->
            runCatching {
                vendorsCollection.document(vendor.id)
                    .set(vendor.toLocalEntity().toFirestoreDto())
                    .awaitResult()
            }
        }
    }

    private suspend fun vendorsCollection() = firestore.collection("apartments")
        .document(apartmentScopeProvider.getApartmentId())
        .collection("vendors")
}
