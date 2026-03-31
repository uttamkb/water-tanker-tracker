package com.apartment.watertracker.data.local.mapper

import com.apartment.watertracker.data.local.entity.SupplyEntryEntity
import com.apartment.watertracker.data.local.entity.VendorEntity
import com.apartment.watertracker.domain.model.SupplyEntry
import com.apartment.watertracker.domain.model.Vendor
import java.time.Instant

fun VendorEntity.toDomain(): Vendor = Vendor(
    id = id,
    apartmentId = apartmentId,
    supplierName = supplierName,
    contactPerson = contactPerson,
    phoneNumber = phoneNumber,
    alternatePhoneNumber = alternatePhoneNumber,
    address = address,
    notes = notes,
    isActive = isActive,
    qrValue = qrValue,
)

fun Vendor.toEntity(): VendorEntity = VendorEntity(
    id = id,
    apartmentId = apartmentId,
    supplierName = supplierName,
    contactPerson = contactPerson,
    phoneNumber = phoneNumber,
    alternatePhoneNumber = alternatePhoneNumber,
    address = address,
    notes = notes,
    isActive = isActive,
    qrValue = qrValue,
)

fun SupplyEntryEntity.toDomain(): SupplyEntry = SupplyEntry(
    id = id,
    apartmentId = apartmentId,
    vendorId = vendorId,
    hardnessPpm = hardnessPpm,
    capturedAt = Instant.ofEpochMilli(capturedAtEpochMillis),
    latitude = latitude,
    longitude = longitude,
    gpsAccuracyMeters = gpsAccuracyMeters,
    vehicleNumber = vehicleNumber,
    remarks = remarks,
    photoUrl = photoUrl,
    duplicateFlag = duplicateFlag,
    duplicateReferenceId = duplicateReferenceId,
    createdByUserId = createdByUserId,
)

fun SupplyEntry.toEntity(): SupplyEntryEntity = SupplyEntryEntity(
    id = id,
    apartmentId = apartmentId,
    vendorId = vendorId,
    hardnessPpm = hardnessPpm,
    capturedAtEpochMillis = capturedAt.toEpochMilli(),
    latitude = latitude,
    longitude = longitude,
    gpsAccuracyMeters = gpsAccuracyMeters,
    vehicleNumber = vehicleNumber,
    remarks = remarks,
    photoUrl = photoUrl,
    duplicateFlag = duplicateFlag,
    duplicateReferenceId = duplicateReferenceId,
    createdByUserId = createdByUserId,
)
