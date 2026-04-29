# Android Senior Developer Code Review Skill

This skill allows the agent to perform high-level architectural audits and detailed code reviews for modern Android applications (Jetpack Compose, Clean Architecture, Hilt, Room, Flow).

## Audit Checklist

### 1. Stability & Error Handling
- [ ] **Unsafe Casts:** Check `combine` or `zip` operators in ViewModels for unsafe `as` casts. Use `as?` with fallbacks.
- [ ] **Silent Exceptions:** Audit `runCatching` blocks. Ensure `.onFailure` handles logging or UI state updates.
- [ ] **Flow Termination:** Verify `.catch {}` usage in ViewModels. Ensure non-fatal errors don't permanently kill the UI update loop.

### 2. Data Persistence (Room)
- [ ] **Migration Safety:** Identify `fallbackToDestructiveMigration()`. Ensure it's removed or managed via staged rollouts before Prod.
- [ ] **Thread Safety:** Ensure DAOs are accessed via Repository layer with proper Coroutine Dispatchers.

### 3. Networking & Remote Data (Firebase/Firestore)
- [ ] **Serialization:** Ensure all Firestore DTOs have no-arg constructors (or default values in Kotlin) and are protected in `proguard-rules.pro`.
- [ ] **Auth Sync:** Verify user profile creation/sync logic during Google Sign-in to prevent orphaned users.

### 4. Security & Compliance
- [ ] **Secret Hardcoding:** Search for API Keys, Webhook secrets, or hardcoded URLs in both Kotlin and Cloud Functions.
- [ ] **Permission Leakage:** Verify `android:exported` flags in `AndroidManifest.xml`.
- [ ] **Sensitive Logs:** Ensure PII (email, phone, name) isn't being logged to `Log.d` or `Log.e` in Release builds.

### 5. Production Hardening (R8/ProGuard)
- [ ] **Keep Rules:** Verify rules for libraries that use reflection (Gson, Moshi, Retrofit, Firebase, Razorpay, Hilt).

## How to use
Run this skill whenever a new feature is completed or before a production release to generate a "Hardening Report".
