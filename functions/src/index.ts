import {onRequest} from "firebase-functions/v2/https";
import {onDocumentCreated} from "firebase-functions/v2/firestore";
import {setGlobalOptions} from "firebase-functions/v2";
import * as admin from "firebase-admin";
import * as crypto from "crypto";

// Set global options to us-central1 (default) or change to your specific region
setGlobalOptions({region: "us-central1"});

if (!admin.apps.length) {
  admin.initializeApp();
}
const db = admin.firestore();

// IMPORTANT: Set these in your Firebase environment variables later!
const RAZORPAY_WEBHOOK_SECRET = "your_webhook_secret_from_razorpay";

export const razorpayWebhook = onRequest(async (req, res) => {
  // 1. Verify the webhook signature
  const signature = req.headers["x-razorpay-signature"];

  if (typeof signature !== "string") {
    console.error("Missing or invalid signature header!");
    res.status(400).send("Invalid signature");
    return;
  }

  // Firebase Functions populate req.rawBody automatically for text/json
  if (!req.rawBody) {
    console.error("Missing raw body!");
    res.status(400).send("Missing raw body");
    return;
  }

  const body = req.rawBody.toString();

  const expectedSignature = crypto
    .createHmac("sha256", RAZORPAY_WEBHOOK_SECRET)
    .update(body)
    .digest("hex");

  if (expectedSignature !== signature) {
    console.error("Signature mismatch!");
    res.status(400).send("Invalid signature");
    return;
  }

  // 2. Parse the payload
  const payload = req.body;
  const event = payload?.event;

  console.log(`Received Razorpay event: ${event}`);

  // 3. Handle successful subscription payments
  if (event === "subscription.charged") {
    const subscription = payload?.payload?.subscription?.entity;

    // This relies on you passing the apartmentId into the Razorpay "notes"
    // field when you initially create the subscription link.
    const apartmentId = subscription?.notes?.apartmentId;

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
      console.error(`Error updating Firestore: ${apartmentId}`, error);
      res.status(500).send("Internal Server Error");
    }
  } else {
    // Acknowledge other events but do nothing
    res.status(200).send("Event not handled");
  }
});

/**
 * Triggers when a new tanker entry is created.
 * Sends a push notification to all Admins of that apartment.
 */
export const onDeliveryCreated = onDocumentCreated(
  {
    document: "apartments/{apartmentId}/deliveries/{deliveryId}",
    region: "us-central1",
  },
  async (event) => {
    const entry = event.data?.data();
    if (!entry) return;

    const apartmentId = event.params.apartmentId;

    try {
      // 1. Find all Admins for this apartment
      const adminsSnapshot = await db.collection("users")
        .where("apartmentId", "==", apartmentId)
        .where("role", "==", "ADMIN")
        .get();

      if (adminsSnapshot.empty) {
        console.log(`No admins found for apartment ${apartmentId}`);
        return;
      }

      // 2. Collect all FCM tokens from these admins
      const allTokens: string[] = [];
      adminsSnapshot.docs.forEach((doc) => {
        const userData = doc.data();
        const tokens = userData.fcmTokens as string[] | undefined;
        if (tokens && tokens.length > 0) {
          allTokens.push(...tokens);
        }
      });

      const uniqueTokens = [...new Set(allTokens)];
      if (uniqueTokens.length === 0) {
        console.log(`No FCM tokens found for admins of ${apartmentId}`);
        return;
      }

      // 3. Prepare the notification payload
      const vendorId = entry.vendorId;
      const vendorDoc = await db.collection("apartments")
        .doc(apartmentId)
        .collection("vendors")
        .doc(vendorId)
        .get();
      const vendorName = vendorDoc.exists ? vendorDoc.data()?.supplierName : "A Vendor";

      // Updated to match the actual Delivery domain model we created earlier
      const title = "🚚 New Tanker Arrived";
      const body = `${vendorName} just delivered ${entry.quantityLiters}L. ` +
                   `Driver: ${entry.driverName || "N/A"}.`;

      const message: admin.messaging.MulticastMessage = {
        tokens: uniqueTokens,
        notification: {
          title: title,
          body: body,
        },
        data: {
          apartmentId: apartmentId,
          deliveryId: event.params.deliveryId,
        },
      };

      // 4. Send the message
      const response = await admin.messaging().sendEachForMulticast(message);
      console.log(`Sent ${response.successCount} notifications for delivery in ${apartmentId}`);
    } catch (error) {
      console.error("Error in onDeliveryCreated:", error);
    }
  }
);

