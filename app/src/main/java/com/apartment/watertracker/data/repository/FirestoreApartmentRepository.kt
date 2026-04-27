package com.apartment.watertracker.data.repository

import com.apartment.watertracker.data.remote.mapper.toAppUser
import com.apartment.watertracker.data.remote.mapper.toDomain
import com.apartment.watertracker.data.remote.mapper.toFirestoreDto
import com.apartment.watertracker.data.remote.model.FirestoreApartmentDto
import com.apartment.watertracker.data.remote.model.FirestoreOperatorInviteDto
import com.apartment.watertracker.data.remote.model.FirestoreUserProfileDto
import com.apartment.watertracker.data.remote.util.awaitResult
import com.apartment.watertracker.data.tenant.ApartmentScopeProvider
import com.apartment.watertracker.domain.model.ApartmentProfile
import com.apartment.watertracker.domain.model.AppUser
import com.apartment.watertracker.domain.model.OperatorInvite
import com.apartment.watertracker.domain.model.UserRole
import com.apartment.watertracker.domain.repository.ApartmentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class FirestoreApartmentRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val apartmentScopeProvider: ApartmentScopeProvider,
) : ApartmentRepository {

    override fun observeCurrentApartment(): Flow<ApartmentProfile?> = callbackFlow {
        val apartmentId = runCatching { apartmentScopeProvider.getApartmentId() }.getOrNull()
        if (apartmentId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("apartments")
            .document(apartmentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                } else {
                    trySend(snapshot?.toObject(FirestoreApartmentDto::class.java)?.toDomain(apartmentId))
                }
            }

        awaitClose { registration.remove() }
    }

    override fun observeMyApartments(): Flow<List<ApartmentProfile>> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("apartments")
            .whereEqualTo("createdByUserId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                } else {
                    val apartments = snapshot?.documents.orEmpty().mapNotNull { doc ->
                        doc.toObject(FirestoreApartmentDto::class.java)?.toDomain(doc.id)
                    }
                    trySend(apartments.sortedBy { it.name })
                }
            }

        awaitClose { registration.remove() }
    }

    override fun observeAllApartments(): Flow<List<ApartmentProfile>> = callbackFlow {
        val registration = firestore.collection("apartments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                } else {
                    val apartments = snapshot?.documents.orEmpty().mapNotNull { doc ->
                        doc.toObject(FirestoreApartmentDto::class.java)?.toDomain(doc.id)
                    }
                    trySend(apartments.sortedBy { it.name })
                }
            }

        awaitClose { registration.remove() }
    }

    override fun observeApartmentUsers(): Flow<List<AppUser>> = callbackFlow {
        val apartmentId = runCatching { apartmentScopeProvider.getApartmentId() }.getOrNull()
        if (apartmentId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("users")
            .whereEqualTo("apartmentId", apartmentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                } else {
                    val users = snapshot?.documents.orEmpty().mapNotNull { document ->
                        document.toObject(FirestoreUserProfileDto::class.java)?.toAppUser(document.id)
                    }
                    trySend(users.sortedBy { it.name })
                }
            }

        awaitClose { registration.remove() }
    }

    override fun observeOperatorInvites(): Flow<List<OperatorInvite>> = callbackFlow {
        val apartmentId = runCatching { apartmentScopeProvider.getApartmentId() }.getOrNull()
        if (apartmentId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("apartments")
            .document(apartmentId)
            .collection("operator_invites")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                } else {
                    val invites = snapshot?.documents.orEmpty().mapNotNull { document ->
                        document.toObject(FirestoreOperatorInviteDto::class.java)?.toDomain(document.id)
                    }
                    trySend(invites.sortedByDescending { it.createdAt })
                }
            }

        awaitClose { registration.remove() }
    }

    override suspend fun updateApartmentName(name: String) {
        val apartmentId = apartmentScopeProvider.getApartmentId()
        val currentUserId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("apartments")
            .document(apartmentId)
            .set(
                mapOf(
                    "apartmentId" to apartmentId,
                    "name" to name,
                    "createdByUserId" to currentUserId,
                ),
            )
            .awaitResult()

        firestore.collection("users")
            .document(currentUserId)
            .update("apartmentName", name)
            .awaitResult()
    }

    override suspend fun createApartment(name: String) {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        val apartmentId = UUID.randomUUID().toString()
        val dto = FirestoreApartmentDto(
            apartmentId = apartmentId,
            name = name,
            createdByUserId = currentUserId,
            subscriptionStatus = "ACTIVE",
            subscriptionExpiresAtEpochMillis = Instant.now().plus(30, ChronoUnit.DAYS).toEpochMilli(),
        )

        firestore.collection("apartments")
            .document(apartmentId)
            .set(dto)
            .awaitResult()

        // Switch current user profile to the new apartment.
        firestore.collection("users")
            .document(currentUserId)
            .update(
                mapOf(
                    "apartmentId" to apartmentId,
                    "apartmentName" to name,
                ),
            )
            .awaitResult()
    }

    override suspend fun switchApartment(apartmentId: String) {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        val apartmentDoc = firestore.collection("apartments")
            .document(apartmentId)
            .get()
            .awaitResult()
        val apartment = apartmentDoc.toObject(FirestoreApartmentDto::class.java) ?: return

        firestore.collection("users")
            .document(currentUserId)
            .update(
                mapOf(
                    "apartmentId" to apartmentId,
                    "apartmentName" to apartment.name,
                ),
            )
            .awaitResult()
    }

    override suspend fun createOperatorInvite(email: String) {
        val apartmentId = apartmentScopeProvider.getApartmentId()
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        val invite = OperatorInvite(
            id = UUID.randomUUID().toString(),
            apartmentId = apartmentId,
            email = email,
            role = UserRole.SECURITY_GUARD,
            status = "PENDING",
            createdAt = Instant.now(),
            createdByUserId = currentUserId,
        )

        firestore.collection("apartments")
            .document(apartmentId)
            .collection("operator_invites")
            .document(invite.id)
            .set(invite.toFirestoreDto())
            .awaitResult()
    }

    override suspend fun updateSubscription(apartmentId: String, status: String, expiresAtEpochMillis: Long?) {
        firestore.collection("apartments")
            .document(apartmentId)
            .update(
                mapOf(
                    "subscriptionStatus" to status,
                    "subscriptionExpiresAtEpochMillis" to expiresAtEpochMillis,
                ),
            )
            .awaitResult()
    }
}
