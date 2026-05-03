package com.apartment.watertracker.data.tenant

import com.apartment.watertracker.core.tenant.TenantDefaults
import com.apartment.watertracker.data.remote.model.FirestoreUserProfileDto
import com.apartment.watertracker.data.remote.util.awaitResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApartmentScopeProvider @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // A reactive flow of the current user's profile, cached for performance
    private val userProfileFlow: StateFlow<FirestoreUserProfileDto?> = 
        MutableStateFlow(firebaseAuth.currentUser?.uid).map { uid ->
            if (uid == null) return@map null
            
            try {
                firestore.collection("users")
                    .document(uid)
                    .get()
                    .awaitResult()
                    .toObject(FirestoreUserProfileDto::class.java)
            } catch (e: Exception) {
                null
            }
        }.stateIn(scope, SharingStarted.Eagerly, null)

    suspend fun getApartmentId(): String {
        val firebaseUser = firebaseAuth.currentUser ?: return TenantDefaults.DEFAULT_APARTMENT_ID
        
        // Fast path: use cached profile if available
        val cachedId = userProfileFlow.value?.apartmentId
        if (!cachedId.isNullOrBlank()) return cachedId

        // Fallback: Fetch manually if cache is empty
        val profile = firestore.collection("users")
            .document(firebaseUser.uid)
            .get()
            .awaitResult()
            .toObject(FirestoreUserProfileDto::class.java)

        return profile?.apartmentId?.ifBlank { firebaseUser.uid }
            ?: firebaseUser.uid
    }

    suspend fun getApartmentName(): String {
        val firebaseUser = firebaseAuth.currentUser ?: return TenantDefaults.DEFAULT_APARTMENT_NAME
        
        val cachedName = userProfileFlow.value?.apartmentName
        if (!cachedName.isNullOrBlank()) return cachedName

        val profile = firestore.collection("users")
            .document(firebaseUser.uid)
            .get()
            .awaitResult()
            .toObject(FirestoreUserProfileDto::class.java)

        return profile?.apartmentName?.ifBlank { "${firebaseUser.displayName ?: "Apartment"} Space" }
            ?: "${firebaseUser.displayName ?: "Apartment"} Space"
    }
}