/**
 * Triggers when a new operator invite is created.
 * Using Firebase 'Trigger Email' Extension standard format (writing to 'mail' collection).
 */
export const onOperatorInvited = onDocumentCreated(
  {
    document: "apartments/{apartmentId}/operator_invites/{inviteId}",
    region: "us-central1",
  },
  async (event) => {
    const invite = event.data?.data();
    if (!invite) return;

    const email = invite.email;
    const apartmentId = event.params.apartmentId;

    try {
      // 1. Get Apartment Info to include in the email
      const apartmentDoc = await db.collection("apartments").doc(apartmentId).get();
      const apartmentName = apartmentDoc.exists ? apartmentDoc.data()?.name : "an Apartment";

      // 2. Write to the 'mail' collection so the Firebase Trigger Email Extension picks it up
      // Note: This requires installing "Trigger Email" extension in the Firebase Console
      await db.collection("mail").add({
        to: email,
        message: {
          subject: `You've been invited to manage ${apartmentName} on WaterTracker`,
          text: `Hello! You have been invited as an operator for ${apartmentName}. ` +
                `Download the WaterTracker app and sign in with this email to accept the invite.`,
          html: `
            <h2>Welcome to WaterTracker</h2>
            <p>You have been invited as an operator for <b>${apartmentName}</b>.</p>
            <p>Download the WaterTracker app from the Play Store and sign in with this email to accept the invite.</p>
            <br>
            <p><i>The WaterTracker Team</i></p>
          `,
        },
      });

      console.log(`Successfully queued invite email for ${email} at ${apartmentId}`);
    } catch (error) {
      console.error("Error in onOperatorInvited:", error);
    }
  }
);

/**
 * Triggers when a new bid is created.
 * Sends a push notification to all Admins of that apartment.
 */
export const onBidCreated = onDocumentCreated(
  {
    document: "bids/{bidId}",
    region: "us-central1",
  },
  async (event) => {
    const bid = event.data?.data();
    if (!bid) return;

    const requestId = bid.requestId;

    try {
      // 1. Get the request to find the apartmentId
      const requestDoc = await db.collection("tanker_requests").doc(requestId).get();
      if (!requestDoc.exists) return;

      const requestData = requestDoc.data();
      const apartmentId = requestData?.apartmentId;

      // 2. Find all Admins for this apartment
      const adminsSnapshot = await db.collection("users")
        .where("apartmentId", "==", apartmentId)
        .where("role", "==", "ADMIN")
        .get();

      if (adminsSnapshot.empty) return;

      // 3. Collect FCM tokens from these admins
      const allTokens: string[] = [];
      adminsSnapshot.docs.forEach((doc) => {
        const userData = doc.data();
        const tokens = userData.fcmTokens as string[] | undefined;
        if (tokens && tokens.length > 0) {
          allTokens.push(...tokens);
        }
      });

      const uniqueTokens = [...new Set(allTokens)];
      if (uniqueTokens.length === 0) return;

      // 4. Prepare the notification payload
      const message: admin.messaging.MulticastMessage = {
        tokens: uniqueTokens,
        notification: {
          title: "💰 New Bid Received!",
          body: `${bid.vendorName} has bid ₹${bid.price} for your request.`,
        },
        data: {
          requestId: requestId,
          bidId: event.params.bidId,
          type: "NEW_BID"
        },
      };

      // 5. Send the message
      const response = await admin.messaging().sendEachForMulticast(message);
      console.log(`Sent ${response.successCount} notifications for bid in request ${requestId}`);
    } catch (error) {
      console.error("Error in onBidCreated:", error);
    }
  }
);

