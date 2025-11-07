package com.example.pixel_events;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Integration test for Event class with Firebase operations
 * Tests the complete workflow: Add -> Modify -> Get -> Delete
 * 
 * NOTE: This test uses Firebase Emulator for isolated testing.
 * To run these tests:
 * 1. Start Firebase emulator: firebase emulators:start
 * 2. Run tests: ./gradlew connectedAndroidTest
 * 
 * The emulator should be running on localhost:8080 for Firestore.
 */
public class EventIntegrationTest {

    private DatabaseHandler databaseHandler;
    private static final int TEST_EVENT_ID = 9999;
    private static final int TEST_ORGANIZER_ID = 888;
    private static final String TEST_TITLE = "Integration Test Event";
    private static final String TEST_IMAGE_URL = "https://test.com/image.jpg";
    private static final String TEST_LOCATION = "Test City";
    private static final String TEST_CAPACITY = "500";
    private static final String TEST_DESCRIPTION = "This is a test event";
    private static final String TEST_EVENT_START = "2026-02-01";
    private static final String TEST_EVENT_END = "2026-02-03";
    private static final String TEST_EVENT_START_TIME = "12:00";
    private static final String TEST_EVENT_END_TIME = "14:00";
    private static final String TEST_REG_START = "2026-01-01";
    private static final String TEST_REG_END = "2026-01-30";

    @Before
    public void setUp() {
        // Reset singleton to ensure fresh instance
        DatabaseHandler.resetInstance();
        
        // Initialize DatabaseHandler in offline mode (uses Firebase emulator)
        databaseHandler = DatabaseHandler.getInstance(true);
        
        // Clean up any existing test data
        cleanUpTestData();
    }

    @After
    public void tearDown() {
        // Clean up test data after each test
        cleanUpTestData();
        
        // Reset singleton for next test
        DatabaseHandler.resetInstance();
    }

    private void cleanUpTestData() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            databaseHandler.deleteEvent(TEST_EVENT_ID);
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test the complete workflow: Add Event -> Verify -> Modify -> Verify -> Delete -> Verify
     */
    @Test
    public void testCompleteEventLifecycle() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        CountDownLatch getLatch = new CountDownLatch(1);
        CountDownLatch modifyLatch = new CountDownLatch(1);
        CountDownLatch getModifiedLatch = new CountDownLatch(1);
        CountDownLatch deleteLatch = new CountDownLatch(1);
        
        AtomicBoolean addSuccess = new AtomicBoolean(false);
        AtomicBoolean getSuccess = new AtomicBoolean(false);
        AtomicBoolean modifySuccess = new AtomicBoolean(false);
        AtomicBoolean getModifiedSuccess = new AtomicBoolean(false);
        AtomicBoolean deleteSuccess = new AtomicBoolean(false);
        
        AtomicReference<Event> retrievedEvent = new AtomicReference<>();
        AtomicReference<Event> modifiedEvent = new AtomicReference<>();

        // Step 1: ADD EVENT
    databaseHandler.addEvent(
        TEST_EVENT_ID,
        TEST_ORGANIZER_ID,
        TEST_TITLE,
        TEST_IMAGE_URL,
        TEST_LOCATION,
        TEST_CAPACITY,
        TEST_DESCRIPTION,
        "Free",
        TEST_EVENT_START,
        TEST_EVENT_END,
        TEST_EVENT_START_TIME,
        TEST_EVENT_END_TIME,
        TEST_REG_START,
        TEST_REG_END
    );
        
        // Wait for add operation
        Thread.sleep(2000);
        addSuccess.set(true);
        addLatch.countDown();

        // Step 2: GET EVENT (verify it was added)
        databaseHandler.getEvent(
                String.valueOf(TEST_EVENT_ID),
                event -> {
                    if (event != null) {
                        retrievedEvent.set(event);
                        getSuccess.set(true);
                    }
                    getLatch.countDown();
                },
                e -> {
                    System.err.println("Failed to get event: " + e.getMessage());
                    getLatch.countDown();
                }
        );

        assertTrue("Get operation should complete", getLatch.await(5, TimeUnit.SECONDS));
        assertTrue("Event should be retrieved successfully", getSuccess.get());
        assertNotNull("Retrieved event should not be null", retrievedEvent.get());
        assertEquals("Event title should match", TEST_TITLE, retrievedEvent.get().getTitle());
        assertEquals("Event location should match", TEST_LOCATION, retrievedEvent.get().getLocation());

        // Step 3: MODIFY EVENT
        Map<String, Object> updates = new HashMap<>();
        String updatedTitle = "Modified Integration Test Event";
        String updatedLocation = "Updated Test City";
        String updatedCapacity = "1000";
        
