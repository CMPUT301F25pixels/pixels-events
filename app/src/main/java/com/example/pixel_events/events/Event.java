package com.example.pixel_events.events;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.qr.QRCode;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;

import com.example.pixel_events.database.DatabaseHandler;

import java.util.HashMap;
import java.util.Map;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class Event {
    private int eventId;
    private String title;
    private String imageUrl;
    private String location;
    private String capacity;
    private String description;
    private int waitlistId;
    private int organizerId;
    private String qrCodeData;  // The data encoded in the QR code
    private transient QRCode qrCode;  // Transient so Firebase doesn't try to serialize it
    private String eventStartDate;
    private String eventEndDate;
    private String registrationStartDate;
    private String registrationEndDate;
    private String eventStartTime;
    private String eventEndTime;
    private String fee;
    
    // Flag to control whether setters should automatically update database
    private boolean autoUpdateDatabase = true;

    /**
     * Empty constructor for Firebase
     */
    public Event() {}
    
    /**
     * Disable automatic database updates (useful for testing)
     */
    public void setAutoUpdateDatabase(boolean autoUpdate) {
        this.autoUpdateDatabase = autoUpdate;
    }

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
 View event details with QR code within app            String fee,
            String eventStartDate,
            String eventEndDate,
            String eventStartTime,
            String eventEndTime,
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
        validateNotEmpty(eventStartTime, "Event Start Time");
        validateNotEmpty(eventEndTime, "Event End Time");
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
        this.qrCodeData = generateQRCodeData();
        this.qrCode = new QRCode(this.qrCodeData);
    }
    
    /**
     * Generate QR code data string for this event
     * Format: "Event-{eventId}-{organizerId}"
     * @return The QR code data string
     */
    private String generateQRCodeData() {
        return "Event-" + this.eventId + "-" + this.organizerId;

        // Validate dates and times
        validateDateRelations(eventStartDate, eventEndDate,
                registrationStartDate, registrationEndDate,
                eventStartTime, eventEndTime);
                
        // Fee: if null/empty -> assume Free; normalize user-entered "free"
        if (fee == null || fee.trim().isEmpty()) {
            this.fee = "Free";
        } else {
            String f = fee.trim();
            if (f.equalsIgnoreCase("free")) {
                this.fee = "Free";
            } else {
                this.fee = f;
            }
        }
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.eventStartTime = eventStartTime;
        this.eventEndTime = eventEndTime;
        this.registrationStartDate = registrationStartDate;
        this.registrationEndDate = registrationEndDate;

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
     * Save this event to the database
     * Call this method explicitly to persist the event to Firebase
     */
    public void saveToDatabase() {
        try {
            DatabaseHandler db = DatabaseHandler.getInstance();
            db.addEvent(this.eventId, this.organizerId, this.title, this.imageUrl,
                    this.location, this.capacity, this.description, this.fee, this.eventStartDate,
                    this.eventEndDate, this.eventStartTime, this.eventEndTime,
                    this.registrationStartDate, this.registrationEndDate);
        } catch (Exception e) {
            Log.e("Event", "Failed to save event to database", e);
        }
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
    
    /**
     * Get the QR code data string (what's encoded in the QR code)
     * @return The QR code data string
     */
    public String getQrCodeData() {
        return qrCodeData;
    }
    
    /**
     * Get the QR code object
     * If the QR code hasn't been generated yet (e.g., after Firebase retrieval),
     * it will be generated on-demand
     * @return The QRCode object containing the bitmap
     */
    public QRCode getQrCode() {
        if (qrCode == null && qrCodeData != null) {
            // Regenerate QR code if it doesn't exist (e.g., after Firebase deserialization)
            qrCode = new QRCode(qrCodeData);
        }
        return qrCode;
    }
    
    /**
     * Get the QR code as a Bitmap
     * @return The QR code bitmap, or null if generation fails
     */
    public Bitmap getQrCodeBitmap() {
        QRCode qr = getQrCode();
        return qr != null ? qr.getBitmap() : null;
    }
    
    public String getEventStartDate() {
        return eventStartDate;
    }
    public String getEventEndDate() {
        return eventEndDate;
    }
    public String getFee() {
        return fee;
    }
    /**
     * Get the event start time (HH:mm)
     * @return event start time string
     */
    public String getEventStartTime() {
        return eventStartTime;
    }

    /**
     * Get the event end time (HH:mm)
     * @return event end time string
     */
    public String getEventEndTime() {
        return eventEndTime;
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

    public void setEventStartDate(String eventStartDate) {
        validateNotEmpty(eventStartDate, "Event Start Date");
        // Validate relationships with other dates/times before applying
        validateDateRelations(eventStartDate, this.eventEndDate,
                this.registrationStartDate, this.registrationEndDate,
                this.eventStartTime, this.eventEndTime);

        this.eventStartDate = eventStartDate;
        updateDatabase("eventStartDate", eventStartDate);
    }
    
    public void setEventEndDate(String eventEndDate) {
        validateNotEmpty(eventEndDate, "Event End Date");
        validateDateRelations(this.eventStartDate, eventEndDate,
                this.registrationStartDate, this.registrationEndDate,
                this.eventStartTime, this.eventEndTime);

        this.eventEndDate = eventEndDate;
        updateDatabase("eventEndDate", eventEndDate);
    }
    
    public void setRegistrationStartDate(String registrationStartDate) {
        validateNotEmpty(registrationStartDate, "Registration Start Date");
        validateDateRelations(this.eventStartDate, this.eventEndDate,
                registrationStartDate, this.registrationEndDate,
                this.eventStartTime, this.eventEndTime);

        this.registrationStartDate = registrationStartDate;
        updateDatabase("registrationStartDate", registrationStartDate);
    }
    
    public void setRegistrationEndDate(String registrationEndDate) {
        validateNotEmpty(registrationEndDate, "Registration End Date");
        validateDateRelations(this.eventStartDate, this.eventEndDate,
                this.registrationStartDate, registrationEndDate,
                this.eventStartTime, this.eventEndTime);

        this.registrationEndDate = registrationEndDate;
        updateDatabase("registrationEndDate", registrationEndDate);
    }

    /**
     * Validate date/time formats and logical relationships:
     *  - Dates/times must parse using yyyy-MM-dd and HH:mm
     *  - All provided dates must be after today
     *  - eventEndDate > eventStartDate (when both provided)
     *  - registrationEndDate > registrationStartDate (when both provided)
     *  - registrationEndDate must be before eventStartDate (when both provided)
     *  - if event start and end are same date, eventEndTime must be after eventStartTime
     *
     * Any invalid format or violated relation throws IllegalArgumentException.
     */
    private void validateDateRelations(
            String eventStartDate,
            String eventEndDate,
            String registrationStartDate,
            String registrationEndDate,
            String eventStartTime,
            String eventEndTime
    ) {
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateFmt.setLenient(false);
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.US);
        timeFmt.setLenient(false);

        try {
            Date startDateObj = null;
            Date endDateObj = null;
            Date regStartObj = null;
            Date regEndObj = null;

            if (eventStartDate != null && !eventStartDate.trim().isEmpty()) {
                startDateObj = dateFmt.parse(eventStartDate);
            }
            if (eventEndDate != null && !eventEndDate.trim().isEmpty()) {
                endDateObj = dateFmt.parse(eventEndDate);
            }
            if (registrationStartDate != null && !registrationStartDate.trim().isEmpty()) {
                regStartObj = dateFmt.parse(registrationStartDate);
            }
            if (registrationEndDate != null && !registrationEndDate.trim().isEmpty()) {
                regEndObj = dateFmt.parse(registrationEndDate);
            }

            // Today's date at midnight
            Date today = dateFmt.parse(dateFmt.format(new Date()));

            if (startDateObj != null && !startDateObj.after(today)) {
                throw new IllegalArgumentException("Event start date must be after today");
            }
            if (endDateObj != null && !endDateObj.after(today)) {
                throw new IllegalArgumentException("Event end date must be after today");
            }
            if (regStartObj != null && !regStartObj.after(today)) {
                throw new IllegalArgumentException("Registration start date must be after today");
            }
            if (regEndObj != null && !regEndObj.after(today)) {
                throw new IllegalArgumentException("Registration end date must be after today");
            }

            // End must be after start when both present
            if (startDateObj != null && endDateObj != null && !endDateObj.after(startDateObj)) {
                throw new IllegalArgumentException("Event end date must be after event start date");
            }

            // Registration ordering
            if (regStartObj != null && regEndObj != null && !regEndObj.after(regStartObj)) {
                throw new IllegalArgumentException("Registration end date must be after registration start date");
            }

            // Registration must finish before event starts (if both present)
            if (regEndObj != null && startDateObj != null && !regEndObj.before(startDateObj)) {
                throw new IllegalArgumentException("Registration must end before the event start date");
            }

            // Time validation when both times provided
            if (eventStartTime != null && !eventStartTime.trim().isEmpty()
                    && eventEndTime != null && !eventEndTime.trim().isEmpty()) {
                Date startTimeObj = timeFmt.parse(eventStartTime);
                Date endTimeObj = timeFmt.parse(eventEndTime);

                // If event occurs on the same day, ensure end time is after start time
                if (startDateObj != null && endDateObj != null
                        && dateFmt.format(startDateObj).equals(dateFmt.format(endDateObj))) {
                    if (!endTimeObj.after(startTimeObj)) {
                        throw new IllegalArgumentException("Event end time must be after start time when on the same date");
                    }
                }
            }

        } catch (ParseException pe) {
            throw new IllegalArgumentException("Invalid date/time format: " + pe.getMessage());
        }
    }

    /**
     * Helper method to update a single field in the database
     * @param fieldName The name of the field to update
     * @param value The new value for the field
     */
    private void updateDatabase(String fieldName, Object value) {
        // Only update database if auto-update is enabled and event has valid ID
        if (!autoUpdateDatabase || this.eventId <= 0) {
            return;
        }
        
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put(fieldName, value);
            
            DatabaseHandler.getInstance().modifyEvent(this.eventId, updates, error -> {
                if (error != null) {
                    Log.e("Event", "Failed to update " + fieldName + ": " + error);
                }
            });
        } catch (Exception e) {
            Log.e("Event", "Failed to access database for update", e);
        }
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
