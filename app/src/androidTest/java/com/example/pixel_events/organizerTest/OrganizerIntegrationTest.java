package com.example.pixel_events.organizerTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.utils.ImageConversion;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class OrganizerIntegrationTest {
    private DatabaseHandler databaseHandler;
    private Event testEvent;
    private static final int TEST_EVENT_ID = 9999;
    private static final int TEST_ORGANIZER_ID = 888;
    private static final String TEST_TITLE = "Integration Test Event";
    private String TEST_IMAGE_URL;
    private static final String TEST_LOCATION = "Test City";
    private static final int TEST_CAPACITY = 500;
    private static final String TEST_FEE = "Free";
    private static final String TEST_DESCRIPTION = "This is a test event";
    private static final String TEST_EVENT_START = "2026-02-01";
    private static final String TEST_EVENT_END = "2026-02-03";
    private static final String TEST_EVENT_START_TIME = "12:00";
    private static final String TEST_EVENT_END_TIME = "14:00";
    private static final String TEST_REG_START = "2026-01-01";
    private static final String TEST_REG_END = "2026-01-30";
    private static final ArrayList<String> TEST_TAGS = new ArrayList<String>(Arrays.asList("Adventure", "Cultural"));
    private Bitmap TEST_BITMAP;

    @Before
    public void setUp() {
        DatabaseHandler.resetInstance();

        // Initialize DatabaseHandler in offline mode (uses Firebase emulator)
        databaseHandler = DatabaseHandler.getInstance(true);

        // Create a bitmap to upload for testing
        TEST_BITMAP = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

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

    @Test
    public void testBitmapToBaseURL() {
        String base64 = ImageConversion.bitmapToBase64(TEST_BITMAP);
        assertNotNull("Base64 string should not be null", base64);
        assertTrue("Base64 string should not be empty", !base64.isEmpty());
    }

    @Test
    public void testBase64ToBitmap() {
        String base64 = ImageConversion.bitmapToBase64(TEST_BITMAP);
        Bitmap test_bitmap = ImageConversion.base64ToBitmap(base64);
        assertNotNull("Bitmap should not be null", test_bitmap);
        assertEquals("Bitmap dimensions should match", TEST_BITMAP.getWidth(), test_bitmap.getWidth());
        assertEquals("Bitmap dimensions should match", TEST_BITMAP.getHeight(), test_bitmap.getHeight());
    }

    /**
     * This test both US 02.04.01 and US 02.04.02
     * @throws InterruptedException
     */
    @Test
    public void testImageUploadAndModify() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        CountDownLatch getLatch = new CountDownLatch(1);
        CountDownLatch modifyLatch = new CountDownLatch(1);
        CountDownLatch getModifiedLatch = new CountDownLatch(1);

        AtomicBoolean addSuccess = new AtomicBoolean(false);
        AtomicBoolean getSuccess = new AtomicBoolean(false);
        AtomicBoolean modifySuccess = new AtomicBoolean(false);
        AtomicBoolean getModifiedSuccess = new AtomicBoolean(false);

        AtomicReference<Event> retrievedEvent = new AtomicReference<>();
        AtomicReference<Event> modifiedEvent = new AtomicReference<>();

        TEST_IMAGE_URL = ImageConversion.bitmapToBase64(TEST_BITMAP);
        Event testEvent = new Event(
                TEST_EVENT_ID,
                TEST_ORGANIZER_ID,
                TEST_TITLE,
                TEST_IMAGE_URL,
                TEST_LOCATION,
                TEST_CAPACITY,
                TEST_DESCRIPTION,
                TEST_FEE,
                TEST_EVENT_START,
                TEST_EVENT_END,
                TEST_EVENT_START_TIME,
                TEST_EVENT_END_TIME,
                TEST_REG_START,
                TEST_REG_END,
                TEST_TAGS,
                Boolean.FALSE
        );

        databaseHandler.addEvent(testEvent);

        // Wait for add operation
        Thread.sleep(2000);
        addSuccess.set(true);
        addLatch.countDown();

        databaseHandler.getEvent(
                TEST_EVENT_ID,
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
        assertEquals("Event title should match", TEST_IMAGE_URL, retrievedEvent.get().getImageUrl());

        Map<String, Object> updates = new HashMap<>();
        TEST_BITMAP = Bitmap.createBitmap(150, 100, Bitmap.Config.ARGB_8888);
        String updatedImageUrl = ImageConversion.bitmapToBase64(TEST_BITMAP);

        updates.put("imageUrl", updatedImageUrl);
        databaseHandler.modify(databaseHandler.getEventCollection(), TEST_EVENT_ID, updates, error -> {
            if (error == null) {
                modifySuccess.set(true);
            } else {
                System.err.println("Failed to modify event: " + error);
            }
            modifyLatch.countDown();
        });

        assertTrue("Modify operation should complete", modifyLatch.await(5, TimeUnit.SECONDS));
        assertTrue("Event should be modified successfully", modifySuccess.get());

        Thread.sleep(1000);

        // Step 4: GET MODIFIED EVENT (verify modifications)
        databaseHandler.getEvent(
                TEST_EVENT_ID,
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
        assertEquals("Event image URL should be updated", updatedImageUrl, modifiedEvent.get().getImageUrl());
    }
}
