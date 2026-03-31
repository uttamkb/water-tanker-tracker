package com.apartment.watertracker.di

import android.content.Context
import androidx.room.Room
import com.apartment.watertracker.data.local.WaterTrackerDatabase
import com.apartment.watertracker.data.local.dao.SupplyEntryDao
import com.apartment.watertracker.data.local.dao.VendorDao
import com.apartment.watertracker.data.repository.FirestoreApartmentRepository
import com.apartment.watertracker.data.repository.AndroidLocationRepository
import com.apartment.watertracker.data.repository.FirebaseAuthRepository
import com.apartment.watertracker.data.repository.OfflineFirstSupplyEntryRepository
import com.apartment.watertracker.data.repository.OfflineFirstVendorRepository
import com.apartment.watertracker.domain.repository.AuthRepository
import com.apartment.watertracker.domain.repository.ApartmentRepository
import com.apartment.watertracker.domain.repository.LocationRepository
import com.apartment.watertracker.domain.repository.SupplyEntryRepository
import com.apartment.watertracker.domain.repository.VendorRepository
import com.apartment.watertracker.domain.usecase.CheckDuplicateEntryUseCase
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): WaterTrackerDatabase {
        return Room.databaseBuilder(
            context,
            WaterTrackerDatabase::class.java,
            "water-tracker.db",
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideVendorDao(database: WaterTrackerDatabase): VendorDao = database.vendorDao()

    @Provides
    fun provideSupplyEntryDao(database: WaterTrackerDatabase): SupplyEntryDao = database.supplyEntryDao()

    @Provides
    fun provideCheckDuplicateEntryUseCase(): CheckDuplicateEntryUseCase = CheckDuplicateEntryUseCase()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context,
    ) = LocationServices.getFusedLocationProviderClient(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindAuthRepository(repository: FirebaseAuthRepository): AuthRepository

    @Binds
    abstract fun bindApartmentRepository(repository: FirestoreApartmentRepository): ApartmentRepository

    @Binds
    abstract fun bindVendorRepository(repository: OfflineFirstVendorRepository): VendorRepository

    @Binds
    abstract fun bindSupplyEntryRepository(repository: OfflineFirstSupplyEntryRepository): SupplyEntryRepository

    @Binds
    abstract fun bindLocationRepository(repository: AndroidLocationRepository): LocationRepository
}
