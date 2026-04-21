# WaterTracker: Implementation & User Journey Playbook

This document outlines the standard operating procedures, user personas, and their respective journeys within the WaterTracker ecosystem.

## 👥 Personas & Roles

| Persona | Role in App | Primary Responsibility |
| :--- | :--- | :--- |
| **Platform Owner** | Super Admin | Onboarding new Societies, managing subscriptions, and platform-wide health. |
| **Society Admin** | Admin | Configuration, Vendor Management, Billing, Analytics. |
| **Security Guard** | Operator | Real-time scan and entry of tankers at the gate. |
| **Facility Manager** | Admin/Operator | Quality auditing, checking logs, and settling vendor payments. |
| **Vendor** | External (QR) | Providing water and receiving unique digital identifiers. |

---

## 🏗️ Initial Setup (The "Day 0" Journey)

### 1. Download & Installation
- **Who:** Society Admin / Security Guard
- **Action:** Search for **"WaterTracker"** on the Google Play Store -> Install the application.

### 2. Apartment Onboarding
There are two ways an apartment enters the system:
- **Path A (Self-Serve):** A **Society Admin** signs in via Google -> Selects "Create New Apartment" -> Enters Building Name.
- **Path B (Managed):** The **Platform Owner** onboards the apartment via the "All Apartments" console on behalf of the society.

**Outcome:** A unique `apartmentId` is generated, and the primary user is assigned the **ADMIN** role.

### 3. Subscription Activation
- **Step 1:** After onboarding, the Society Admin will see a **"Subscription Inactive"** alert on the Dashboard.
- **Step 2:** Admin clicks **"Renew Subscription via UPI"**.
- **Step 3:** Redirects to the payment portal (Razorpay) to pay the platform fee.
- **Path B (Manual):** Alternatively, the Platform Owner can manually set the status to **ACTIVE** in the "All Apartments" console after receiving offline payment.
- **Outcome:** The app is unlocked for all members of that society.

### 4. Vendor Registration
- **Who:** Society Admin / Facility Manager
- **Action:** Go to "Vendor Desk" -> Click "Register Vendor" -> Enter Supplier Name, Phone, and default Tanker Capacity (e.g., 5000L).
- **Outcome:** A unique QR code is generated for that vendor.
- **Next Step:** Print the QR code or share the PDF with the Vendor to stick on their tanker.

### 3. Team Invitation
- **Who:** Society Admin
- **Action:** Go to "Team Access" -> "Invite Member" -> Enter the Security Guard's email and select **OPERATOR** role.
- **Outcome:** Guard receives an email; once they sign in, they are automatically scoped to this apartment.

---

## 🔄 Daily Operations (The "Live" Journeys)

### Journey A: The Gate Entry (High Frequency)
- **Actor:** Security Guard
- **Flow:** 
    1. Tanker arrives at the gate.
    2. Guard opens app -> **Scan Tanker**.
    3. Scans Vendor QR on the truck.
    4. System auto-identifies Vendor and default volume.
    5. Guard enters **TDS Level** (Mandatory per policy).
    6. Guard clicks **Save Entry**.
- **Outcome:** Delivery is logged, GPS location is verified, and a push notification is sent to the Admin.

### Journey B: The Marketplace Request (As Needed)
- **Actor:** Facility Manager
- **Flow:**
    1. Water level is low.
    2. Manager goes to **Request Tanker**.
    3. Selects "Urgent" -> Quantity: 10,000L -> Broadcast.
    4. Local vendors receive the lead (Future feature) or Manager notifies them.
    5. Manager checks **Bids** -> Accepts the best price/ETA.
- **Outcome:** Fulfills the water gap efficiently.

---

## 📊 End-of-Month (The "Settlement" Journeys)

### Journey C: Quality Audit & Analytics
- **Actor:** Facility Manager
- **Flow:** 
    1. Manager opens **Analytics**.
    2. Reviews the pH and TDS trends for the month.
    3. Identifies if a specific Vendor is supplying poor quality water.
- **Outcome:** Data-driven decisions on which vendors to keep or penalize.

### Journey D: Invoicing & Payment
- **Actor:** Society Admin
- **Flow:**
    1. Admin goes to **Billing & Payments**.
    2. Clicks "Generate Invoices" -> System calculates totals per vendor.
    3. Admin reviews "Vendor Ledger".
    4. **Option 1 (App):** Admin clicks "Pay via App" -> Razorpay opens -> Settles via UPI.
    5. **Option 2 (Bank):** Admin pays via Bank Portal -> Clicks "Mark Paid Offline" -> Enters Bank UTR.
- **Outcome:** Payment status is synced; Vendor record is updated.

---

## 🛡️ Trust & Security Protocols
1. **GPS Verification:** All entries capture latitude/longitude to ensure the guard is actually at the gate.
2. **Duplicate Detection:** If a guard tries to scan the same truck twice within 30 mins, the app triggers a "Possible Duplicate" alert.
3. **Multi-Tenancy:** Data for *Sowparnika Sanvi* is never visible to *Prestige Ferns* users.
