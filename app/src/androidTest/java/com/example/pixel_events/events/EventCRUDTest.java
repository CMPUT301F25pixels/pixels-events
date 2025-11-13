package com.example.pixel_events.events;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.database.DatabaseHandler;
import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.*;

/**
 * Instrumented test to verify Event CRUD operations with Firebase
 */
@RunWith(AndroidJUnit4.class)
public class EventCRUDTest {
    private static final String TAG = "EventCRUDTest";
    private DatabaseHandler db;

    @Before
    public void setup() {
        try {
            FirebaseApp.initializeApp(getApplicationContext());
        } catch (IllegalStateException e) {
            // Already initialized
        }
        db = DatabaseHandler.getInstance();
    }

    @Test
    public void testCreateAndRetrieveEvent() throws InterruptedException {
        Log.d(TAG, "Starting testCreateAndRetrieveEvent");

        // Create test event data
        int testEventId = (int) (System.currentTimeMillis() / 1000L);
        int organizerId = 999;
        String title = "Test Event " + testEventId;
        String imageUrl = "";
        String location = "Test Location";
        String capacity = "100";
        String description = "Test Description";
        String fee = "Free";
        String eventStartDate = "2026-01-08";
        String eventEndDate = "2026-01-10";
        String eventStartTime = "10:00";
        String eventEndTime = "12:00";
        String registrationStartDate = "2026-01-01";
        String registrationEndDate = "2026-01-06";
        ArrayList<String> tags = new ArrayList<>();
        tags.add("Test");

        // Step 1: Create and save event
        Log.d(TAG, "Creating event with ID: " + testEventId);
        Event testEvent = new Event(testEventId, organizerId, title, imageUrl, location,
                capacity, description, fee, eventStartDate, eventEndDate, eventStartTime,
                eventEndTime, registrationStartDate, registrationEndDate, tags);

        testEvent.saveToDatabase();
        Log.d(TAG, "Event saved to database");

        // Wait for Firebase write to complete
        Thread.sleep(2000);

        // Step 2: Retrieve the event
        CountDownLatch latch = new CountDownLatch(1);
        final Event[] retrievedEvent = {null};
        final Exception[] error = {null};

        db.getEvent(testEventId,
                event -> {
                    retrievedEvent[0] = event;
                    latch.countDown();
                },
                e -> {
                    error[0] = e;
                    latch.countDown();
                });

        // Wait for retrieval (max 10 seconds)
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Step 3: Verify retrieval
        assertTrue("Event retrieval timed out", completed);
        assertNull("Error occurred during retrieval: " + (error[0] != null ? error[0].getMessage() : ""), error[0]);
        assertNotNull("Retrieved event is null", retrievedEvent[0]);

        // Step 4: Verify all fields
        Event event = retrievedEvent[0];
        assertEquals("Event ID mismatch", testEventId, event.getEventId());
        assertEquals("Title mismatch", title, event.getTitle());
        assertEquals("Location mismatch", location, event.getLocation());
        assertEquals("Capacity mismatch", capacity, event.getCapacity());
        assertEquals("Description mismatch", description, event.getDescription());
        assertEquals("Fee mismatch", fee, event.getFee());
        assertEquals("Start date mismatch", eventStartDate, event.getEventStartDate());
        assertEquals("End date mismatch", eventEndDate, event.getEventEndDate());
        assertEquals("Start time mismatch", eventStartTime, event.getEventStartTime());
        assertEquals("End time mismatch", eventEndTime, event.getEventEndTime());

        Log.d(TAG, "✅ Test passed! Event created and retrieved successfully");
    }

    @Test
    public void testRetrieveAllEvents() throws InterruptedException {
        Log.d(TAG, "Starting testRetrieveAllEvents");

        CountDownLatch latch = new CountDownLatch(1);
        final int[] eventCount = {0};
        final Exception[] error = {null};

        db.getAllEvents(
                events -> {
                    eventCount[0] = events.size();
                    Log.d(TAG, "Retrieved " + eventCount[0] + " events from database");
                    for (Event event : events) {
                        Log.d(TAG, "Event: " + event.getTitle() + " (ID: " + event.getEventId() + ")");
                    }
                    latch.countDown();
                },
                e -> {
                    error[0] = e;
                    latch.countDown();
                });

        boolean completed = latch.await(10, TimeUnit.SECONDS);

        assertTrue("Event retrieval timed out", completed);
        assertNull("Error occurred: " + (error[0] != null ? error[0].getMessage() : ""), error[0]);
        assertTrue("No events found in database", eventCount[0] > 0);

        Log.d(TAG, "✅ Test passed! Retrieved " + eventCount[0] + " events");
    }
}

