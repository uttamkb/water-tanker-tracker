package com.apartment.watertracker.data.local.mapper

import com.apartment.watertracker.data.local.entity.DeliveryEntity
import com.apartment.watertracker.data.remote.model.FirestoreDeliveryDto
import com.apartment.watertracker.domain.model.Delivery
import com.apartment.watertracker.domain.model.DeliveryStatus

// Domain to Local Entity
fun Delivery.toEntity(): DeliveryEntity {
    return DeliveryEntity(
        id = id,
        apartmentId = apartmentId,
        vendorId = vendorId,
        timestamp = timestamp,
        quantityLiters = quantityLiters,
        driverName = driverName,
        status = status.name,
        latitude = latitude,
        longitude = longitude
    )
}

// Local Entity to Domain
fun DeliveryEntity.toDomain(): Delivery {
    return Delivery(
        id = id,
        apartmentId = apartmentId,
        vendorId = vendorId,
        timestamp = timestamp,
        quantityLiters = quantityLiters,
        driverName = driverName,
        status = try { DeliveryStatus.valueOf(status) } catch (e: Exception) { DeliveryStatus.COMPLETED },
        latitude = latitude,
        longitude = longitude
    )
}

// Remote DTO to Local Entity
fun FirestoreDeliveryDto.toLocalEntity(documentId: String): DeliveryEntity {
    return DeliveryEntity(
        id = id.ifEmpty { documentId },
        apartmentId = apartmentId,
        vendorId = vendorId,
        timestamp = timestamp,
        quantityLiters = quantityLiters,
        driverName = driverName,
        status = status,
        latitude = latitude,
        longitude = longitude
    )
}

// Local Entity to Remote DTO
fun DeliveryEntity.toFirestoreDto(): FirestoreDeliveryDto {
    return FirestoreDeliveryDto(
        id = id,
        apartmentId = apartmentId,
        vendorId = vendorId,
        timestamp = timestamp,
        quantityLiters = quantityLiters,
        driverName = driverName,
        status = status,
        latitude = latitude,
        longitude = longitude
    )
}
