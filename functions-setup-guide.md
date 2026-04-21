# Firebase Cloud Functions Setup Guide for Razorpay Integration

To implement the automated Razorpay subscription flow we discussed, you need a backend endpoint that Razorpay can "ping" (webhook) every time a payment succeeds. Firebase Cloud Functions is the perfect place for this since it integrates directly with your existing Firestore database.

Here are the step-by-step instructions to set this up in your project.

### 1. Initialize Firebase Functions
Open your terminal in your project root (`/Users/uttamkumar_barik/Documents/codex`) and run:

```bash
firebase init functions
```

*   **Language:** Choose `TypeScript` (recommended) or `JavaScript`.
*   **ESLint:** Choose `Yes` or `No` based on your preference.
*   **Install dependencies:** Choose `Yes`.

This will create a new `/functions` directory in your project containing an `index.js` or `index.ts` file.

### 2. Install Required Packages
Navigate into the newly created `functions` directory and install the Razorpay SDK and Firebase Admin SDK:

```bash
cd functions
npm install razorpay firebase-admin firebase-functions
```

### 3. Write the Webhook Logic
Open `functions/src/index.ts` (or `.js`) and add the following code. This function listens for an HTTP POST request from Razorpay, verifies its authenticity, and updates the `apartment` document in Firestore.

```typescript
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import * as crypto from "crypto";

admin.initializeApp();
const db = admin.firestore();

// IMPORTANT: Set these in your Firebase environment variables later!
const RAZORPAY_WEBHOOK_SECRET = "your_webhook_secret_from_razorpay";

export const razorpayWebhook = functions.https.onRequest(async (req, res) => {
    // 1. Verify the webhook signature to ensure it's actually from Razorpay
    const signature = req.headers["x-razorpay-signature"] as string;
    const body = req.rawBody.toString();

    const expectedSignature = crypto
        .createHmac("sha256", RAZORPAY_WEBHOOK_SECRET)
        .update(body)
        .digest("hex");

    if (expectedSignature !== signature) {
        console.error("Invalid signature!");
        res.status(400).send("Invalid signature");
        return;
    }

    // 2. Parse the payload
    const payload = req.body;
    const event = payload.event;

    console.log(`Received event: ${event}`);

    // 3. Handle successful subscription payments
    if (event === "subscription.charged") {
        const subscription = payload.payload.subscription.entity;
        
        // This relies on you passing the apartmentId into the Razorpay "notes" 
        // field when you initially create the subscription link.
        const apartmentId = subscription.notes?.apartmentId;

        if (!apartmentId) {
            console.error("No apartmentId found in notes!");
            res.status(400).send("Missing apartmentId in notes");
            return;
        }

        try {
            // Calculate expiry (e.g., 30 days from now)
            const nextMonth = new Date();
            nextMonth.setDate(nextMonth.getDate() + 30);

            // 4. Unlock the app for this apartment!
            await db.collection("apartments").doc(apartmentId).update({
                subscriptionStatus: "ACTIVE",
                subscriptionExpiresAtEpochMillis: nextMonth.getTime(),
            });

            console.log(`Successfully renewed apartment: ${apartmentId}`);
            res.status(200).send("Success");
        } catch (error) {
            console.error(`Error updating Firestore for apartment ${apartmentId}`, error);
            res.status(500).send("Internal Server Error");
        }
    } else {
        // Acknowledge other events but do nothing
        res.status(200).send("Event not handled");
    }
});
```

### 4. Deploy the Function
Deploy your function to Firebase:

```bash
firebase deploy --only functions
```

Firebase will give you a **Function URL** (e.g., `https://us-central1-your-project.cloudfunctions.net/razorpayWebhook`).

### 5. Configure Razorpay
1.  Log in to your Razorpay Dashboard.
2.  Go to **Settings > Webhooks**.
3.  Click **Add New Webhook**.
4.  Paste your **Firebase Function URL**.
5.  Create a strong "Secret" (and update your Cloud Function code/env config to match it).
6.  Under **Active Events**, select `subscription.charged`.
7.  Save!

### The End-to-End Flow
Now, when an Admin's free trial expires on your app:
1. The app locks and they click "Renew via UPI".
2. They pay ₹999 on your Razorpay page (where you captured their `apartmentId` in the `notes` field).
3. Razorpay charges their account.
4. Razorpay sends the `subscription.charged` webhook to your Cloud Function.
5. Your Cloud Function updates the `apartments` Firestore document.
6. The Android App instantly sees the Firestore update (thanks to snapshot listeners) and unlocks the dashboard real-time!
