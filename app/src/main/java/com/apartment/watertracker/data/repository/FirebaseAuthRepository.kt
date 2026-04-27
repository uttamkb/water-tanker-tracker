package com.apartment.watertracker.data.repository

import android.app.Activity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.apartment.watertracker.data.remote.mapper.toAppUser
import com.apartment.watertracker.data.remote.mapper.toFirestoreDto
import com.apartment.watertracker.data.remote.mapper.toRole
import com.apartment.watertracker.data.remote.model.FirestoreOperatorInviteDto
import com.apartment.watertracker.data.remote.model.FirestoreUserProfileDto
import com.apartment.watertracker.data.remote.util.awaitResult
import com.apartment.watertracker.domain.model.AppUser
import com.apartment.watertracker.domain.model.UserRole
import com.apartment.watertracker.domain.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val appContext: android.content.Context,
) : AuthRepository {

    private val usersCollection = firestore.collection("users")

    override val currentUser: Flow<AppUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                trySend(null)
                return@AuthStateListener
            }

            usersCollection.document(firebaseUser.uid).get()
                .addOnSuccessListener { snapshot ->
                    val profile = snapshot.toObject(FirestoreUserProfileDto::class.java)
                    trySend(
                        firebaseUser.toAppUser(
                            role = profile?.toRole() ?: UserRole.SOCIETY_ADMIN,
                            apartmentId = profile?.apartmentId?.ifBlank { firebaseUser.uid } ?: firebaseUser.uid,
                            apartmentName = profile?.apartmentName ?: "${firebaseUser.displayName ?: "Apartment"} Space",
                        ),
                    )
                }
                .addOnFailureListener {
                    trySend(
                        firebaseUser.toAppUser(
                            apartmentId = firebaseUser.uid,
                            apartmentName = "${firebaseUser.displayName ?: "Apartment"} Space",
                        ),
                    )
                }
        }

        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogle(activity: Activity) {
        val credentialManager = CredentialManager.create(activity)
        val credential = runCatching {
            credentialManager.getCredential(
                context = activity,
                request = buildGoogleRequest(),
            ).credential
        }.getOrElse {
            credentialManager.getCredential(
                context = activity,
                request = buildSignInWithGoogleRequest(),
            ).credential
        }

        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw IllegalStateException("Unsupported Google credential response")
        }

        val googleCredential = try {
            GoogleIdTokenCredential.createFrom(credential.data)
        } catch (exception: GoogleIdTokenParsingException) {
            throw IllegalStateException("Could not parse Google ID token", exception)
        }

        val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
        val authResult = firebaseAuth.signInWithCredential(firebaseCredential).awaitResult()
        val firebaseUser = checkNotNull(authResult.user)
        val userEmail = firebaseUser.email ?: ""
        
        var existingProfile = usersCollection.document(firebaseUser.uid)
            .get()
            .awaitResult()
            .toObject(FirestoreUserProfileDto::class.java)
            
        // Check for pending invites ONLY if this is a brand new user or they are still in their default self-apartment
        if (existingProfile == null || existingProfile.apartmentId == firebaseUser.uid) {
            val inviteResult = findAndAcceptInvite(userEmail)
            if (inviteResult != null) {
                // An invite was found and accepted!
                val (newApartmentId, newRole) = inviteResult
                
                // Fetch the actual apartment name to store in profile
                val apartmentDoc = firestore.collection("apartments").document(newApartmentId).get().awaitResult()
                val apartmentName = apartmentDoc.getString("name") ?: "Joined Apartment"
                
                existingProfile = FirestoreUserProfileDto(
                    name = firebaseUser.displayName ?: "",
                    email = userEmail,
                    role = newRole,
                    apartmentId = newApartmentId,
                    apartmentName = apartmentName,
                    fcmTokens = existingProfile?.fcmTokens ?: emptyList()
                )
            }
        }

        val appUser = firebaseUser.toAppUser(
            role = existingProfile?.toRole() ?: UserRole.SOCIETY_ADMIN,
            apartmentId = existingProfile?.apartmentId?.ifBlank { firebaseUser.uid } ?: firebaseUser.uid,
            apartmentName = existingProfile?.apartmentName ?: "${firebaseUser.displayName ?: "Apartment"} Space",
        ).copy(fcmTokens = existingProfile?.fcmTokens ?: emptyList())

        usersCollection.document(firebaseUser.uid)
            .set(appUser.toFirestoreDto())
            .awaitResult()

        // Only create a new apartment if they didn't exist AND didn't accept an invite
        if (existingProfile == null) {
            firestore.collection("apartments")
                .document(appUser.apartmentId)
                .set(
                    mapOf(
                        "apartmentId" to appUser.apartmentId,
                        "name" to appUser.apartmentName,
                        "createdByUserId" to firebaseUser.uid,
                    ),
                )
                .awaitResult()
        }
    }
    
    /**
     * Searches all apartments for a pending invite matching the user's email.
     * If found, marks it ACCEPTED and returns the [apartmentId, role].
     * Note: In a true SaaS, this requires a Collection Group query.
     */
    private suspend fun findAndAcceptInvite(email: String): Pair<String, String>? {
        if (email.isBlank()) return null
        
        try {
            // Find any pending invite across all apartments for this email
            val invitesSnapshot = firestore.collectionGroup("operator_invites")
                .whereEqualTo("email", email)
                .whereEqualTo("status", "PENDING")
                .limit(1)
                .get()
                .awaitResult()
                
            if (!invitesSnapshot.isEmpty) {
                val inviteDoc = invitesSnapshot.documents.first()
                val dto = inviteDoc.toObject(FirestoreOperatorInviteDto::class.java) ?: return null
                
                // Mark as accepted
                inviteDoc.reference.update("status", "ACCEPTED").awaitResult()
                
                return Pair(dto.apartmentId, dto.role)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override suspend fun registerFcmToken(token: String) {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        usersCollection.document(currentUserId)
            .update("fcmTokens", FieldValue.arrayUnion(token))
            .awaitResult()
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        val credentialManager = CredentialManager.create(appContext)
        runCatching {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        }
    }

    private fun buildGoogleRequest(): GetCredentialRequest {
        val webClientId = requireWebClientId()

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    private fun buildSignInWithGoogleRequest(): GetCredentialRequest {
        val webClientId = requireWebClientId()
        val signInOption = GetSignInWithGoogleOption.Builder(webClientId).build()
        return GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()
    }

    private fun requireWebClientId(): String {
        return appContext.defaultWebClientId()
            ?: throw IllegalStateException(
                "default_web_client_id is missing. Enable Google sign-in in Firebase and download updated google-services.json.",
            )
    }
}

private fun android.content.Context.defaultWebClientId(): String? {
    val resourceId = resources.getIdentifier(
        "default_web_client_id",
        "string",
        packageName,
    )
    if (resourceId == 0) return null
    return getString(resourceId)
}
