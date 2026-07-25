const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

const db = admin.firestore();

/**
 * Backend Cloud Function helper to verify if a caller is an active admin.
 */
async function verifyCallerIsActiveAdmin(context) {
  if (!context.auth || !context.auth.token || !context.auth.token.email) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "User must be authenticated with a valid email address."
    );
  }

  const userEmail = context.auth.token.email.trim().toLowerCase();
  const adminDocRef = db.collection("admins").doc(userEmail);
  const snapshot = await adminDocRef.get();

  if (!snapshot.exists || snapshot.data().status !== "active") {
    throw new functions.https.HttpsError(
      "permission-denied",
      "Access denied: Email does not exist in 'admins' collection or status is not active."
    );
  }

  return { email: userEmail, role: snapshot.data().role || "admin" };
}

/**
 * Cloud Function to ban or unban a user securely on the backend.
 */
exports.adminBanUser = functions.https.onCall(async (data, context) => {
  await verifyCallerIsActiveAdmin(context);

  const { targetUserId, isBlocked } = data;
  if (!targetUserId) {
    throw new functions.https.HttpsError("invalid-argument", "targetUserId is required.");
  }

  await db.collection("users").doc(targetUserId).update({
    isBlocked: Boolean(isBlocked),
    updatedAt: admin.firestore.FieldValue.serverTimestamp()
  });

  return { success: true, message: `User ${targetUserId} block status set to ${isBlocked}` };
});

/**
 * Cloud Function to broadcast push notification to all users via FCM.
 */
exports.adminBroadcastNotification = functions.https.onCall(async (data, context) => {
  await verifyCallerIsActiveAdmin(context);

  const { title, body, imageUrl } = data;
  if (!title || !body) {
    throw new functions.https.HttpsError("invalid-argument", "Title and Body are required.");
  }

  const payload = {
    notification: {
      title: title,
      body: body,
      imageUrl: imageUrl || ""
    },
    topic: "all_users"
  };

  await admin.messaging().send(payload);

  // Record notification log in Firestore
  await db.collection("notifications").add({
    title,
    body,
    imageUrl: imageUrl || null,
    targetUserId: null,
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
    sentByEmail: context.auth.token.email
  });

  return { success: true, message: "Broadcast push notification sent successfully." };
});

/**
 * Cloud Function to modify user diamond balances securely.
 */
exports.adminModifyUserDiamonds = functions.https.onCall(async (data, context) => {
  await verifyCallerIsActiveAdmin(context);

  const { targetUserId, deltaAmount } = data;
  if (!targetUserId || deltaAmount === undefined) {
    throw new functions.https.HttpsError("invalid-argument", "targetUserId and deltaAmount are required.");
  }

  const userRef = db.collection("users").doc(targetUserId);

  await db.runTransaction(async (transaction) => {
    const userDoc = await transaction.get(userRef);
    if (!userDoc.exists) {
      throw new functions.https.HttpsError("not-found", "Target user does not exist.");
    }

    const currentBalance = userDoc.data().diamondBalance || 0;
    const newBalance = Math.max(0, currentBalance + deltaAmount);

    transaction.update(userRef, { diamondBalance: newBalance });
  });

  return { success: true, message: `Successfully updated diamonds for user ${targetUserId}` };
});
