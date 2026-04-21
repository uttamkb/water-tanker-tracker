# Skill: Product Owner Feature Verification

This skill enables the agent to act as a **Product Owner (PO)** to verify that implemented features meet both functional requirements and "Premium" experience standards.

## 🎯 Objectives
- Validate that technical code aligns with business goals defined in `PRODUCT_ROADMAP.md`.
- Ensure UI/UX implementations match the high-end "Premium" aesthetic defined in `ROADMAP.md`.
- Verify data integrity, offline-first behavior, and multi-tenant isolation.

## 🛠 Verification Procedures

### 1. Visual & Experience Audit (UI/UX)
Verify the "Premium" look and feel by checking the following:
- **Surface Depth:** Are all cards using the `PremiumCard` component? (Subtle borders, specific elevations).
- **Glassmorphism:** Do navigation bars or overlays use `GlassSurface` with appropriate transparency?
- **Motion Physics:** Do interactive elements use `animateContentSize` or spring-based `animateFloatAsState`?
- **Loading States:** Are circular progress bars replaced with `shimmerEffect` skeletons?
- **Typography:** Does the code strictly use `MaterialTheme.typography` tokens rather than hardcoded sizes?

### 2. Functional Verification
Map features to code implementation:
- **Offline-First:** Verify the Repository uses a `LocalSource` (Room) as the primary truth and syncs via a `RemoteSource` (Firestore).
- **Multi-Tenancy:** Ensure every Firestore query is scoped using `apartmentId` from the `ApartmentScopeProvider`.
- **Scanning Flow:** Confirm the `ScanScreen` correctly resolves the vendor and navigates to `SupplyEntryScreen`.
- **Reporting:** Verify the `ReportsViewModel` correctly calculates sums and handles CSV/PDF exports.

### 3. Logic & Integrity Verification
- **Unit Tests:** Run `./gradlew test` to ensure core business rules (like duplicate detection) are passing.
- **Data Mappers:** Check `DomainMappers.kt` and `FirestoreMappers.kt` to ensure all fields (including `isSynced`) are handled.

## 📝 Generating a Verification Report
When asked to verify a feature, the agent should:
1.  **Locate** the relevant files (UI, ViewModel, Repository).
2.  **Analyze** the implementation against the criteria above.
3.  **Cross-reference** with `PRODUCT_ROADMAP.md`.
4.  **Produce** a "Product Owner Audit Report" in the following format:

### [Feature Name] - PO Verification Report
- **Status:** [✅ PASSED / ⚠️ PARTIAL / ❌ FAILED]
- **Functional Requirements:** [List of met/unmet business rules]
- **Experience Standards:** [List of UI/UX standards met]
- **Technical Integrity:** [Notes on architecture/multi-tenancy/sync]
- **Action Items:** [Required fixes if any]
