package com.apartment.watertracker.data.repository

import com.apartment.watertracker.data.remote.mapper.toDomain
import com.apartment.watertracker.data.remote.mapper.toFirestoreDto
import com.apartment.watertracker.data.remote.model.FirestoreTankerRequestDto
import com.apartment.watertracker.data.tenant.ApartmentScopeProvider
import com.apartment.watertracker.domain.model.RequestStatus
import com.apartment.watertracker.domain.model.TankerRequest
import com.apartment.watertracker.domain.repository.TankerRequestRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreTankerRequestRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val apartmentScopeProvider: ApartmentScopeProvider
) : TankerRequestRepository {

    private val collection = firestore.collection("tanker_requests")

    override fun observeMyRequests(): Flow<List<TankerRequest>> = callbackFlow {
        val apartmentId = apartmentScopeProvider.getApartmentId()
        val registration = collection
            .whereEqualTo("apartmentId", apartmentId)
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirestoreTankerRequestDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                trySend(requests)
            }
        awaitClose { registration.remove() }
    }

    override fun observeOpenRequests(): Flow<List<TankerRequest>> = callbackFlow {
        val registration = collection
            .whereEqualTo("status", RequestStatus.OPEN.name)
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirestoreTankerRequestDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                trySend(requests)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun createRequest(request: TankerRequest) {
        collection.add(request.toFirestoreDto()).await()
    }

    override suspend fun cancelRequest(requestId: String) {
        collection.document(requestId).update("status", RequestStatus.CANCELLED.name).await()
    }

    override suspend fun markAsFulfilled(requestId: String) {
        collection.document(requestId).update("status", RequestStatus.FULFILLED.name).await()
    }
}
