# Agent Instructions: WaterTracker Project

You are an expert Android Developer assisting with the development of **WaterTracker**, a multi-tenant SaaS application for tracking apartment water tanker deliveries.

## Project Overview
WaterTracker is designed to manage water tanker logistics for apartment complexes. It uses a multi-tenant architecture where data is isolated at the apartment level using Firestore rules and a scoped repository pattern.

**Key Features:**
- **Auth:** Firebase Authentication (Google Sign-in).
- **Multi-tenancy:** Apartment-scoped Firestore sync with Room for offline support.
- **Scanning:** QR code scanning for vendor identification using CameraX and ML Kit.
- **Location:** Real-time GPS capture for delivery verification.
- **Admin Flows:** Apartment setup, operator invites, and vendor management.
- **Data Persistence:** Room Database acting as a local cache for Firestore.

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Dependency Injection:** Hilt
- **Local Database:** Room
- **Backend/Cloud:** Firebase (Auth, Firestore, Cloud Functions)
- **Media/Vision:** CameraX, ML Kit (Barcode Scanning)
- **Location:** Google Play Services (Fused Location Provider)
- **Architecture:** Clean Architecture (Domain, Data, UI layers) with Repository pattern and ViewModel-driven UI.

## Core Architecture Principles
1. **Offline-First:** All feature implementations must prioritize Room as the single source of truth for the UI, with background synchronization between Room and Firestore.
2. **Multi-Tenant Security:** Ensure all Firestore queries and Repository implementations are scoped to the current `apartmentId`.
3. **Unidirectional Data Flow (UDF):** Use ViewModels to expose State/UI State and handle User Intents.
4. **Modular Features:** Features are organized into feature-specific packages (e.g., `feature/dashboard`, `feature/scan`) containing their own UI, ViewModel, and domain logic.

## Development Guidelines
- **UI Components:** Use the `PrimaryScaffold` and existing design system components found in `core/ui/components` to maintain consistency.
- **Error Handling:** Implement robust error handling for network failures and permission denials (Location, Camera).
- **Dependency Management:** Use the Gradle Version Catalog (`libs.versions.toml`) for all dependencies.
- **Testing:** When adding new logic, consider adding corresponding unit tests in the `test` source sets.

## Project Roadmap (Current Status)
- [x] Project Scaffold & Gradle Setup
- [x] Firebase Auth & Hilt Wiring
- [x] Room & Firestore Sync Infrastructure
- [x] QR Scanning Flow (CameraX + ML Kit)
- [x] Location Capture Implementation
- [ ] Complete Feature Repositories (Implementation of contracts)
- [ ] UI/UX Polishing for all features
- [ ] Cloud Functions implementation for notifications/alerts
