package com.apartment.watertracker.data.remote.mapper

import com.apartment.watertracker.data.local.entity.SupplyEntryEntity
import com.apartment.watertracker.data.local.entity.VendorEntity
import com.apartment.watertracker.data.remote.model.FirestoreApartmentDto
import com.apartment.watertracker.data.remote.model.FirestoreOperatorInviteDto
import com.apartment.watertracker.domain.model.ApartmentProfile
import com.apartment.watertracker.data.remote.model.FirestoreSupplyEntryDto
import com.apartment.watertracker.data.remote.model.FirestoreUserProfileDto
import com.apartment.watertracker.data.remote.model.FirestoreVendorDto
import com.apartment.watertracker.data.remote.model.FirestoreBidDto
import com.apartment.watertracker.data.remote.model.FirestoreInvoiceDto
import com.apartment.watertracker.data.remote.model.FirestoreTankerRequestDto
import com.apartment.watertracker.domain.model.AppUser
import com.apartment.watertracker.domain.model.Bid
import com.apartment.watertracker.domain.model.BidStatus
import com.apartment.watertracker.domain.model.Invoice
import com.apartment.watertracker.domain.model.InvoiceStatus
import com.apartment.watertracker.domain.model.OperatorInvite
import com.apartment.watertracker.domain.model.RequestStatus
import com.apartment.watertracker.domain.model.RequestUrgency
import com.apartment.watertracker.domain.model.TankerRequest
import com.apartment.watertracker.domain.model.UserRole
import com.google.firebase.auth.FirebaseUser
import java.time.Instant

fun Invoice.toFirestoreDto(): FirestoreInvoiceDto = FirestoreInvoiceDto(
    apartmentId = apartmentId,
    vendorId = vendorId,
    vendorName = vendorName,
    billingMonth = billingMonth,
    totalLiters = totalLiters,
    deliveryCount = deliveryCount,
    totalAmount = totalAmount,
    status = status.name,
    dueDateMillis = dueDate.toEpochMilli(),
    createdAtMillis = createdAt.toEpochMilli(),
    paymentReference = paymentReference
)

fun FirestoreInvoiceDto.toDomain(id: String): Invoice = Invoice(
    id = id,
    apartmentId = apartmentId,
    vendorId = vendorId,
    vendorName = vendorName,
    billingMonth = billingMonth,
    totalLiters = totalLiters,
    deliveryCount = deliveryCount,
    totalAmount = totalAmount,
    status = InvoiceStatus.valueOf(status),
    dueDate = Instant.ofEpochMilli(dueDateMillis),
    createdAt = Instant.ofEpochMilli(createdAtMillis),
    paymentReference = paymentReference
)

fun Bid.toFirestoreDto(): FirestoreBidDto = FirestoreBidDto(
    requestId = requestId,
    vendorId = vendorId,
    vendorName = vendorName,
    price = price,
    estimatedArrivalMillis = estimatedArrival.toEpochMilli(),
    createdAtMillis = createdAt.toEpochMilli(),
    status = status.name,
    notes = notes
)

fun FirestoreBidDto.toDomain(id: String): Bid = Bid(
    id = id,
    requestId = requestId,
    vendorId = vendorId,
    vendorName = vendorName,
    price = price,
    estimatedArrival = Instant.ofEpochMilli(estimatedArrivalMillis),
    createdAt = Instant.ofEpochMilli(createdAtMillis),
    status = BidStatus.valueOf(status),
    notes = notes
)

fun TankerRequest.toFirestoreDto(): FirestoreTankerRequestDto = FirestoreTankerRequestDto(
    apartmentId = apartmentId,
    apartmentName = apartmentName,
    requestedByUserId = requestedByUserId,
    quantityLiters = quantityLiters,
    urgency = urgency.name,
    createdAtMillis = createdAt.toEpochMilli(),
    status = status.name,
    notes = notes,
    bidsCount = bidsCount
)

fun FirestoreTankerRequestDto.toDomain(id: String): TankerRequest = TankerRequest(
    id = id,
    apartmentId = apartmentId,
    apartmentName = apartmentName,
    requestedByUserId = requestedByUserId,
    quantityLiters = quantityLiters,
    urgency = RequestUrgency.valueOf(urgency),
    createdAt = Instant.ofEpochMilli(createdAtMillis),
    status = RequestStatus.valueOf(status),
    notes = notes,
    bidsCount = bidsCount
)

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
    defaultCapacityLiters = defaultCapacityLiters,
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
    defaultCapacityLiters = defaultCapacityLiters,
)

fun SupplyEntryEntity.toFirestoreDto(): FirestoreSupplyEntryDto = FirestoreSupplyEntryDto(
    apartmentId = apartmentId,
    vendorId = vendorId,
    hardnessPpm = hardnessPpm,
    phLevel = phLevel,
    tdsPpm = tdsPpm,
    volumeLiters = volumeLiters,
    qualityRating = qualityRating,
    timelinessRating = timelinessRating,
    hygieneRating = hygieneRating,
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
    phLevel = phLevel,
    tdsPpm = tdsPpm,
    volumeLiters = volumeLiters,
    qualityRating = qualityRating,
    timelinessRating = timelinessRating,
    hygieneRating = hygieneRating,
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
    isSynced = true, // Records coming from Firestore are inherently synced
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
    fcmTokens = emptyList(),
)

fun AppUser.toFirestoreDto(): FirestoreUserProfileDto = FirestoreUserProfileDto(
    name = name,
    email = email,
    role = role.name,
    apartmentId = apartmentId,
    apartmentName = apartmentName,
    fcmTokens = fcmTokens,
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
    fcmTokens = fcmTokens,
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
