const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_parishioner_1";
const BOB_UID = "bob_parishioner_2";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function serverTimestamp() {
  return new Date(Date.now() - 1000);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validProfile(uid) {
  return {
    userId: uid,
    parishId: "AJN-1935-042",
    fullName: "Maria D'Souza",
    email: "maria@example.com",
    phone: "+91 9823012345",
    address: "Railway Colony, Ajni, Nagpur",
    sccUnit: "St. Jude Unit",
    headOfFamily: "Joseph D'Souza",
    dateOfBirth: "1992-05-14",
    baptismDate: "1992-06-21",
    spouseName: "",
    websiteSyncStatus: "FIRESTORE_ONLY",
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

function validRequest(uid, type = "BIRTH") {
  return {
    userId: uid,
    certificateType: type,
    applicantName: "Maria D'Souza",
    subjectName: "Leo D'Souza",
    eventDate: "2024-03-10",
    placeOrParish: "St. Anthony's Church, Ajni",
    parentsOrSpouseNames: "Joseph & Maria D'Souza",
    sponsorOrNextOfKin: "Peter Fernandes",
    registryReference: "AJN-1935-042",
    purpose: "School admission record",
    notes: "Please issue two copies.",
    status: "SUBMITTED",
    websiteSyncStatus: "FIRESTORE_ONLY",
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

test("Unauthenticated user: cannot read or write parishioner profiles or certificate requests", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("certificate_requests").get());
});

test("Owner: can create and update their own parishioner profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);
  await assertSucceeds(profileRef.set(validProfile(ALICE_UID)));
  await assertSucceeds(
    profileRef.update({
      phone: "+91 9999988888",
      sccUnit: "Holy Family Unit",
      updatedAt: serverTimestamp(),
    })
  );
});

test("Security: rejects ghost fields and cross-user profile access", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set({
      ...validProfile(BOB_UID),
      isAdmin: true,
    })
  );
});

test("Owner: can create all 5 certificate types, list own requests, and cancel while submitted", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const reqCol = aliceDb.collection("users").doc(ALICE_UID).collection("certificate_requests");

  for (const type of ["BIRTH", "COMMUNION", "CONFIRMATION", "MARRIAGE", "DEATH"]) {
    await assertSucceeds(reqCol.doc(`req_${type}`).set(validRequest(ALICE_UID, type)));
  }

  await assertSucceeds(reqCol.get());
  await assertSucceeds(
    reqCol.doc("req_BIRTH").update({
      status: "CANCELLED",
      updatedAt: serverTimestamp(),
    })
  );
  // Terminal state lock: cannot update after CANCELLED
  await assertFails(
    reqCol.doc("req_BIRTH").update({
      notes: "Re-opening",
      updatedAt: serverTimestamp(),
    })
  );
});

test("Security: rejects invalid certificate type, pre-approved status on create, and cross-user reads", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const aliceReqCol = aliceDb.collection("users").doc(ALICE_UID).collection("certificate_requests");

  await assertFails(aliceReqCol.doc("bad_type").set(validRequest(ALICE_UID, "INVALID")));
  await assertFails(
    aliceReqCol.doc("bad_status").set({
      ...validRequest(ALICE_UID, "MARRIAGE"),
      status: "READY",
    })
  );

  await assertSucceeds(aliceReqCol.doc("alice_req_1").set(validRequest(ALICE_UID, "COMMUNION")));
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("certificate_requests").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("certificate_requests").doc("alice_req_1").get());
});
