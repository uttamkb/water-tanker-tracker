# WaterTracker Issue Tracker

This document logs critical stability issues and build warnings found during the production audit.

| ID | Issue Description | Severity | Status | Resolution |
|:---|:---|:---:|:---:|:---|
| #001 | Unsafe type casting in `DashboardViewModel` during flow `combine`. | Critical | ✅ DONE | Implemented safe casting (`as?`) with empty-list fallbacks and `@Suppress("UNCHECKED_CAST")`. |
| #002 | Native library stripping warnings (`libbarhopper_v3.so`, etc). | Warning | ✅ DONE | Verified as expected behavior for ML Kit / CameraX libraries. No functional impact. |
| #003 | Firebase Serialization: Missing ProGuard rules for DTOs. | Critical | ✅ DONE | Added `-keep class ...data.remote.model.** { *; }` to `proguard-rules.pro`. |
