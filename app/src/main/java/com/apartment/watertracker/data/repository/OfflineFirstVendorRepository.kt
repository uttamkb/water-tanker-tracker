package com.apartment.watertracker.data.repository

import android.util.Log
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
        }.onFailure { e ->
            Log.e("VendorRepo", "Failed to sync vendor ${vendor.id}", e)
        }
    }

    override suspend fun deleteVendor(vendorId: String) {
        vendorDao.deleteById(vendorId)
        val vendorsCollection = vendorsCollection()
        runCatching {
            vendorsCollection.document(vendorId).delete().awaitResult()
        }.onFailure { e ->
            Log.e("VendorRepo", "Failed to delete vendor $vendorId", e)
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
        }.onFailure { e ->
            Log.e("VendorRepo", "Failed to refresh vendors", e)
        }
    }

    private suspend fun vendorsCollection() = firestore.collection("apartments")
        .document(apartmentScopeProvider.getApartmentId())
        .collection("vendors")
}
