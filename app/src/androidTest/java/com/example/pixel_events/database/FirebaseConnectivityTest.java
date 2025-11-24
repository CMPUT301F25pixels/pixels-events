package com.example.pixel_events.database;

import static org.junit.Assert.*;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.events.Event;
import com.example.pixel_events.profile.Profile;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class FirebaseConnectivityTest {
    private static final String TAG = "FirebaseConnectivityTest";
    private DatabaseHandler db;
    private FirebaseFirestore firestore;

    @Before
    public void setUp() {
        db = DatabaseHandler.getInstance();
        firestore = FirebaseFirestore.getInstance();
        assertNotNull("DatabaseHandler should not be null", db);
        assertNotNull("FirebaseFirestore should not be null", firestore);
    }

    @Test
    public void testFirebaseInitialized() {
        FirebaseApp app = FirebaseApp.getInstance();
        assertNotNull("Firebase should be initialized", app);
        Log.d(TAG, "Firebase initialized successfully");
    }

    @Test
    public void testFirestoreConnection() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);

        // Try to read from Firestore
        firestore.collection("AccountData")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    success.set(true);
                    Log.d(TAG, "Successfully connected to Firestore");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to connect to Firestore", e);
                    latch.countDown();
                });

        assertTrue("Firestore query should complete within 10 seconds",
                latch.await(10, TimeUnit.SECONDS));
        assertTrue("Should be able to connect to Firestore", success.get());
    }

    @Test
    public void testCreateAndRetrieveEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);

        int testEventId = (int) (System.currentTimeMillis() / 1000L);
        String testTitle = "Test Event " + testEventId;

        ArrayList<String> tags = new ArrayList<>();
        tags.add("Test");

        Log.d(TAG, "Creating test event with ID: " + testEventId);
        Event testEvent = new Event(testEventId, 1, testTitle, "", "Test Location",
                "100", "Test Description", "Free", "2026-01-08", "2026-01-10",
                "10:00", "18:00", "2026-01-01", "2026-01-06", tags);

        // Create event
        db.addEvent(testEvent);

        // Wait a bit for Firestore to process
        Thread.sleep(2000);

        // Try to retrieve it
        db.getEvent(testEventId,
                event -> {
                    if (event != null) {
                        assertEquals("Event title should match", testTitle, event.getTitle());
                        success.set(true);
                        Log.d(TAG, "Successfully retrieved test event: " + testTitle);
                    } else {
                        Log.e(TAG, "Event was null after retrieval");
                    }
                    latch.countDown();
                },
                e -> {
                    Log.e(TAG, "Error retrieving event", e);
                    latch.countDown();
                });

        assertTrue("Event retrieval should complete within 10 seconds",
                latch.await(10, TimeUnit.SECONDS));
        assertTrue("Should be able to create and retrieve event", success.get());

        // Cleanup
        db.deleteEvent(testEventId);
    }

    @Test
    public void testEventListRetrieval() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Integer> eventCount = new AtomicReference<>(0);

        Log.d(TAG, "Testing event list retrieval from EventData collection");

        firestore.collection("EventData")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventCount.set(queryDocumentSnapshots.size());
                    Log.d(TAG, "Found " + eventCount.get() + " events in database");

                    // Log first event details if any exist
                    if (eventCount.get() > 0) {
                        Map<String, Object> firstEvent = queryDocumentSnapshots.getDocuments().get(0).getData();
                        Log.d(TAG, "First event data: " + firstEvent);
                    }

                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to retrieve events", e);
                    latch.countDown();
                });

        assertTrue("Event list query should complete within 10 seconds",
                latch.await(10, TimeUnit.SECONDS));
        Log.d(TAG, "Event list retrieval test completed. Events found: " + eventCount.get());
    }

    @Test
    public void testAccountCreationAndRetrieval() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);

        int testUserId = (int) (System.currentTimeMillis() / 1000L);
        String testUserName = "Test User " + testUserId;

        Log.d(TAG, "Creating test account with ID: " + testUserId);

        List<Boolean> notifyPrefs = new ArrayList<>(
                List.of(true, true, true)
        );

        Profile testProfile = new Profile(
                testUserId, "user", testUserName, "Other", "test@example.com",
                "1234567890","", "", "",
                notifyPrefs
        );

        // Create account
        db.addAcc(testProfile);

        // Wait for Firestore to process
        Thread.sleep(2000);

        // Try to retrieve it
        db.getProfile(testUserId,
                profile -> {
                    if (profile != null) {
                        assertEquals("Username should match", testUserName, profile.getUserName());
                        success.set(true);
                        Log.d(TAG, "Successfully retrieved test account: " + testUserName);
                    } else {
                        Log.e(TAG, "Profile was null after retrieval");
                    }
                    latch.countDown();
                },
                e -> {
                    Log.e(TAG, "Error retrieving account", e);
                    latch.countDown();
                });

        assertTrue("Account retrieval should complete within 10 seconds",
                latch.await(10, TimeUnit.SECONDS));
        assertTrue("Should be able to create and retrieve account", success.get());

        // Cleanup
        db.deleteAcc(testUserId);
    }
}

