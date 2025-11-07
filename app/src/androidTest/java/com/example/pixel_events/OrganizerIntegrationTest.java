package com.example.pixel_events;

import com.example.pixel_events.database.DatabaseHandler;

import org.junit.After;
import org.junit.Before;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class OrganizerIntegrationTest {
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
}
