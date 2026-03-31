package com.apartment.watertracker.domain.repository

import com.apartment.watertracker.domain.model.Vendor
import kotlinx.coroutines.flow.Flow

interface VendorRepository {
    fun observeVendors(): Flow<List<Vendor>>
    fun observeVendor(vendorId: String): Flow<Vendor?>
    suspend fun getVendorByQrValue(qrValue: String): Vendor?
    suspend fun saveVendor(vendor: Vendor)
    suspend fun deleteVendor(vendorId: String)
    suspend fun refreshVendors()
}
