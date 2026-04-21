package com.apartment.watertracker.data.repository

import com.apartment.watertracker.data.remote.mapper.toDomain
import com.apartment.watertracker.data.remote.mapper.toFirestoreDto
import com.apartment.watertracker.data.remote.model.FirestoreBidDto
import com.apartment.watertracker.domain.model.Bid
import com.apartment.watertracker.domain.model.BidStatus
import com.apartment.watertracker.domain.model.RequestStatus
import com.apartment.watertracker.domain.repository.BidRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreBidRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : BidRepository {

    private val bidsCollection = firestore.collection("bids")
    private val requestsCollection = firestore.collection("tanker_requests")

    override fun observeBidsForRequest(requestId: String): Flow<List<Bid>> = callbackFlow {
        val registration = bidsCollection
            .whereEqualTo("requestId", requestId)
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val bids = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirestoreBidDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                trySend(bids)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun placeBid(bid: Bid) {
        firestore.runTransaction { transaction ->
            val requestRef = requestsCollection.document(bid.requestId)
            val requestDoc = transaction.get(requestRef)
            val currentBids = requestDoc.getLong("bidsCount") ?: 0L
            
            transaction.update(requestRef, "bidsCount", currentBids + 1)
            transaction.set(bidsCollection.document(bid.id), bid.toFirestoreDto())
        }.await()
    }

    override suspend fun acceptBid(requestId: String, bidId: String) {
        firestore.runBatch { batch ->
            // Mark bid as ACCEPTED
            batch.update(bidsCollection.document(bidId), "status", BidStatus.ACCEPTED.name)
            
            // Mark request as FULFILLED (or maybe a new state like 'ASSIGNED')
            batch.update(requestsCollection.document(requestId), "status", RequestStatus.FULFILLED.name)
            
            // Optionally reject other bids (future logic)
        }.await()
    }

    override suspend fun rejectBid(bidId: String) {
        bidsCollection.document(bidId).update("status", BidStatus.REJECTED.name).await()
    }
}