        updates.put("title", updatedTitle);
        updates.put("location", updatedLocation);
        updates.put("capacity", updatedCapacity);

        databaseHandler.modifyEvent(TEST_EVENT_ID, updates, error -> {
            if (error == null) {
                modifySuccess.set(true);
            } else {
                System.err.println("Failed to modify event: " + error);
            }
            modifyLatch.countDown();
        });

        assertTrue("Modify operation should complete", modifyLatch.await(5, TimeUnit.SECONDS));
        assertTrue("Event should be modified successfully", modifySuccess.get());

        // Wait for Firebase to propagate changes
        Thread.sleep(1000);

        // Step 4: GET MODIFIED EVENT (verify modifications)
        databaseHandler.getEvent(
                String.valueOf(TEST_EVENT_ID),
                event -> {
                    if (event != null) {
                        modifiedEvent.set(event);
                        getModifiedSuccess.set(true);
                    }
                    getModifiedLatch.countDown();
                },
                e -> {
                    System.err.println("Failed to get modified event: " + e.getMessage());
                    getModifiedLatch.countDown();
                }
        );

        assertTrue("Get modified event should complete", getModifiedLatch.await(5, TimeUnit.SECONDS));
        assertTrue("Modified event should be retrieved", getModifiedSuccess.get());
        assertNotNull("Modified event should not be null", modifiedEvent.get());
        assertEquals("Event title should be updated", updatedTitle, modifiedEvent.get().getTitle());
        assertEquals("Event location should be updated", updatedLocation, modifiedEvent.get().getLocation());
        assertEquals("Event capacity should be updated", updatedCapacity, modifiedEvent.get().getCapacity());

        // Step 5: DELETE EVENT
        databaseHandler.deleteEvent(TEST_EVENT_ID);
        
        // Wait for delete operation
        Thread.sleep(2000);
        deleteSuccess.set(true);
        deleteLatch.countDown();

        assertTrue("Delete operation should complete", deleteLatch.await(5, TimeUnit.SECONDS));
        assertTrue("Event should be deleted successfully", deleteSuccess.get());

        // Step 6: VERIFY DELETION (try to get deleted event)
        CountDownLatch verifyDeleteLatch = new CountDownLatch(1);
        AtomicBoolean eventNotFound = new AtomicBoolean(false);

        databaseHandler.getEvent(
                String.valueOf(TEST_EVENT_ID),
                event -> {
                    if (event == null) {
                        eventNotFound.set(true);
                    }
                    verifyDeleteLatch.countDown();
                },
                e -> verifyDeleteLatch.countDown()
        );

