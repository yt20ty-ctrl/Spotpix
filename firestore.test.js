const { describe, it, before, after, beforeEach } = require('node:test');
const fs = require('node:fs');
const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
} = require('@firebase/rules-unit-testing');

const PROJECT_ID = 'demo-spotpix-rules-test';
const DATABASE_ID = 'ai-studio-android-instalin-a87549ec-8095-4b64-9f38-93c48ec27670';

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      host: '127.0.0.1',
      port: 8085,
      rules: fs.readFileSync('firestore.rules', 'utf8'),
    },
  });
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

after(async () => {
  await testEnv.cleanup();
});

function nowDate() {
  return new Date(Date.now() - 1000);
}

function validUserPayload(userId) {
  return {
    userId,
    username: 'alex_veta',
    displayName: 'Alex Rivera',
    email: 'alex@spotpix.veta.io',
    bio: 'Creator on Spotpix by VETA',
    avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200',
    role: 'USER',
    blueTickVerified: false,
    isMonetized: false,
    createdAt: nowDate(),
  };
}

function validPostPayload(userId) {
  return {
    userId,
    authorUsername: 'alex_veta',
    caption: 'Liquid glass aesthetics #Spotpix #VETA',
    mediaUrl: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800',
    mediaType: 'POST',
    likesCount: 0,
    createdAt: nowDate(),
  };
}

describe('Spotpix Firestore Security Rules', () => {
  it('denies unauthenticated access to users and posts', async () => {
    const anonDb = testEnv.unauthenticatedContext().firestore(DATABASE_ID);
    await assertFails(anonDb.doc('users/user_1').get());
    await assertFails(anonDb.doc('users/user_1').set(validUserPayload('user_1')));
    await assertFails(anonDb.doc('posts/post_1').set(validPostPayload('user_1')));
  });

  it('allows owner to create and read their own valid UserProfile', async () => {
    const aliceDb = testEnv.authenticatedContext('alice_1').firestore(DATABASE_ID);
    await assertSucceeds(aliceDb.doc('users/alice_1').set(validUserPayload('alice_1')));
    await assertSucceeds(aliceDb.doc('users/alice_1').get());
  });

  it('denies non-admin self-escalation of role, blueTickVerified, or isMonetized on create or update', async () => {
    const aliceDb = testEnv.authenticatedContext('alice_1').firestore(DATABASE_ID);
    await assertFails(
      aliceDb.doc('users/alice_1').set({
        ...validUserPayload('alice_1'),
        blueTickVerified: true,
      })
    );
    await assertFails(
      aliceDb.doc('users/alice_1').set({
        ...validUserPayload('alice_1'),
        role: 'SUPER_ADMIN',
      })
    );

    await assertSucceeds(aliceDb.doc('users/alice_1').set(validUserPayload('alice_1')));
    await assertFails(
      aliceDb.doc('users/alice_1').update({
        blueTickVerified: true,
        updatedAt: nowDate(),
      })
    );
  });

  it('allows Super Admin VaanijSingh28@gmail.com to approve Blue Tick and Monetization', async () => {
    const aliceDb = testEnv.authenticatedContext('alice_1').firestore(DATABASE_ID);
    await assertSucceeds(aliceDb.doc('users/alice_1').set(validUserPayload('alice_1')));

    const adminDb = testEnv
      .authenticatedContext('admin_vaanij', {
        email: 'VaanijSingh28@gmail.com',
        email_verified: true,
      })
      .firestore(DATABASE_ID);

    await assertSucceeds(
      adminDb.doc('users/alice_1').update({
        blueTickVerified: true,
        isMonetized: true,
        updatedAt: nowDate(),
      })
    );
  });

  it('enforces strict SpotpixPost ownership and schema validation', async () => {
    const aliceDb = testEnv.authenticatedContext('alice_1').firestore(DATABASE_ID);
    const bobDb = testEnv.authenticatedContext('bob_2').firestore(DATABASE_ID);

    // Create valid post
    await assertSucceeds(aliceDb.doc('posts/post_1').set(validPostPayload('alice_1')));

    // Deny extra unallowed field
    await assertFails(
      aliceDb.doc('posts/post_bad_extra').set({
        ...validPostPayload('alice_1'),
        hackedFlag: true,
      })
    );

    // Deny future createdAt timestamp
    const futureDate = new Date(Date.now() + 86400 * 1000);
    await assertFails(
      aliceDb.doc('posts/post_future').set({
        ...validPostPayload('alice_1'),
        createdAt: futureDate,
      })
    );

    // Deny cross-user read, update, delete
    await assertFails(bobDb.doc('posts/post_1').get());
    await assertFails(
      bobDb.doc('posts/post_1').update({
        caption: 'Tampered by Bob',
        updatedAt: nowDate(),
      })
    );
    await assertFails(bobDb.doc('posts/post_1').delete());

    // Allow owner update of caption and likesCount
    await assertSucceeds(
      aliceDb.doc('posts/post_1').update({
        caption: 'Updated caption by Alice',
        likesCount: 42,
        updatedAt: nowDate(),
      })
    );

    // Deny owner mutating immutable userId
    await assertFails(
      aliceDb.doc('posts/post_1').update({
        userId: 'bob_2',
        updatedAt: nowDate(),
      })
    );

    // Allow owner delete
    await assertSucceeds(aliceDb.doc('posts/post_1').delete());
  });
});
