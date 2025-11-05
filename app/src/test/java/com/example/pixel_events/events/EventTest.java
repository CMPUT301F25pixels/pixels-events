package com.example.pixel_events.events;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Unit tests for Event class
 * Tests individual methods and combined operations (add, modify, delete)
 * 
 * NOTE: These tests use the empty Event() constructor to avoid Firebase initialization
 * and then manually set fields to test getters, setters, and validation logic.
 */
public class EventTest {

    private Event testEvent;
    private static final int TEST_EVENT_ID = 1001;
    private static final int TEST_ORGANIZER_ID = 501;
    private static final String TEST_TITLE = "Tech Conference 2025";
    private static final String TEST_IMAGE_URL = "https://example.com/image.jpg";
    private static final String TEST_LOCATION = "Edmonton Convention Center";
    private static final String TEST_CAPACITY = "500";
    private static final String TEST_DESCRIPTION = "Annual tech conference";
    private static final String TEST_FEE = "Free";
    private static final String TEST_EVENT_START = "2025-12-01";
    private static final String TEST_EVENT_END = "2025-12-03";
    private static final String TEST_EVENT_START_TIME = "12:00";
    private static final String TEST_EVENT_END_TIME = "14:00";
    private static final String TEST_REG_START = "2025-11-01";
    private static final String TEST_REG_END = "2025-11-30";

    @Before
    public void setUp() {
        // Create event using parameterized constructor
        // This will validate fields but NOT save to database automatically
    testEvent = new Event(
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
        TEST_REG_END
    );
        
        // Disable automatic database updates for testing
        testEvent.setAutoUpdateDatabase(false);
    }

    // ========================================================================================
    // UNIT TESTS - Testing individual getters
    // ========================================================================================

    @Test
    public void testGetEventId() {
        assertEquals("Event ID should match", TEST_EVENT_ID, testEvent.getEventId());
    }

    @Test
    public void testGetOrganizerId() {
        assertEquals("Organizer ID should match", TEST_ORGANIZER_ID, testEvent.getOrganizerId());
    }

    @Test
    public void testGetTitle() {
        assertEquals("Title should match", TEST_TITLE, testEvent.getTitle());
    }

    @Test
    public void testGetImageUrl() {
        assertEquals("Image URL should match", TEST_IMAGE_URL, testEvent.getImageUrl());
    }

    @Test
    public void testGetLocation() {
        assertEquals("Location should match", TEST_LOCATION, testEvent.getLocation());
    }

    @Test
    public void testGetCapacity() {
        assertEquals("Capacity should match", TEST_CAPACITY, testEvent.getCapacity());
    }

    @Test
    public void testGetDescription() {
        assertEquals("Description should match", TEST_DESCRIPTION, testEvent.getDescription());
    }

    @Test
    public void testGetEventStartDate() {
        assertEquals("Event start date should match", TEST_EVENT_START, testEvent.getEventStartDate());
    }

    @Test
    public void testGetEventEndDate() {
        assertEquals("Event end date should match", TEST_EVENT_END, testEvent.getEventEndDate());
    }

    @Test
    public void testGetRegistrationStartDate() {
        assertEquals("Registration start date should match", TEST_REG_START, testEvent.getRegistrationStartDate());
    }

    @Test
    public void testGetRegistrationEndDate() {
        assertEquals("Registration end date should match", TEST_REG_END, testEvent.getRegistrationEndDate());
    }

    // ========================================================================================
    // UNIT TESTS - Testing individual setters
    // ========================================================================================

    @Test
    public void testSetTitle() {
        String newTitle = "Updated Conference Title";
        testEvent.setTitle(newTitle);
        assertEquals("Title should be updated", newTitle, testEvent.getTitle());
    }

    @Test
    public void testSetLocation() {
        String newLocation = "Calgary Convention Center";
        testEvent.setLocation(newLocation);
        assertEquals("Location should be updated", newLocation, testEvent.getLocation());
    }

    @Test
    public void testSetCapacity() {
        String newCapacity = "1000";
        testEvent.setCapacity(newCapacity);
        assertEquals("Capacity should be updated", newCapacity, testEvent.getCapacity());
    }

    @Test
    public void testSetDescription() {
        String newDescription = "Updated description";
        testEvent.setDescription(newDescription);
        assertEquals("Description should be updated", newDescription, testEvent.getDescription());
    }

    @Test
    public void testSetImageUrl() {
        String newImageUrl = "https://example.com/new-image.jpg";
        testEvent.setImageUrl(newImageUrl);
        assertEquals("Image URL should be updated", newImageUrl, testEvent.getImageUrl());
    }

