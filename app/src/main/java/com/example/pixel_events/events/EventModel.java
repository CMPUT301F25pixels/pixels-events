package com.example.pixel_events.events;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventModel {
    private String title;
    private String location;
    private String type;
    private int imageResId;
    private Date dateTime;
    private String capacity;
    private String description;
    private String fee;
    private String organizerName;
    private int eventId;
    private int organizerId;

    private static final SimpleDateFormat DATE_TIME_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.US);
    private static final SimpleDateFormat TIME_ONLY_FORMAT =
            new SimpleDateFormat("h:mm a", Locale.US);

    public EventModel(String title, String date, String time, String location,
                      String type, int imageResId) {
        this.title = title;
        this.location = location;
        this.type = type;
        this.imageResId = imageResId;
        this.organizerName = "ABC Company";

        try {
            this.dateTime = DATE_TIME_FORMAT.parse(date + " " + time);
        } catch (ParseException e) {
            e.printStackTrace();
            this.dateTime = new Date(0);
        }
    }

    public EventModel(int eventId, int organizerId, String title, int imageResId,
                      String location, String capacity, String description,
                      String fee, String date, String time, String organizerName) {
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.title = title;
        this.imageResId = imageResId;
        this.location = location;
        this.capacity = capacity;
        this.description = description;
        this.fee = fee;
        this.organizerName = organizerName;
        this.type = (fee == null || fee.equalsIgnoreCase("free")) ? "Free" : "Paid";

        try {
            this.dateTime = DATE_TIME_FORMAT.parse(date + " " + time);
        } catch (ParseException e) {
            e.printStackTrace();
            this.dateTime = new Date(0);
        }
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public int getImageResId() {
        return imageResId;
    }

    public Date getDate() {
        return dateTime;
    }

    public String getCapacity() {
        return capacity;
    }

    public String getDescription() {
        return description;
    }

    public String getFee() {
        return fee;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public int getEventId() {
        return eventId;
    }

    public int getOrganizerId() {
        return organizerId;
    }

    public String getFormattedDate() {
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, EEE", Locale.US);
        return displayFormat.format(dateTime);
    }

    public String getFormattedTime() {
        return TIME_ONLY_FORMAT.format(dateTime);
    }

    public String getFormattedDateTime() {
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, EEE h:mm a", Locale.US);
        return displayFormat.format(dateTime);
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFee(String fee) {
        this.fee = fee;
        this.type = (fee == null || fee.equalsIgnoreCase("free")) ? "Free" : "Paid";
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }
}

