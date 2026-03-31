package com.apartment.watertracker.domain.repository

import com.apartment.watertracker.domain.model.ApartmentProfile
import com.apartment.watertracker.domain.model.AppUser
import com.apartment.watertracker.domain.model.OperatorInvite
import kotlinx.coroutines.flow.Flow

interface ApartmentRepository {
    fun observeCurrentApartment(): Flow<ApartmentProfile?>
    fun observeMyApartments(): Flow<List<ApartmentProfile>>
    fun observeAllApartments(): Flow<List<ApartmentProfile>>
    fun observeApartmentUsers(): Flow<List<AppUser>>
    fun observeOperatorInvites(): Flow<List<OperatorInvite>>
    suspend fun updateApartmentName(name: String)
    suspend fun createApartment(name: String)
    suspend fun switchApartment(apartmentId: String)
    suspend fun updateSubscription(apartmentId: String, status: String, expiresAtEpochMillis: Long?)
    suspend fun createOperatorInvite(email: String)
}
