package com.apartment.watertracker.data.tenant

import com.apartment.watertracker.core.tenant.TenantDefaults
import com.apartment.watertracker.data.remote.model.FirestoreUserProfileDto
import com.apartment.watertracker.data.remote.util.awaitResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApartmentScopeProvider @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {

    suspend fun getApartmentId(): String {
        val firebaseUser = firebaseAuth.currentUser ?: return TenantDefaults.DEFAULT_APARTMENT_ID
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
        val profile = firestore.collection("users")
            .document(firebaseUser.uid)
            .get()
            .awaitResult()
            .toObject(FirestoreUserProfileDto::class.java)

        return profile?.apartmentName?.ifBlank { "${firebaseUser.displayName ?: "Apartment"} Space" }
            ?: "${firebaseUser.displayName ?: "Apartment"} Space"
    }
}
