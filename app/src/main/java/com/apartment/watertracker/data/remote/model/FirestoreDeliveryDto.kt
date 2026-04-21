package com.apartment.watertracker.data.remote.model

import com.google.firebase.firestore.PropertyName

data class FirestoreDeliveryDto(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",
    
    @get:PropertyName("apartmentId") @set:PropertyName("apartmentId")
    var apartmentId: String = "",
    
    @get:PropertyName("vendorId") @set:PropertyName("vendorId")
    var vendorId: String = "",
    
    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Long = 0L,
    
    @get:PropertyName("quantityLiters") @set:PropertyName("quantityLiters")
    var quantityLiters: Int = 0,
    
    @get:PropertyName("driverName") @set:PropertyName("driverName")
    var driverName: String? = null,
    
    @get:PropertyName("status") @set:PropertyName("status")
    var status: String = "COMPLETED",
    
    @get:PropertyName("latitude") @set:PropertyName("latitude")
    var latitude: Double? = null,
    
    @get:PropertyName("longitude") @set:PropertyName("longitude")
    var longitude: Double? = null
)
