# WaterTracker Implementation Roadmap

This file tracks the progress of the WaterTracker development.

## ✅ Phase 1: MVP - The "Utility & Audit" Phase [COMPLETED]
*Goal: Solve the "Audit" and "Manual Entry" problem with high-utility, offline-first tools.*

### 📦 Epic 1: Multi-Tenant Data Core [DONE]
- [x] **Task 1.1:** Setup `ApartmentScopeProvider` for strict multi-tenancy.
- [x] **Task 1.2:** Implement Room `SupplyEntryEntity` and DAO.
- [x] **Task 1.3:** Setup `OfflineFirstSupplyEntryRepository` with Firestore sync logic.
- [x] **Task 1.4:** Build "Post-Scan Validation" UI.

### 🔍 Epic 2: QR Scanning & Entry Flow [DONE]
- [x] **Task 2.1:** Integrate CameraX with ML Kit for high-speed barcode scanning.
- [x] **Task 2.2:** Build `SupplyEntryScreen` with auto-filling vendor metadata.
- [x] **Task 2.3:** Capture GPS location/accuracy during scan event.

### 📊 Epic 3: Reporting & Exports [DONE]
- [x] **Task 3.1:** Implement "Digital Logbook" (Timeline View) with interactive expansion.
- [x] **Task 3.2:** Build Monthly Summary logic (Tanker counts, Volume totals).
- [x] **Task 3.3:** Implement CSV Export utility.
- [x] **Task 3.4:** Implement PDF Export utility using iText7 + WhatsApp sharing.

### 🛡️ Epic 4: Data Integrity & Sync (Robustness) [DONE]
- [x] **Task 4.1:** Refine `CheckDuplicateEntryUseCase` to handle accidental double-scans.
- [x] **Task 4.2:** Add "Sync Status" UI Indicator (🟢 Synced / 🟠 Pending Upload).

### 🎨 Epic 5: Premium UI/UX Overhaul [DONE]
- [x] **Task 5.1:** Implement "Midnight & Gold" Premium Design System.
- [x] **Task 5.2:** Apply Glassmorphism to Navigation and Overlays.
- [x] **Task 5.3:** Standardize Premium Card depth and Shimmer loading states.
- [x] **Task 5.4:** Implement Physics-based motion (Springs) and smooth transitions.

---

## ✅ Phase 2: The "Trust & Reputation" Phase [COMPLETED]
*Goal: Build the "Moat" by introducing qualitative data (Quality & Rating).*

### 🧪 Epic 6: Water Quality Logs [DONE]
- [x] **Task 6.1:** Add fields for pH, TDS, and Hardness level logging.
- [x] **Task 6.2:** Create "Quality Trend" charts for Apartment Admins.

### ⭐ Epic 7: Vendor Rating System [DONE]
- [x] **Task 7.1:** Implement post-delivery rating (Quality, Timeliness, Hygiene).
- [x] **Task 7.2:** Build "Vendor Leaderboard" based on aggregate ratings.

---

## ✅ Phase 3: The "Marketplace" Phase [COMPLETED]
*Goal: Evolve from an internal tool to a B2B marketplace bridging apartments and suppliers.*

### 📢 Epic 8: Vendor Lead Generation [DONE]
- [x] **Task 8.1:** "Request Tanker" broadcast feature (Notifying local registered vendors).
- [x] **Task 8.2:** Simple "Vendor Response/Bidding" interface.
- [x] **Task 8.3:** Push notifications for incoming bids/quotes.

### 💳 Epic 9: Integrated Payments & Billing [DONE]
- [x] **Task 9.1:** Integrated invoicing (Monthly auto-generation based on deliveries).
- [x] **Task 9.2:** Razorpay/UPI integration for one-click vendor payments.
- [x] **Task 9.3:** Manual "Mark Paid Offline" for bank/cheque payments.

---

## 🚀 Phase 4: Scale & Intelligence [COMPLETED]
*Goal: Predictive analytics and automated quality assurance.*

### 🧠 Epic 10: Smart Forecasting [DONE]
- [x] **Task 10.1:** Predict next tanker requirement based on historical usage.
- [x] **Task 10.2:** Low-water level alerts (IoT integration ready / Background Worker).

### 📋 Epic 11: Compliance & Audit [DONE]
- [x] **Task 11.1:** Export compliance ready Audit Logs for legal purposes (CSV Generation).
- [x] **Task 11.2:** Proof of location/presence logic natively handling non-repudiation (Replaced Digital Signatures).

---

## 🧪 Testing & Quality
- [x] **PO Audit:** Phase 1 Feature & UI Verification Audit (See `TESTING_COMPLETION_REPORT.md`).
- [x] Unit Tests for Duplicate detection logic.
- [ ] Instrumented Tests for Room migrations.
