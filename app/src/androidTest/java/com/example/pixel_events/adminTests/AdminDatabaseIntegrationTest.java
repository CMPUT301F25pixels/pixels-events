package com.example.pixel_events.adminTests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
    private static final int TIMEOUT_SEC = 10;
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
        Thread.sleep(5000);
    }


    private void createTestProfile(int id, String role) throws InterruptedException {
        Profile testProfile = new Profile(
                id, role, "Admin Test User", "Other", "testadmin"+id+"@example.com",
                "123", "T6J0Z0", "AB", "Edmonton",
                new ArrayList<>(Arrays.asList(true, true, true))
        );
        testProfile.setAutoUpdateDatabase(false);
        databaseHandler.addAcc(testProfile);
        Thread.sleep(2000);
    }

    /**
     * Helper method to synchronously fetch an event and return it (or null if not found/error).
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

        assertTrue("Event fetch timed out", latch.await(TIMEOUT_SEC, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw error.get();
        }
        return result.get();
    }

    /**
     * Tests US 03.01.01: As an administrator, I want to be able to remove events.
     */
    @Test
    public void testAdminCanRemoveEvent() throws Exception {
        createTestEvent(TEST_IMAGE_URL);

        Event initialEvent = fetchEvent(TEST_EVENT_ID);
        assertNotNull("Event should exist before deletion", initialEvent);

        databaseHandler.deleteEvent(TEST_EVENT_ID);
        Thread.sleep(3000);

        Event finalEvent = fetchEvent(TEST_EVENT_ID);

        assertNull("Event should be removed after deletion", finalEvent);
    }

    /**
     * Tests US 03.02.01 & US 03.07.01: Admin can remove user/organizer profiles.
     */
    @Test
    public void testAdminCanRemoveUserAndOrganizerProfiles() throws Exception {
        createTestProfile(TEST_USER_ID, "user");
        createTestProfile(TEST_ORGANIZER_ID, "org");


        CountDownLatch checkLatch = new CountDownLatch(1);
        AtomicReference<Profile> userProfile = new AtomicReference<>();
        databaseHandler.getProfile(TEST_USER_ID, p -> { userProfile.set(p); checkLatch.countDown(); }, e -> checkLatch.countDown());
        assertTrue("User Profile fetch timed out", checkLatch.await(TIMEOUT_SEC, TimeUnit.SECONDS));
        assertNotNull("User profile should exist before deletion", userProfile.get());

        // ACTION: Remove profiles
        databaseHandler.deleteAcc(TEST_USER_ID);
        databaseHandler.deleteAcc(TEST_ORGANIZER_ID);
        Thread.sleep(3000); // Add sleep after asynchronous delete

        // Verification: Profiles should be removed
        CountDownLatch verifyUserLatch = new CountDownLatch(1);
        CountDownLatch verifyOrgLatch = new CountDownLatch(1);
        userProfile.set(null); // Reset references

        databaseHandler.getProfile(TEST_USER_ID, p -> { userProfile.set(p); verifyUserLatch.countDown(); }, e -> verifyUserLatch.countDown());
        databaseHandler.getProfile(TEST_ORGANIZER_ID, p -> { /* Don't set orgProfile to null */ verifyOrgLatch.countDown(); }, e -> verifyOrgLatch.countDown());

        verifyUserLatch.await(3, TimeUnit.SECONDS);
        verifyOrgLatch.await(3, TimeUnit.SECONDS);

        assertNull("User profile should be removed after deletion", userProfile.get());
    }

}