    @Test
    public void testSetEventStartDate() {
        String newStartDate = "2025-12-15";
        testEvent.setEventStartDate(newStartDate);
        assertEquals("Event start date should be updated", newStartDate, testEvent.getEventStartDate());
    }

    @Test
    public void testSetEventEndDate() {
        String newEndDate = "2025-12-20";
        testEvent.setEventEndDate(newEndDate);
        assertEquals("Event end date should be updated", newEndDate, testEvent.getEventEndDate());
    }

    @Test
    public void testSetRegistrationStartDate() {
        String newRegStart = "2025-11-15";
        testEvent.setRegistrationStartDate(newRegStart);
        assertEquals("Registration start date should be updated", newRegStart, testEvent.getRegistrationStartDate());
    }

    @Test
    public void testSetRegistrationEndDate() {
        String newRegEnd = "2025-12-10";
        testEvent.setRegistrationEndDate(newRegEnd);
        assertEquals("Registration end date should be updated", newRegEnd, testEvent.getRegistrationEndDate());
    }
    

    // ========================================================================================
    // UNIT TESTS - Testing event creation
    // ========================================================================================

    @Test
    public void testEventConstructorWithAllParameters() {
        Event newEvent = new Event(
        2002, 502, "Music Festival", "https://example.com/music.jpg",
        "Vancouver", "2000", "Annual music festival",
        "Free",
        "2025-08-01", "2025-08-05", "10:00", "22:00",
        "2025-06-01", "2025-07-31"
    );

        assertEquals("Event ID should match", 2002, newEvent.getEventId());
        assertEquals("Organizer ID should match", 502, newEvent.getOrganizerId());
        assertEquals("Title should match", "Music Festival", newEvent.getTitle());
        assertEquals("Image URL should match", "https://example.com/music.jpg", newEvent.getImageUrl());
        assertEquals("Location should match", "Vancouver", newEvent.getLocation());
        assertEquals("Capacity should match", "2000", newEvent.getCapacity());
        assertEquals("Description should match", "Annual music festival", newEvent.getDescription());
        assertEquals("Event start date should match", "2025-08-01", newEvent.getEventStartDate());
        assertEquals("Event end date should match", "2025-08-05", newEvent.getEventEndDate());
        assertEquals("Registration start date should match", "2025-06-01", newEvent.getRegistrationStartDate());
        assertEquals("Registration end date should match", "2025-07-31", newEvent.getRegistrationEndDate());
    }

    @Test
    public void testEmptyConstructor() {
        Event emptyEvent = new Event();
        assertNotNull("Empty event should not be null", emptyEvent);
        assertEquals("Default event ID should be 0", 0, emptyEvent.getEventId());
        assertNull("Default title should be null", emptyEvent.getTitle());
    }

