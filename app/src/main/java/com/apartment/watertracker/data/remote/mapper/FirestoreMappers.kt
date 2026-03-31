package com.apartment.watertracker.data.remote.mapper

import com.apartment.watertracker.data.local.entity.SupplyEntryEntity
import com.apartment.watertracker.data.local.entity.VendorEntity
import com.apartment.watertracker.data.remote.model.FirestoreApartmentDto
import com.apartment.watertracker.data.remote.model.FirestoreOperatorInviteDto
import com.apartment.watertracker.domain.model.ApartmentProfile
import com.apartment.watertracker.data.remote.model.FirestoreSupplyEntryDto
import com.apartment.watertracker.data.remote.model.FirestoreUserProfileDto
import com.apartment.watertracker.data.remote.model.FirestoreVendorDto
import com.apartment.watertracker.domain.model.AppUser
import com.apartment.watertracker.domain.model.OperatorInvite
import com.apartment.watertracker.domain.model.UserRole
import com.google.firebase.auth.FirebaseUser
import java.time.Instant
import java.time.ZoneOffset

fun VendorEntity.toFirestoreDto(): FirestoreVendorDto = FirestoreVendorDto(
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

fun FirestoreVendorDto.toEntity(id: String): VendorEntity = VendorEntity(
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

fun SupplyEntryEntity.toFirestoreDto(): FirestoreSupplyEntryDto = FirestoreSupplyEntryDto(
    apartmentId = apartmentId,
    vendorId = vendorId,
    hardnessPpm = hardnessPpm,
    capturedAtEpochMillis = capturedAtEpochMillis,
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

fun FirestoreSupplyEntryDto.toEntity(id: String): SupplyEntryEntity = SupplyEntryEntity(
    id = id,
    apartmentId = apartmentId,
    vendorId = vendorId,
    hardnessPpm = hardnessPpm,
    capturedAtEpochMillis = capturedAtEpochMillis,
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

fun FirebaseUser.toAppUser(
    role: UserRole = UserRole.ADMIN,
    apartmentId: String,
    apartmentName: String?,
): AppUser = AppUser(
    id = uid,
    name = displayName ?: "Apartment User",
    email = email.orEmpty(),
    role = role,
    apartmentId = apartmentId,
    apartmentName = apartmentName,
)

fun AppUser.toFirestoreDto(): FirestoreUserProfileDto = FirestoreUserProfileDto(
    name = name,
    email = email,
    role = role.name,
    apartmentId = apartmentId,
    apartmentName = apartmentName,
)

fun FirestoreUserProfileDto.toRole(): UserRole = when (role.uppercase()) {
    "OPERATOR" -> UserRole.OPERATOR
    else -> UserRole.ADMIN
}

fun FirestoreApartmentDto.toDomain(id: String): ApartmentProfile = ApartmentProfile(
    id = id,
    name = name,
    createdByUserId = createdByUserId,
    subscriptionStatus = subscriptionStatus.ifBlank { "ACTIVE" },
    subscriptionExpiresAt = subscriptionExpiresAtEpochMillis?.let { Instant.ofEpochMilli(it) },
)

fun FirestoreUserProfileDto.toAppUser(id: String): AppUser = AppUser(
    id = id,
    name = name,
    email = email,
    role = toRole(),
    apartmentId = apartmentId,
    apartmentName = apartmentName,
)

fun OperatorInvite.toFirestoreDto(): FirestoreOperatorInviteDto = FirestoreOperatorInviteDto(
    apartmentId = apartmentId,
    email = email,
    role = role.name,
    status = status,
    createdAtEpochMillis = createdAt.toEpochMilli(),
    createdByUserId = createdByUserId,
)

fun FirestoreOperatorInviteDto.toDomain(id: String): OperatorInvite = OperatorInvite(
    id = id,
    apartmentId = apartmentId,
    email = email,
    role = when (role.uppercase()) {
        "ADMIN" -> UserRole.ADMIN
        else -> UserRole.OPERATOR
    },
    status = status,
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    createdByUserId = createdByUserId,
)
