# Water Tracker Android Starter Blueprint

This workspace now contains a clean native Android starter layout for the apartment water tanker tracking app.

## Product scope in this starter

- Google login for apartment staff
- Vendor registration and QR generation
- QR scan to create supply entries
- Mandatory GPS capture before save
- Duplicate entry warning before save
- Monthly reporting by vendor

## SaaS direction

The app is now moving toward a multi-tenant SaaS shape rather than a single-apartment app.

- users belong to an apartment tenant
- vendors belong to an apartment tenant
- supply entries belong to an apartment tenant
- Firestore data is scoped under `apartments/{apartmentId}/...`
- QR payloads are apartment-aware so a QR from one apartment does not resolve to another apartment's vendor records

The current code includes apartment-scoped data fields and collections, but full SaaS onboarding still needs a later phase:

- apartment creation and admin onboarding
- inviting users into an apartment
- tenant-aware Firestore security rules
- subscription/billing setup

## Project shape

```text
WaterTracker/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/apartment/watertracker/
│           ├── MainActivity.kt
│           ├── WaterTrackerApp.kt
│           ├── core/
│           │   ├── navigation/
│           │   └── ui/
│           ├── domain/
│           │   ├── model/
│           │   ├── repository/
│           │   └── usecase/
│           └── feature/
│               ├── auth/
│               ├── dashboard/
│               ├── entries/
│               ├── reports/
│               ├── scan/
│               └── vendors/
├── gradle/
│   └── libs.versions.toml
└── docs/
    └── android-starter-blueprint.md
```

## Why this structure

- `core/` holds reusable UI, navigation, and later shared utilities.
- `domain/` defines business models, repository contracts, and use cases.
- `feature/` groups UI and logic by product module so the app can grow without becoming tangled.
- `docs/` keeps product and engineering decisions close to the codebase.

## Recommended package expansion

As implementation starts, each feature should grow like this:

```text
feature/vendors/
├── data/
│   ├── local/
│   ├── remote/
│   └── repository/
├── domain/
│   └── usecase/
└── presentation/
    ├── VendorsScreen.kt
    ├── VendorDetailScreen.kt
    ├── AddVendorScreen.kt
    └── VendorsViewModel.kt
```

Do the same for `scan`, `entries`, and `reports`.

## Screen plan

- `LoginScreen`
  - Google sign-in
  - approved-user check
- `DashboardScreen`
  - quick stats
  - jump to scan/vendors/reports
- `VendorsScreen`
  - vendor list
  - add/edit vendor
  - generate and display QR
- `ScanScreen`
  - CameraX preview
  - ML Kit barcode recognition
- `SupplyEntryScreen`
  - vendor prefilled from QR
  - auto date/time
  - mandatory GPS state
  - hardness field
  - optional photo and remarks
  - duplicate warning
- `ReportsScreen`
  - month filter
  - vendor summary
  - entry audit list

## Data model starter

### Vendor

- `id`
- `apartmentId`
- `supplierName`
- `contactPerson`
- `phoneNumber`
- `alternatePhoneNumber`
- `address`
- `notes`
- `isActive`
- `qrValue`

### SupplyEntry

- `id`
- `apartmentId`
- `vendorId`
- `hardnessPpm`
- `capturedAt`
- `latitude`
- `longitude`
- `gpsAccuracyMeters`
- `vehicleNumber`
- `remarks`
- `photoUrl`
- `duplicateFlag`
- `duplicateReferenceId`
- `createdByUserId`

## Duplicate warning rule

Starter rule:

- fetch the latest entry for the same vendor
- compare current time with previous entry time
- if the gap is less than or equal to 60 minutes, show warning
- allow save anyway
- persist `duplicateFlag = true`

Later improvements:

- include vehicle number match
- include distance threshold between GPS points
- admin-configurable duplicate window

## GPS rule

For MVP, the save button should remain disabled until:

- location permission is granted
- GPS is turned on
- a current location is captured
- accuracy is within the acceptable range you decide

Recommended stored fields:

- latitude
- longitude
- accuracy
- capture timestamp

## Offline-first approach

Use these layers:

- `Room` as the source of truth on device
- `Firestore` for cloud sync across users/devices
- `WorkManager` for deferred sync when offline

Suggested sync behavior:

- save vendor and entry changes locally first
- mark unsynced records with a sync status
- push unsynced data in the background
- resolve conflicts with last-write-wins for edits, but never silently drop entries

## Firebase starter tasks

Before the app can run fully, add:

1. Firebase project
2. Android app registration with package `com.apartment.watertracker`
3. `google-services.json` in `app/`
4. Authentication
   - enable Google provider
5. Firestore database
6. Firebase Storage only if photo upload is kept

## Room starter entities to add next

- `VendorEntity`
- `SupplyEntryEntity`
- `SyncQueueEntity`

Starter DAOs:

- `VendorDao`
- `SupplyEntryDao`
- `SyncQueueDao`

Database:

- `WaterTrackerDatabase`

## Repositories to implement next

- `FirebaseAuthRepository`
- `OfflineFirstVendorRepository`
- `OfflineFirstSupplyEntryRepository`
- `LocationRepository`
- `QrScannerRepository`

## ViewModels to add next

- `LoginViewModel`
- `DashboardViewModel`
- `VendorsViewModel`
- `ScanViewModel`
- `SupplyEntryViewModel`
- `ReportsViewModel`

## Immediate build milestones

### Milestone 1

- Firebase login working
- navigation working
- static screens polished

Status:

- navigation shell is in place
- Firebase login wiring is in place
- screens now use ViewModels and local state

### Milestone 2

- Room database added
- vendor create/list working locally
- QR generation working

Status:

- Room database is in place
- vendor create/list is working through the local repository and Firestore sync
- QR generation is wired and printable/shareable from the vendor screen

### Milestone 3

- Camera scan working
- GPS capture working
- supply entry save working
- duplicate warning working

Status:

- scan flow is wired with CameraX + ML Kit and a manual fallback
- GPS capture is wired to the device location provider
- supply entry save works locally
- duplicate warning works against the last vendor entry

### Milestone 4

- Firestore sync
- monthly reporting
- flagged entry review

Status:

- Firestore-backed vendor and supply entry sync is wired
- monthly reporting reads from the local Room cache after refresh
- flagged entry review UI is still a next-step implementation

## SaaS notes for next phase

- Replace the default apartment fallback with a real apartment onboarding flow.
- Move from permissive tenant defaults to enforced apartment membership.
- Add Firestore security rules that limit reads and writes to the signed-in user's apartment.
- Add apartment branding and plan settings if this becomes a product sold to multiple apartment communities.

## Important implementation notes

- Keep QR content limited to a vendor identifier, not full vendor details.
- QR content should stay apartment-scoped for SaaS safety.
- Do not make photo mandatory.
- Operators should not be able to delete entries.
- Edits to supply entries should be admin-only and always audited.
- If GPS cannot be captured, do not allow save for normal operator flow.
