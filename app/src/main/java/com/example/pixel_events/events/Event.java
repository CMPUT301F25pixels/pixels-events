package com.example.pixel_events.events;

import android.util.Log;

import com.example.pixel_events.database.DatabaseHandler;

import java.util.HashMap;
import java.util.Map;

public class Event {
    private int eventId;
    private String title;
    private String imageUrl;
    private String location;
    private String capacity;
    private String description;
    private int waitlistId;
    private int organizerId;
    private String qrCode;
    private String eventStartDate;
    private String eventEndDate;
    private String registrationStartDate;
    private String registrationEndDate;

    /**
     * Empty constructor for Firebase
     */
    public Event() {}

    /**
     * Initialize Event
     * @throws IllegalArgumentException if any required field is null or empty
     */
    public Event(
            int eventId,
            int organizerId,
            String title,
            String imageUrl,
            String location,
            String capacity,
            String description,
            String eventStartDate,
            String eventEndDate,
            String registrationStartDate,
            String registrationEndDate
    ) {
        // Validate required fields
        validateNotEmpty(title, "Title");
        validateNotEmpty(location, "Location");
        validateNotEmpty(capacity, "Capacity");
        validateNotEmpty(description, "Description");
        validateNotEmpty(eventStartDate, "Event Start Date");
        validateNotEmpty(eventEndDate, "Event End Date");
        validateNotEmpty(registrationStartDate, "Registration Start Date");
        validateNotEmpty(registrationEndDate, "Registration End Date");
        
        // Validate IDs are positive
        if (eventId <= 0) {
            throw new IllegalArgumentException("Event ID must be positive");
        }
        if (organizerId <= 0) {
            throw new IllegalArgumentException("Organizer ID must be positive");
        }
        
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.location = location;
        this.capacity = capacity;
        this.description = description;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.registrationStartDate = registrationStartDate;
        this.registrationEndDate = registrationEndDate;
        // Add a reference to the QR code class
        // this.qrCode = new qrcode(this).
    }

    /**
     * Validates that a string field is not null or empty
     * @param value The value to validate
     * @param fieldName The name of the field (for error messages)
     * @throws IllegalArgumentException if value is null or empty
     */
    private void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }

    /**
     * Create and save event to database
     * @param db DatabaseHandler reference
     */
    public void createEvent(DatabaseHandler db) {
        db.addEvent(this.eventId, this.organizerId, this.title, this.imageUrl,
                this.location, this.capacity, this.description, this.eventStartDate,
                this.eventEndDate, this.registrationStartDate, this.registrationEndDate);
    }

    // Getters
    public int getEventId() {
        return eventId;
    }
    public String getTitle() {
        return title;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public String getLocation() {
        return location;
    }
    public String getCapacity() {
        return capacity;
    }
    public String getDescription() {
        return description;
    }
    public int getWaitlistId() {
        return waitlistId;
    }
    public int getOrganizerId() {
        return organizerId;
    }
    public String getQrCode() {
        return qrCode;
    }
    public String getEventStartDate() {
        return eventStartDate;
    }
    public String getEventEndDate() {
        return eventEndDate;
    }
    public String getRegistrationStartDate() {
        return registrationStartDate;
    }
    public String getRegistrationEndDate() {
        return registrationEndDate;
    }

    // Setters - These update both the local field and the database
    public void setEventId(int eventId) {
        if (eventId <= 0) {
            throw new IllegalArgumentException("Event ID must be positive");
        }
        this.eventId = eventId;
        updateDatabase("eventId", eventId);
    }
    
    public void setTitle(String title) {
        validateNotEmpty(title, "Title");
        this.title = title;
        updateDatabase("title", title);
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        updateDatabase("imageUrl", imageUrl);
    }
    
    public void setLocation(String location) {
        validateNotEmpty(location, "Location");
        this.location = location;
        updateDatabase("location", location);
    }
    
    public void setCapacity(String capacity) {
        validateNotEmpty(capacity, "Capacity");
        this.capacity = capacity;
        updateDatabase("capacity", capacity);
    }
    
    public void setDescription(String description) {
        validateNotEmpty(description, "Description");
        this.description = description;
        updateDatabase("description", description);
    }
    
    public void setWaitlistId(int waitlistId) {
        this.waitlistId = waitlistId;
        updateDatabase("waitlistId", waitlistId);
    }
    
    public void setOrganizerId(int organizerId) {
        if (organizerId <= 0) {
            throw new IllegalArgumentException("Organizer ID must be positive");
        }
        this.organizerId = organizerId;
        updateDatabase("organizerId", organizerId);
    }
    
    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
        updateDatabase("qrCode", qrCode);
    }
    
    public void setEventStartDate(String eventStartDate) {
        validateNotEmpty(eventStartDate, "Event Start Date");
        this.eventStartDate = eventStartDate;
        updateDatabase("eventStartDate", eventStartDate);
    }
    
    public void setEventEndDate(String eventEndDate) {
        validateNotEmpty(eventEndDate, "Event End Date");
        this.eventEndDate = eventEndDate;
        updateDatabase("eventEndDate", eventEndDate);
    }
    
    public void setRegistrationStartDate(String registrationStartDate) {
        validateNotEmpty(registrationStartDate, "Registration Start Date");
        this.registrationStartDate = registrationStartDate;
        updateDatabase("registrationStartDate", registrationStartDate);
    }
    
    public void setRegistrationEndDate(String registrationEndDate) {
        validateNotEmpty(registrationEndDate, "Registration End Date");
        this.registrationEndDate = registrationEndDate;
        updateDatabase("registrationEndDate", registrationEndDate);
    }

    /**
     * Helper method to update a single field in the database
     * @param fieldName The name of the field to update
     * @param value The new value for the field
     */
    private void updateDatabase(String fieldName, Object value) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(fieldName, value);
        
        DatabaseHandler.getInstance().modifyEvent(this.eventId, updates, error -> {
            if (error != null) {
                Log.e("Event", "Failed to update " + fieldName + ": " + error);
            }
        });
    }
}

/*
 * Class:
 *      Event
 *
 * Responsibilities:
 *      Setup an object to easily use event data
 *      Provide getters and setters for all fields
 *      Create event in database
 *
 * Collaborators:
 *      DatabaseHandler
 */
