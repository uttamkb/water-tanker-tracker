package com.apartment.watertracker.domain.repository

import com.apartment.watertracker.domain.model.Delivery
import kotlinx.coroutines.flow.Flow

interface DeliveryRepository {
    fun observeDeliveries(): Flow<List<Delivery>>
    suspend fun getDeliveryById(deliveryId: String): Delivery?
    suspend fun getDeliveriesByVendor(vendorId: String): List<Delivery>
    suspend fun findVendorByBarcode(barcode: String): com.apartment.watertracker.domain.model.Vendor?
    suspend fun saveDelivery(delivery: Delivery)
    suspend fun deleteDelivery(deliveryId: String)
    suspend fun refreshDeliveries()
}
