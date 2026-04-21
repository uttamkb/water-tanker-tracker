package com.apartment.watertracker.domain.repository

import android.app.Activity
import com.apartment.watertracker.domain.model.AppUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AppUser?>
    suspend fun signInWithGoogle(activity: Activity)
    suspend fun signOut()
    suspend fun registerFcmToken(token: String)
}