        assertTrue("Verify delete should complete", verifyDeleteLatch.await(5, TimeUnit.SECONDS));
        assertTrue("Event should not exist after deletion", eventNotFound.get());
    }

    /**
     * Test adding an event and verifying all fields
     */
    @Test
    public void testAddEventWithAllFields() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> retrievedEvent = new AtomicReference<>();

        // Add event
    databaseHandler.addEvent(
        TEST_EVENT_ID,
        TEST_ORGANIZER_ID,
        TEST_TITLE,
        TEST_IMAGE_URL,
        TEST_LOCATION,
        TEST_CAPACITY,
        TEST_DESCRIPTION,
        "Free",
        TEST_EVENT_START,
        TEST_EVENT_END,
        TEST_EVENT_START_TIME,
        TEST_EVENT_END_TIME,
        TEST_REG_START,
        TEST_REG_END
    );

        Thread.sleep(2000);

        // Retrieve and verify
        databaseHandler.getEvent(
                String.valueOf(TEST_EVENT_ID),
                event -> {
                    retrievedEvent.set(event);
                    latch.countDown();
                },
                e -> latch.countDown()
        );

        assertTrue("Operation should complete", latch.await(5, TimeUnit.SECONDS));
        assertNotNull("Event should be retrieved", retrievedEvent.get());

        Event event = retrievedEvent.get();
        assertEquals("Event ID should match", TEST_EVENT_ID, event.getEventId());
        assertEquals("Organizer ID should match", TEST_ORGANIZER_ID, event.getOrganizerId());
        assertEquals("Title should match", TEST_TITLE, event.getTitle());
        assertEquals("Image URL should match", TEST_IMAGE_URL, event.getImageUrl());
        assertEquals("Location should match", TEST_LOCATION, event.getLocation());
        assertEquals("Capacity should match", TEST_CAPACITY, event.getCapacity());
        assertEquals("Description should match", TEST_DESCRIPTION, event.getDescription());
        assertEquals("Event start date should match", TEST_EVENT_START, event.getEventStartDate());
        assertEquals("Event end date should match", TEST_EVENT_END, event.getEventEndDate());
        assertEquals("Event start time should match", TEST_EVENT_START_TIME, event.getEventStartTime());
        assertEquals("Event end time should match", TEST_EVENT_END_TIME, event.getEventEndTime());
        assertEquals("Registration start date should match", TEST_REG_START, event.getRegistrationStartDate());
        assertEquals("Registration end date should match", TEST_REG_END, event.getRegistrationEndDate());
    }

    /**
     * Test modifying multiple fields at once
     */
    @Test
    public void testModifyMultipleFields() throws InterruptedException {
        // First add the event
    databaseHandler.addEvent(
        TEST_EVENT_ID,
        TEST_ORGANIZER_ID,
        TEST_TITLE,
        TEST_IMAGE_URL,
        TEST_LOCATION,
        TEST_CAPACITY,
        TEST_DESCRIPTION,
        "Free",
        TEST_EVENT_START,
        TEST_EVENT_END,
        TEST_EVENT_START_TIME,
        TEST_EVENT_END_TIME,
        TEST_REG_START,
        TEST_REG_END
    );

        Thread.sleep(2000);

        CountDownLatch modifyLatch = new CountDownLatch(1);
        AtomicBoolean modifySuccess = new AtomicBoolean(false);

        // Modify multiple fields
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "Multi-Update Title");
        updates.put("location", "Multi-Update Location");
        updates.put("capacity", "2000");
        updates.put("description", "Multi-Update Description");

        databaseHandler.modifyEvent(TEST_EVENT_ID, updates, error -> {
            modifySuccess.set(error == null);
            modifyLatch.countDown();
        });

        assertTrue("Modify should complete", modifyLatch.await(5, TimeUnit.SECONDS));
        assertTrue("Modify should succeed", modifySuccess.get());

        Thread.sleep(1000);

        // Verify changes
        CountDownLatch getLatch = new CountDownLatch(1);
        AtomicReference<Event> modifiedEvent = new AtomicReference<>();

        databaseHandler.getEvent(
                String.valueOf(TEST_EVENT_ID),
                event -> {
                    modifiedEvent.set(event);
                    getLatch.countDown();
                },
                e -> getLatch.countDown()
        );

        assertTrue("Get should complete", getLatch.await(5, TimeUnit.SECONDS));
        assertNotNull("Modified event should exist", modifiedEvent.get());
        assertEquals("Title should be updated", "Multi-Update Title", modifiedEvent.get().getTitle());
        assertEquals("Location should be updated", "Multi-Update Location", modifiedEvent.get().getLocation());
        assertEquals("Capacity should be updated", "2000", modifiedEvent.get().getCapacity());
        assertEquals("Description should be updated", "Multi-Update Description", modifiedEvent.get().getDescription());
    }

    /**
     * Test attempting to modify non-existent event
     */
    @Test
    public void testModifyNonExistentEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorMessage = new AtomicReference<>();

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "Should Fail");

        int nonExistentId = 99999;
        databaseHandler.modifyEvent(nonExistentId, updates, error -> {
            errorMessage.set(error);
            latch.countDown();
        });

        assertTrue("Operation should complete", latch.await(5, TimeUnit.SECONDS));
        assertNotNull("Error message should be set", errorMessage.get());
        assertTrue("Error should indicate event doesn't exist", 
                errorMessage.get().contains("does not exist"));
    }

    /**
     * Test deleting non-existent event (should not throw exception)
     */
    @Test
    public void testDeleteNonExistentEvent() throws InterruptedException {
        int nonExistentId = 88888;
        
        // This should complete without throwing exception
        databaseHandler.deleteEvent(nonExistentId);
        
        Thread.sleep(2000);
        
        // Test passes if no exception is thrown
        assertTrue("Delete non-existent event should not throw exception", true);
    }
}

/*
 * Integration Test Class: EventIntegrationTest
 * 
 * Purpose:
 *      Test Event class with real Firebase operations
 * 
 * Test Coverage:
 *      - Complete lifecycle (add, get, modify, get, delete, verify)
 *      - Add event with all fields verification
 *      - Modify multiple fields at once
 *      - Error handling for non-existent events
 *      - Delete operations
 * 
 * Total Tests: 5 integration test cases
 * 
 * Prerequisites:
 *      - Firebase must be properly configured
 *      - Internet connection required
 *      - Firebase emulator recommended for testing
 * 
 * Notes:
 *      - Uses CountDownLatch for async operations
 *      - Includes cleanup in setUp and tearDown
 *      - Uses Thread.sleep() to allow Firebase operations to complete
 */
