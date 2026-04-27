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

### 🔑 Epic 12: Security & Signing
- [ ] **Task 12.1: Production Keystore Generation**
    - [ ] Create a `release.keystore` file securely.
    - [ ] Configure `signingConfigs` in `app/build.gradle.kts`.
    - [ ] Store credentials in `local.properties` (non-git).
- [ ] **Task 12.2: ProGuard/R8 Hardening**
    - [ ] Enable `isMinifyEnabled = true` for release build.
    - [ ] Test release build for common reflection crashes (Hilt/Room).

### ☁️ Epic 13: Firebase Production Setup
- [ ] **Task 13.1: Release Fingerprints**
    - [ ] Extract SHA-1/SHA-256 from production keystore.
    - [ ] Add fingerprints to Firebase Console.
- [ ] **Task 13.2: Firestore/Storage Rules Audit**
    - [ ] Verify multi-tenant isolation rules.
    - [ ] Ensure public access is completely disabled.

### 📱 Epic 14: Play Store Listing & Compliance
- [ ] **Task 14.1: Store Assets**
    - [ ] High-res App Icon (512x512).
    - [ ] Feature Graphic (1024x500).
    - [ ] 4-8 Smartphone Screenshots.
- [ ] **Task 14.2: Legal & Privacy**
    - [ ] Draft Privacy Policy focusing on Camera & Location usage.
    - [ ] Fill out "Data Safety" questionnaire in Play Console.
- [ ] **Task 14.3: App Bundle Generation**
    - [ ] Run `./gradlew bundleRelease`.
    - [ ] Verify AAB size and contents.

### 🧪 Epic 15: Internal Testing & Rollout
- [ ] **Task 15.1: Internal Testing Track**
    - [ ] Upload AAB to Play Console Internal Track.
    - [ ] Invite first 10 "Society Admins" for beta feedback.
- [ ] **Task 15.2: Production Rollout**
    - [ ] Promote to Production with 10% staged rollout.

---

## 🧪 Testing & Quality
- [x] **PO Audit:** All development phases verified (See `TESTING_COMPLETION_REPORT.md`).
- [x] **Unit Tests:** 100% Pass Rate.
- [x] **Simulator Check:** UI/UX verified on Pixel 9 Emulator.
