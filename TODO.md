# WaterTracker Implementation Roadmap

This file tracks the progress of the WaterTracker development and release.

## ✅ Phase 1: MVP - The "Utility & Audit" Phase [COMPLETED]
*Goal: Solve the "Audit" and "Manual Entry" problem with high-utility, offline-first tools.*

## ✅ Phase 2: The "Trust & Reputation" Phase [COMPLETED]
*Goal: Build the "Moat" by introducing qualitative data (Quality & Rating).*

## ✅ Phase 3: The "Marketplace" Phase [COMPLETED]
*Goal: Evolve from an internal tool to a B2B marketplace bridging apartments and suppliers.*

## ✅ Phase 4: Scale & Intelligence [COMPLETED]
*Goal: Predictive analytics and automated quality assurance.*

---

## 🚀 Phase 5: Production Release Planning [UP NEXT]
*Goal: Secure, sign, and publish the application to the Google Play Store.*

### 🔑 Epic 12: Security & Signing [DONE]
- [x] **Task 12.1: Production Keystore Generation**
    - [x] Create a `release.keystore` file securely.
    - [x] Configure `signingConfigs` in `app/build.gradle.kts`.
    - [x] Store credentials in `local.properties` (non-git).
- [x] **Task 12.2: ProGuard/R8 Hardening**
    - [x] Enable `isMinifyEnabled = true` for release build.
    - [x] Test release build for common reflection crashes (Hilt/Room).

### ☁️ Epic 13: Firebase Production Setup [DONE]
- [x] **Task 13.1: Release Fingerprints**
    - [x] Extract SHA-1/SHA-256 from production keystore.
    - [x] Add fingerprints to Firebase Console.
- [x] **Task 13.2: Firestore/Storage Rules Audit**
    - [x] Verify multi-tenant isolation rules.
    - [x] Ensure public access is completely disabled.

### 📱 Epic 14: Play Store Listing & Compliance [IN PROGRESS]
- [ ] **Task 14.1: Store Assets**
    - [ ] High-res App Icon (512x512).
    - [ ] Feature Graphic (1024x500).
    - [ ] 4-8 Smartphone Screenshots.
- [ ] **Task 14.2: Legal & Privacy**
    - [ ] Draft Privacy Policy focusing on Camera & Location usage.
    - [ ] Fill out "Data Safety" questionnaire in Play Console.
- [x] **Task 14.3: App Bundle Generation**
    - [x] Run `./gradlew bundleRelease`.
    - [x] Verify AAB size and contents.

### ✅ Epic 15: Production Hardening & Code Quality [COMPLETED]
- [x] **Task 15.1: Critical Stability Fixes**
    - [x] Replace unsafe `as` casts in `DashboardViewModel` with safe casting.
    - [x] Implement `.onFailure` logging for all repository `runCatching` blocks.
- [x] **Task 15.2: Persistence & Security**
    - [x] Remove `fallbackToDestructiveMigration()` from Room configuration.
    - [x] Move Razorpay Webhook Secret to Firebase environment variables (Code-side update).
- [x] **Task 15.3: UI Resilience**
    - [x] Refactor `DashboardViewModel` to handle Flow exceptions without terminating the state stream.
    - [x] Replace hardcoded tanker rates with actual data from marketplace bids (Updated with production logic).

### 🧪 Epic 16: Pre-Release Testing [IN PROGRESS]
- [ ] **Task 16.1: Full Feature Audit**
    - [ ] Complete all items in `TESTING_CHECKLIST.md`.
    - [ ] Verify UI consistency on physical devices.
- [ ] **Task 16.2: Final Stability Pass**
    - [ ] Perform "Monkey Test" (Stress test UI taps).
    - [ ] Verify zero crashes in Logcat during standard journeys.

### 🚀 Epic 17: Play Store Listing & Rollout
- [ ] **Task 17.1: Internal Testing Track**
    - [ ] Upload AAB to Play Console Internal Track.
    - [ ] Invite first 10 "Society Admins" for beta feedback.
- [ ] **Task 17.2: Production Rollout**
    - [ ] Promote to Production with 10% staged rollout.

---

## 🧪 Testing & Quality
- [x] **PO Audit:** All development phases verified (See `TESTING_COMPLETION_REPORT.md`).
- [x] **Unit Tests:** 100% Pass Rate.
- [x] **Simulator Check:** UI/UX verified on Pixel 9 Emulator.
