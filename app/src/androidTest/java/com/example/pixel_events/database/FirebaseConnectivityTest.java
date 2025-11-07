package com.example.pixel_events.database;

import static org.junit.Assert.*;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.events.Event;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
        
        // Create event
        db.addEvent(testEventId, 1, testTitle, "", "Test Location",
                "100", "Test Description", "Free", "2025-12-01", "2025-12-02",
                "10:00", "18:00", "2025-11-01", "2025-11-30", tags);
        
        // Wait a bit for Firestore to process
        Thread.sleep(2000);
        
        // Try to retrieve it
        db.getEvent(String.valueOf(testEventId), 
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
        
        List<Boolean> notifyPrefs = new ArrayList<>();
        notifyPrefs.add(true);
        notifyPrefs.add(true);
        notifyPrefs.add(true);
        
        // Create account
        db.addAcc(testUserId, "user", testUserName, new Date(), "Other",
                "test@example.com", "Test City", "Test Province", 1234567890, notifyPrefs);
        
        // Wait for Firestore to process
        Thread.sleep(2000);
        
        // Try to retrieve it
        db.getAcc(String.valueOf(testUserId),
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

