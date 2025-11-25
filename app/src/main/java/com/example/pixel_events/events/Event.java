package com.example.pixel_events.events;

import android.util.Log;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.waitinglist.WaitingList;
import com.example.pixel_events.qrcode.QRCode;
import com.example.pixel_events.utils.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Event {
    private int eventId;
    private String title;
    private String imageUrl;
    private String location;
    private String capacity;
    private String description;
    private int organizerId;
    private String qrCode;
    private String eventStartDate;
    private String eventEndDate;
    private String registrationStartDate;
    private String registrationEndDate;
    private String eventStartTime;
    private String eventEndTime;
    private String fee;
    private WaitingList waitingList;
    private ArrayList<String> tags;
    private boolean autoUpdateDatabase = true;

    public Event() {}

    public Event(
        int eventId,
        int organizerId,
        String title,
        String imageUrl,
        String location,
        String capacity,
        String description,
        String fee,
        String eventStartDate,
        String eventEndDate,
        String eventStartTime,
        String eventEndTime,
        String registrationStartDate,
        String registrationEndDate,
        ArrayList<String> tags
    )
    {
        // Validate required fields
        Validator.validateNotEmpty(title, "Title");
        Validator.validateNotEmpty(location, "Location");
        Validator.validateNotEmpty(capacity, "Capacity");
        Validator.validateNotEmpty(description, "Description");
        Validator.validateNotEmpty(eventStartDate, "Event Start Date");
        Validator.validateNotEmpty(eventEndDate, "Event End Date");
        Validator.validateNotEmpty(eventStartTime, "Event Start Time");
        Validator.validateNotEmpty(eventEndTime, "Event End Time");
        Validator.validateNotEmpty(registrationStartDate, "Registration Start Date");
        Validator.validateNotEmpty(registrationEndDate, "Registration End Date");

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
        this.tags = tags;

        // Validate dates and times
        Validator.validateDateRelations(eventStartDate, eventEndDate,
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
        this.qrCode = QRCode.generateQRCodeBase64("Event-" + this.eventId + "-" + this.organizerId);
        this.waitingList = new WaitingList(eventId);
        saveToDatabase();
    }

    public void setAutoUpdateDatabase(boolean autoUpdate) { this.autoUpdateDatabase = autoUpdate; }
    
    public void saveToDatabase()
    {
        if (!autoUpdateDatabase) {
            return;
        }

        try {
            DatabaseHandler db = DatabaseHandler.getInstance();
            db.addEvent(this);
            db.addWaitingList(this.waitingList);
        } catch (Exception e) {
            Log.e("Event", "Failed to save event to database", e);
        }
    }

    private void updateDatabase(String fieldName, Object value)
    {
        // Only update database if auto-update is enabled and event has valid ID
        if (!autoUpdateDatabase || this.eventId <= 0) {
            return;
        }

        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put(fieldName, value);

            DatabaseHandler.getInstance().modify(DatabaseHandler.getInstance().getEventCollection(),
                    this.eventId, updates, error -> {
                if (error != null) {
                    Log.e("Event", "Failed to update " + fieldName + ": " + error);
                }
            });
        } catch (Exception e) {
            Log.e("Event", "Failed to access database for update", e);
        }
    }

    // Getters
    public int getEventId() {
        return eventId;
    }
    public String getTitle() { return title; }
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

    public String getFullDescription(){
        String desc = description;
        desc += "\n\nThe event starts on " + eventStartDate + " and ends on " + eventEndDate + " from " + eventStartTime + " onwards to " + eventEndTime + ".";
        desc += "\n\nThe registration date starts from " + registrationStartDate + " to " + registrationEndDate + ".";
        desc += "\n\nThe event has a capacity of " + capacity + " people.";
        desc += "\n\nRegister now to increase your chances of joining the event.";
        return desc;
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
    public String getFee() {
        return fee;
    }
    public String getEventStartTime() {
        return eventStartTime;
    }
    public String getEventEndTime() {
        return eventEndTime;
    }
    public String getRegistrationStartDate() {
        return registrationStartDate;
    }
    public String getRegistrationEndDate() {
        return registrationEndDate;
    }
    public ArrayList<String> getTags() {
        return tags;
    }

    // Setters / Modify event
    public void setEventId(int eventId)
    {
        if (eventId <= 0) {
            throw new IllegalArgumentException("Event ID must be positive");
        }
        this.eventId = eventId;
        updateDatabase("eventId", eventId);
    }

    public void setTitle(String title)
    {
        Validator.validateNotEmpty(title, "Title");
        this.title = title;
        updateDatabase("title", title);
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
        updateDatabase("imageUrl", imageUrl);
    }

    public void setLocation(String location)
    {
        Validator.validateNotEmpty(location, "Location");
        this.location = location;
        updateDatabase("location", location);
    }

    public void setCapacity(String capacity)
    {
        Validator.validateNotEmpty(capacity, "Capacity");
        this.capacity = capacity;
        updateDatabase("capacity", capacity);
    }

    public void setDescription(String description)
    {
        Validator.validateNotEmpty(description, "Description");
        this.description = description;
        updateDatabase("description", description);
    }

    public void setEventStartDate(String eventStartDate)
    {
        Validator.validateNotEmpty(eventStartDate, "Event Start Date");
        // Validate relationships with other dates/times before applying
        Validator.validateDateRelations(eventStartDate, this.eventEndDate,
                this.registrationStartDate, this.registrationEndDate,
                this.eventStartTime, this.eventEndTime);

        this.eventStartDate = eventStartDate;
        updateDatabase("eventStartDate", eventStartDate);
    }

    public void setEventEndDate(String eventEndDate)
    {
        Validator.validateNotEmpty(eventEndDate, "Event End Date");
        Validator.validateDateRelations(this.eventStartDate, eventEndDate,
                this.registrationStartDate, this.registrationEndDate,
                this.eventStartTime, this.eventEndTime);

        this.eventEndDate = eventEndDate;
        updateDatabase("eventEndDate", eventEndDate);
    }

    public void setRegistrationStartDate(String registrationStartDate) {
        Validator.validateNotEmpty(registrationStartDate, "Registration Start Date");
        Validator.validateDateRelations(this.eventStartDate, this.eventEndDate,
            registrationStartDate, this.registrationEndDate,
            this.eventStartTime, this.eventEndTime);

        this.registrationStartDate = registrationStartDate;
        updateDatabase("registrationStartDate", registrationStartDate);
    }

    public void setRegistrationEndDate(String registrationEndDate)
    {
        Validator.validateNotEmpty(registrationEndDate, "Registration End Date");
        Validator.validateDateRelations(this.eventStartDate, this.eventEndDate,
                this.registrationStartDate, registrationEndDate,
                this.eventStartTime, this.eventEndTime);

        this.registrationEndDate = registrationEndDate;
        updateDatabase("registrationEndDate", registrationEndDate);
    }

    public String getDateString() {
        return this.eventStartDate + " - " + this.eventEndDate + " from  " + this.eventStartTime + " to " + this.eventEndTime;
    }
}