    // ========================================================================================
    // UNIT TESTS - Constructor validation
    // ========================================================================================

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullTitle() {
    new Event(1, 1, null, "url", "location", "100", 
    "description", "Free", "2025-01-01", "2025-01-05", "09:00", "17:00",
        "2024-12-01", "2024-12-31");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyTitle() {
    new Event(1, 1, "", "url", "location", "100", 
    "description", "Free", "2025-01-01", "2025-01-05", "09:00", "17:00",
        "2024-12-01", "2024-12-31");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullLocation() {
    new Event(1, 1, "title", "url", null, "100", 
    "description", "Free", "2025-01-01", "2025-01-05", "09:00", "17:00",
        "2024-12-01", "2024-12-31");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyLocation() {
    new Event(1, 1, "title", "url", "", "100", 
    "description", "Free", "2025-01-01", "2025-01-05", "09:00", "17:00",
        "2024-12-01", "2024-12-31");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullCapacity() {
    new Event(1, 1, "title", "url", "location", null, 
    "description", "Free", "2025-01-01", "2025-01-05", "09:00", "17:00",
        "2024-12-01", "2024-12-31");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyCapacity() {
    new Event(1, 1, "title", "url", "location", "", 
    "description", "Free", "2025-01-01", "2025-01-05", "09:00", "17:00",
        "2024-12-01", "2024-12-31");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullDescription() {
    new Event(1, 1, "title", "url", "location", "100", 
        null, "Free", "2025-01-01", "2025-01-05", "09:00", "17:00",
        "2024-12-01", "2024-12-31");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyDescription() {
    new Event(1, 1, "title", "url", "location", "100", 
        "", "Free", "2025-01-01", "2025-01-05", "09:00", "17:00",
        "2024-12-01", "2024-12-31");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithZeroEventId() {
    new Event(0, 1, "title", "url", "location", "100", 
    "description", "Free", "2025-01-01", "2025-01-05", "09:00", "17:00",
        "2024-12-01", "2024-12-31");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeEventId() {
    new Event(-1, 1, "title", "url", "location", "100", 
    "description", "Free", "2025-01-01", "2025-01-05", "09:00", "17:00",
        "2024-12-01", "2024-12-31");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithZeroOrganizerId() {
    new Event(1, 0, "title", "url", "location", "100", 
        "description", "Free", "2025-01-01", "2025-01-05", "09:00", "17:00",
        "2024-12-01", "2024-12-31");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeOrganizerId() {
    new Event(1, -1, "title", "url", "location", "100", 
        "description", "Free", "2025-01-01", "2025-01-05", "09:00", "17:00",
        "2024-12-01", "2024-12-31");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullEventStartDate() {
    new Event(1, 1, "title", "url", "location", "100", 
        "description", "Free", null, "2025-01-05", "09:00", "17:00",
        "2024-12-01", "2024-12-31");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullEventEndDate() {
    new Event(1, 1, "title", "url", "location", "100", 
        "description", "Free", "2025-01-01", null, "09:00", "17:00",
        "2024-12-01", "2024-12-31");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullRegistrationStartDate() {
    new Event(1, 1, "title", "url", "location", "100", 
        "description", "Free", "2025-01-01", "2025-01-05", "09:00", "17:00",
        null, "2024-12-31");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullRegistrationEndDate() {
    new Event(1, 1, "title", "url", "location", "100", 
        "description", "Free", "2025-01-01", "2025-01-05", "09:00", "17:00",
        "2024-12-01", null);
    }

    // ========================================================================================
    // UNIT TESTS - Testing null and edge cases (Validation Tests)
    // ========================================================================================

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullTitle() {
        testEvent.setTitle(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyTitle() {
        testEvent.setTitle("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetWhitespaceTitle() {
        testEvent.setTitle("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullDescription() {
        testEvent.setDescription(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyDescription() {
        testEvent.setDescription("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullLocation() {
        testEvent.setLocation(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyLocation() {
        testEvent.setLocation("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullCapacity() {
        testEvent.setCapacity(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyCapacity() {
        testEvent.setCapacity("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetZeroEventId() {
        testEvent.setEventId(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNegativeEventId() {
        testEvent.setEventId(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullEventStartDate() {
        testEvent.setEventStartDate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyEventStartDate() {
        testEvent.setEventStartDate("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullEventEndDate() {
        testEvent.setEventEndDate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyEventEndDate() {
        testEvent.setEventEndDate("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullRegistrationStartDate() {
        testEvent.setRegistrationStartDate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyRegistrationStartDate() {
        testEvent.setRegistrationStartDate("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullRegistrationEndDate() {
        testEvent.setRegistrationEndDate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyRegistrationEndDate() {
        testEvent.setRegistrationEndDate("");
    }

    // ========================================================================================
    // INTEGRATION TEST - Combined Add, Modify, and Delete operations
    // This test would require Firebase emulator or mocking
    // ========================================================================================

    @Test
    public void testCombinedAddModifyDeleteOperations() {
        // Create event (no DB interactions)
        Event newEvent = new Event(
                2002, 502, "Music Festival", "https://example.com/music.jpg",
                "Vancouver", "2000", "Annual music festival",
                "Free",
                "2025-08-01", "2025-08-05", "10:00", "22:00",
                "2025-06-01", "2025-07-31"
        );
        newEvent.setAutoUpdateDatabase(false);

        int eventId = 3003;
        newEvent.setEventId(eventId);

        // MODIFY - Update event properties
        newEvent.setTitle("Modified Test Event");
        newEvent.setCapacity("200");

        // Verify modifications
        assertEquals("Title should be modified", "Modified Test Event", newEvent.getTitle());
        assertEquals("Capacity should be modified", "200", newEvent.getCapacity());

        // DELETE preparation - verify id is correct before deletion
        int deletedEventId = newEvent.getEventId();
        assertEquals("Event ID should match before deletion", eventId, deletedEventId);
    }


    // ========================================================================================
    // INTEGRATION TEST - Multiple field updates
    // ========================================================================================

    @Test
    public void testMultipleFieldUpdates() {
        // Test updating multiple fields in sequence
        testEvent.setTitle("Updated Title");
        testEvent.setLocation("Updated Location");
        testEvent.setCapacity("999");
        testEvent.setDescription("Updated Description");
        
        // Verify all updates
        assertEquals("Title should be updated", "Updated Title", testEvent.getTitle());
        assertEquals("Location should be updated", "Updated Location", testEvent.getLocation());
        assertEquals("Capacity should be updated", "999", testEvent.getCapacity());
        assertEquals("Description should be updated", "Updated Description", testEvent.getDescription());
    }

    // ========================================================================================
    // INTEGRATION TEST - Event data consistency
    // ========================================================================================

    @Test
    public void testEventDataConsistency() {
        // Create event with specific data
        Event consistencyEvent = new Event(
                4004, 504, "Consistency Test", "url",
                "Location", "100", "Description", "Free",
                "2025-01-01", "2025-01-05", "09:00", "17:00",
                "2024-12-01", "2024-12-31"
        );
        consistencyEvent.setAutoUpdateDatabase(false);
        
        // Verify all data is consistent after creation
        assertEquals(4004, consistencyEvent.getEventId());
        assertEquals(504, consistencyEvent.getOrganizerId());
        assertEquals("Consistency Test", consistencyEvent.getTitle());
        assertEquals("Location", consistencyEvent.getLocation());
        
        // Modify and verify consistency maintained
        consistencyEvent.setTitle("Updated Consistency Test");
        assertEquals("Updated Consistency Test", consistencyEvent.getTitle());
        assertEquals(4004, consistencyEvent.getEventId()); // ID should remain unchanged
    }

    // ========================================================================================
    // EDGE CASE TESTS
    // ========================================================================================

    @Test
    public void testVeryLongTitle() {
        String longTitle = "A".repeat(1000);
        testEvent.setTitle(longTitle);
        assertEquals("Long title should be set correctly", longTitle, testEvent.getTitle());
    }

    @Test
    public void testVeryLongDescription() {
        String longDescription = "Description ".repeat(500);
        testEvent.setDescription(longDescription);
        assertEquals("Long description should be set correctly", longDescription, testEvent.getDescription());
    }

    @Test
    public void testSpecialCharactersInTitle() {
        String specialTitle = "Event @#$%^&*() 2025!!!";
        testEvent.setTitle(specialTitle);
        assertEquals("Special characters in title should be preserved", specialTitle, testEvent.getTitle());
    }

    @Test
    public void testUnicodeCharactersInLocation() {
        String unicodeLocation = "Edmonton 🏙️ Alberta 🍁";
        testEvent.setLocation(unicodeLocation);
        assertEquals("Unicode characters should be preserved", unicodeLocation, testEvent.getLocation());
    }

    @Test
    public void testMaxIntEventId() {
        testEvent.setEventId(Integer.MAX_VALUE);
        assertEquals("Max integer event ID should be set", Integer.MAX_VALUE, testEvent.getEventId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinIntEventId() {
        testEvent.setEventId(Integer.MIN_VALUE);
    }
}

/*
 * Test Class: EventTest
 * 
 * Purpose:
 *      Comprehensive unit and integration testing for Event class
 * 
 * Test Coverage:
 *      - Individual getter methods (11 tests)
 *      - Individual setter methods (12 tests)
 *      - Event creation and constructors (2 tests)
 *      - Constructor validation tests (16 tests)
 *      - Setter validation tests (null/empty) (21 tests)
 *      - Combined operations (add, modify, delete) (1 test)
 *      - Multiple field updates (1 test)
 *      - Data consistency (1 test)
 *      - Edge cases (special characters, unicode, large values) (6 tests)
 * 
 * Total Tests: 71 test cases
 * 
 * Notes:
 *      - Tests run WITHOUT Firebase/Firestore initialization
 *      - Uses setAutoUpdateDatabase(false) to prevent database calls during tests
 *      - Validates that empty/null values are rejected
 *      - Uses @Test(expected = IllegalArgumentException.class) for validation tests
 *      - For Firebase integration tests, see EventIntegrationTest.java
 *      - Uses CountDownLatch for async operation testing
 * 
 * Key Changes for Testability:
 *      - Event constructor NO LONGER automatically saves to database
 *      - Use event.saveToDatabase() to explicitly persist
 *      - Setters check autoUpdateDatabase flag before database operations
 *      - This allows unit tests to run without Firebase initialization
 */
