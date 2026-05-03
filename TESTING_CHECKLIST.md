# WaterTracker: Pre-Release Testing Checklist

Use this checklist to verify all features before rolling out to the Play Store. It is recommended to use **two physical devices** to test multi-user interactions.

## 🔑 1. Authentication & Onboarding
- [ ] **Google Sign-In:** Login works for a brand new user.
- [ ] **Apartment Creation:** New user can create a new apartment building.
- [ ] **Persistent Session:** App remembers the user after closing and reopening.
- [ ] **Logout:** User can log out and log back in as a different user.

## 🏢 2. Apartment & Team Management
- [ ] **Apartment Setup:** Society Admin can change the building name.
- [ ] **Team Invitations:** Admin can invite a member via email (Role: Security Guard).
- [ ] **Auto-Join:** Invited member (e.g., Guard) logs in and is automatically added to the correct apartment.
- [ ] **Role Enforcement:** Security Guard *cannot* see "Billing" or "Apartment Setup" in the menu.

## 🚚 3. Vendor Management
- [ ] **Registration:** Admin can add a new vendor with name, phone, and capacity.
- [ ] **QR Generation:** App generates a unique QR code for the new vendor.
- [ ] **Vendor List:** All vendors are displayed correctly with their status.
- [ ] **Active/Inactive:** Admin can toggle a vendor's active status.

## 📸 4. Gate Operations (The "Hot" Path)
- [ ] **QR Scanning:** App opens camera and successfully reads a vendor QR code.
- [ ] **Location Capture:** App asks for location permission and captures coordinates.
- [ ] **TDS/Hardness Entry:** Guard can enter water quality metrics.
- [ ] **Duplicate Alert:** Try scanning the same vendor twice within 5 minutes; verify the "Duplicate Warning" appears.
- [ ] **Entry Sync:** Verify the entry appears in the "Recent Deliveries" on the dashboard immediately.

## 🛒 5. Marketplace & Bidding
- [ ] **Request Tanker:** Facility Manager can create a request for 10k Liters.
- [ ] **Bid Visibility:** (Simulate/Mock) Admin can see incoming bids on the request.
- [ ] **Accept Bid:** Admin can accept a bid, and the request status updates to "Accepted".

## 💳 6. Billing & Payments
- [ ] **Invoice Generation:** Admin sees monthly totals grouped by vendor.
- [ ] **Razorpay Flow:** Tapping "Pay" opens the Razorpay checkout (use Test Mode).
- [ ] **Offline Settlement:** Admin can mark an invoice as "Paid Offline" by entering a reference ID.

## 📊 7. Analytics & Intelligence
- [ ] **Smart Forecast:** Dashboard shows "Next Tanker Expected" date (requires at least 3 historical entries).
- [ ] **Low Water Alert:** (Simulate) Check if the "LOW WATER" red banner appears when levels are predicted to be low.
- [ ] **PDF Export:** Facility Manager can generate and open a monthly PDF report.
- [ ] **CSV Export:** Admin can export and share the Audit Log CSV.

## 🔌 8. Robustness & Offline
- [ ] **Airplane Mode Scan:** Turn off internet -> Scan a tanker -> Save. Verify it stays in "Pending Sync".
- [ ] **Reconnect Sync:** Turn on internet -> Verify the pending entry is automatically uploaded to Firestore.
- [ ] **Error Handling:** Verify the app doesn't crash if location is denied (should show a helpful message).

---

## 🧪 Recommended Test Scenario (E2E)
1. **Admin Device:** Log in -> Create "Sunset Apartments" -> Add Vendor "Aqua Flow".
2. **Admin Device:** Invite `test-guard@gmail.com` as Guard.
3. **Guard Device:** Log in as `test-guard@gmail.com` -> Confirm they see "Sunset Apartments".
4. **Guard Device:** Scan "Aqua Flow" QR -> Log 5000L delivery.
5. **Admin Device:** Check dashboard -> Verify the delivery notification and entry exist.
