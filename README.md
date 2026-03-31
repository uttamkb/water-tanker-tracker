# Water Tracker

Starter blueprint for an Android app that tracks apartment water tanker deliveries.

## Stack

- Kotlin
- Jetpack Compose
- Hilt
- Room
- Firebase Auth
- Firestore
- CameraX
- ML Kit barcode scanning
- Google Play Services Location

## First setup

1. Open the project in Android Studio.
2. Add your `google-services.json` file to `app/`.
3. Sync Gradle.
4. Create a debug launcher icon if Android Studio asks for one.
5. Review the SaaS Firestore model in [firestore.rules](/Users/uttamkumar_barik/Documents/codex/firestore.rules) and [docs/firestore-saas-setup.md](/Users/uttamkumar_barik/Documents/codex/docs/firestore-saas-setup.md).
6. Start implementing the feature repositories and ViewModels described in [docs/android-starter-blueprint.md](/Users/uttamkumar_barik/Documents/codex/docs/android-starter-blueprint.md).

## Current status

This scaffold includes:

- Gradle version catalog
- application module
- Compose navigation shell
- feature folders and placeholder screens
- domain models and repository contracts
- Room entities, DAOs, and database
- Hilt dependency wiring
- Firebase Auth wiring for Google sign-in
- apartment-scoped Firestore sync layered on Room for SaaS-ready multi-tenant data
- Firestore rules/config files for tenant isolation
- real device GPS capture through Fused Location Provider
- real QR scanning flow through CameraX and ML Kit
- real vendor QR generation with share/print support
- apartment setup and operator invite admin screens
- ViewModels for login, dashboard, vendors, scan, entries, and reports
- duplicate warning logic and local entry persistence flow
- blueprint documentation for the next implementation phase
