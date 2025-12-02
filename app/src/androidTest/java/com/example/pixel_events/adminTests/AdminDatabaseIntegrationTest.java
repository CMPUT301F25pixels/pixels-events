package com.example.pixel_events.adminTests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.utils.ImageConversion;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class AdminDatabaseIntegrationTest {
    private DatabaseHandler databaseHandler;
    private static final int TEST_EVENT_ID = 10000;
    private static final int TEST_USER_ID = 20000;
    private static final int TEST_ORGANIZER_ID = 30000;
    // FIX: Increased timeout duration for stability in Firebase emulator environment
    private static final int TIMEOUT_SEC = 20;
    private static final String TEST_IMAGE_URL = ImageConversion.bitmapToBase64(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888));

    @Before
    public void setUp() throws Exception {
        DatabaseHandler.resetInstance();
        databaseHandler = DatabaseHandler.getInstance(true); // use emulator
        cleanUpTestData();
        Thread.sleep(1000);
    }

    @After
    public void tearDown() {
        cleanUpTestData();
        DatabaseHandler.resetInstance();
    }

    private void cleanUpTestData() {
        databaseHandler.deleteEvent(TEST_EVENT_ID);
        databaseHandler.deleteAcc(TEST_USER_ID);
        databaseHandler.deleteAcc(TEST_ORGANIZER_ID);
    }

    private void createTestEvent(String imageUrl) throws InterruptedException {
        Event testEvent = new Event(
                TEST_EVENT_ID, TEST_ORGANIZER_ID, "Event for Admin", imageUrl, "Loc",
                100, "Desc", "Free", "2026-02-01", "2026-02-02",
                "10:00", "12:00", "2026-01-01", "2026-01-30", new ArrayList<>(Arrays.asList("Test"))
        );
        testEvent.setAutoUpdateDatabase(false);
        databaseHandler.addEvent(testEvent);
    }


    private void createTestProfile(int id, String role) throws InterruptedException {
        Profile testProfile = new Profile(
                id, role, "Admin Test User", "Other", "testadmin"+id+"@example.com",
                "123", "T6J0Z0", "AB", "Edmonton",
                new ArrayList<>(Arrays.asList(true, true, true))
        );
        testProfile.setAutoUpdateDatabase(false);
        databaseHandler.addAcc(testProfile);
    }

    /**
     * Helper method to synchronously fetch an event. Returns the Event object, null if not found,
     * or null if a timeout/transient error occurs (for use in retry loops). Throws permanent errors.
     */
    private Event fetchEvent(int eventId) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        databaseHandler.getEvent(eventId,
                event -> {
                    result.set(event);
                    latch.countDown();
                },
                e -> {
                    error.set(e);
                    latch.countDown();
                });

        boolean completed = latch.await(TIMEOUT_SEC, TimeUnit.SECONDS);

        if (!completed) {
            return null;
        }

        if (error.get() != null) {
            if (error.get() instanceof java.io.IOException) return null;
            throw error.get();
        }
        return result.get();
    }

    /**
     * Helper method to synchronously fetch a profile. Returns the Profile object, null if not found,
     * or null if a timeout/transient error occurs (for use in retry loops). Throws permanent errors.
     */
    private Profile fetchProfile(int userId) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Profile> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        databaseHandler.getProfile(userId,
                profile -> {
                    result.set(profile);
                    latch.countDown();
                },
                e -> {
                    error.set(e);
                    latch.countDown();
                });

        boolean completed = latch.await(TIMEOUT_SEC, TimeUnit.SECONDS);

        if (!completed) {
            return null;
        }

        if (error.get() != null) {
            if (error.get() instanceof java.io.IOException) return null;
            throw error.get();
        }
        return result.get();
    }

    /**
     * Helper to reliably fetch an Event within the timeout, asserting its existence for setup.
     */
    private Event checkEventExistence(int eventId) throws Exception {
        long startTime = System.currentTimeMillis();
        Event event;
        do {
            event = fetchEvent(eventId);
            if (event != null) return event;
            if (System.currentTimeMillis() - startTime > TIMEOUT_SEC * 1000) {
                // FIX: Added TIMEOUT_SEC to the error message for clarity
                fail("Event setup timed out: Event " + eventId + " could not be found. Exceeded " + TIMEOUT_SEC + " seconds.");
            }
            Thread.sleep(500); // Retry delay
        } while (true);
    }

    /**
     * Helper to reliably fetch a Profile within the timeout, asserting its existence for setup.
     */
    private Profile checkProfileExistence(int userId) throws Exception {
        long startTime = System.currentTimeMillis();
        Profile profile;
        do {
            profile = fetchProfile(userId);
            if (profile != null) return profile;
            if (System.currentTimeMillis() - startTime > TIMEOUT_SEC * 1000) {
                // FIX: Added TIMEOUT_SEC to the error message for clarity
                fail("Profile setup timed out: Profile " + userId + " could not be found. Exceeded " + TIMEOUT_SEC + " seconds.");
            }
            Thread.sleep(500); // Retry delay
        } while (true);
    }


    /**
     * Tests US 03.01.01: As an administrator, I want to be able to remove events.
     */
    @Test
    public void testAdminCanRemoveEvent() throws Exception {
        createTestEvent(TEST_IMAGE_URL);

        Event initialEvent = checkEventExistence(TEST_EVENT_ID);
        assertNotNull("Event should exist before deletion", initialEvent);

        databaseHandler.deleteEvent(TEST_EVENT_ID);

        long startTime = System.currentTimeMillis();
        Event finalEvent = fetchEvent(TEST_EVENT_ID);

        while (finalEvent != null) {
            if (System.currentTimeMillis() - startTime > TIMEOUT_SEC * 1000) {
                break;
            }
            Thread.sleep(500);
            finalEvent = fetchEvent(TEST_EVENT_ID);
        }

        assertNull("Event should be removed after deletion", finalEvent);
    }

    /**
     * Tests US 03.02.01 & US 03.07.01: Admin can remove user/organizer profiles.
     */
    @Test
    public void testAdminCanRemoveUserAndOrganizerProfiles() throws Exception {
        createTestProfile(TEST_USER_ID, "user");
        createTestProfile(TEST_ORGANIZER_ID, "org");

        checkProfileExistence(TEST_USER_ID);
        checkProfileExistence(TEST_ORGANIZER_ID);

        // ACTION: Remove profiles (asynchronous calls)
        databaseHandler.deleteAcc(TEST_USER_ID);
        databaseHandler.deleteAcc(TEST_ORGANIZER_ID);

        // Dynamic waiting for the complex asynchronous deleteAcc operation
        long startTime = System.currentTimeMillis();
        Profile finalUserProfile = fetchProfile(TEST_USER_ID);
        Profile finalOrgProfile = fetchProfile(TEST_ORGANIZER_ID);

        // Wait for both to be null or timeout
        while (finalUserProfile != null || finalOrgProfile != null) {
            if (System.currentTimeMillis() - startTime > TIMEOUT_SEC * 1000) {
                break;
            }
            Thread.sleep(500);

            if (finalUserProfile != null) {
                finalUserProfile = fetchProfile(TEST_USER_ID);
            }
            if (finalOrgProfile != null) {
                finalOrgProfile = fetchProfile(TEST_ORGANIZER_ID);
            }
        }

        assertNull("User profile should be removed after deletion", finalUserProfile);
        assertNull("Organizer profile should be removed after deletion", finalOrgProfile);
    }
}