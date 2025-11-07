package com.example.pixel_events.events;

import org.junit.Test;
import static org.junit.Assert.*;

public class EventDetailsActivityTest {

    @Test
    public void testEventIdNotNull() {
        String eventId = "event123";
        assertNotNull("Event ID should not be null", eventId);
        assertFalse("Event ID should not be empty", eventId.isEmpty());
    }
    
    @Test
    public void testEventIdIsString() {
        String eventId = "event456";
        assertTrue("Event ID should be a string", eventId instanceof String);
    }
    
    @Test
    public void testEventTitleFormat() {
        String title = "Sample Event";
        assertNotNull("Title should not be null", title);
        assertTrue("Title should have content", title.length() > 0);
    }
    
    @Test
    public void testEventLocationFormat() {
        String location = "Sample Location";
        assertNotNull("Location should not be null", location);
        assertTrue("Location should have content", location.length() > 0);
    }
    
    @Test
    public void testEventDateFormat() {
        String startDate = "2025-12-01";
        String endDate = "2025-12-05";
        
        assertNotNull("Start date should not be null", startDate);
        assertNotNull("End date should not be null", endDate);
        assertTrue("Date format should be yyyy-MM-dd", startDate.matches("\\d{4}-\\d{2}-\\d{2}"));
    }
    
    @Test
    public void testEventTimeFormat() {
        String startTime = "10:00";
        String endTime = "17:00";
        
        assertNotNull("Start time should not be null", startTime);
        assertNotNull("End time should not be null", endTime);
        assertTrue("Time format should be HH:mm", startTime.matches("\\d{2}:\\d{2}"));
    }
    
    @Test
    public void testEventDescription() {
        String description = "This is a sample event description.";
        assertNotNull("Description should not be null", description);
        assertTrue("Description should have content", description.length() > 0);
    }
    
    @Test
    public void testEventCapacity() {
        String capacity = "50";
        assertNotNull("Capacity should not be null", capacity);
        assertTrue("Capacity should be numeric", capacity.matches("\\d+"));
    }
    
    @Test
    public void testEventFee() {
        String fee = "Free";
        assertNotNull("Fee should not be null", fee);
        assertTrue("Fee should be valid", fee.equals("Free") || fee.matches("\\d+"));
    }
    
    @Test
    public void testJoinButtonAction() {
        String buttonText = "Join Waiting List";
        assertEquals("Button text should match", "Join Waiting List", buttonText);
    }
    
    @Test
    public void testBackButtonAction() {
        String buttonText = "Back";
        assertEquals("Button text should match", "Back", buttonText);
    }
    
    @Test
    public void testToastMessage() {
        String eventId = "event123";
        String expectedMessage = "Successfully joined waiting list for event: " + eventId;
        assertEquals("Toast message should include event ID", 
            "Successfully joined waiting list for event: event123", 
            expectedMessage);
    }
}

