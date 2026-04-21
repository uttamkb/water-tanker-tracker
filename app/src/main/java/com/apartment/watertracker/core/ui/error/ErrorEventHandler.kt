package com.apartment.watertracker.core.ui.error

import android.database.sqlite.SQLiteException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.IOException

object ErrorEventHandler {
    
    /**
     * Maps an arbitrary Throwable to a user-friendly error message.
     */
    fun handle(throwable: Throwable): String {
        return when (throwable) {
            is IOException, is FirebaseNetworkException -> 
                "No internet connection. Please check your network and try again."
                
            is FirebaseFirestoreException -> 
                handleFirestoreException(throwable)
                
            is SecurityException -> 
                "Permission denied. Please grant the required permissions (e.g., Camera, Location) in device settings."
                
            is SQLiteException -> 
                "A local database error occurred. If the problem persists, try restarting the app."
                
            is IllegalArgumentException -> 
                throwable.localizedMessage ?: "Invalid input provided."
                
            else -> 
                throwable.localizedMessage ?: "An unexpected error occurred. Please try again."
        }
    }

    private fun handleFirestoreException(e: FirebaseFirestoreException): String {
        return when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "You do not have permission to access this data."
            FirebaseFirestoreException.Code.UNAVAILABLE -> "The service is currently unavailable. Please try again later."
            FirebaseFirestoreException.Code.NOT_FOUND -> "The requested data was not found."
            FirebaseFirestoreException.Code.ALREADY_EXISTS -> "This record already exists."
            else -> e.localizedMessage ?: "A cloud database error occurred."
        }
    }
}
