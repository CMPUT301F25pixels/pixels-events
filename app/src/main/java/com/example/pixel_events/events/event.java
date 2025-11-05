package com.example.pixel_events.events;

public class event {
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
        createEvent();

    }

    private void createEvent() {
    }

    // Getters and Setters
    public int getEventId() {
        return eventId;
    }
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getCapacity() {
        return capacity;
    }
    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getWaitlistId() {
        return waitlistId;
    }
    public void setWaitlistId(int waitlistId) {
        this.waitlistId = waitlistId;
    }
    public int getOrganizerId() {
        return organizerId;
    }
    public void setOrganizerId(int organizerId) {
        this.organizerId = organizerId;
    }
    public String getQrCode() {
        return qrCode;
    }
    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
    public String getEventStartDate() {
        return eventStartDate;
    }
    public void setEventStartDate(String eventStartDate) {
        this.eventStartDate = eventStartDate;
    }
    public String getEventEndDate() {
        return eventEndDate;
    }
    public void setEventEndDate(String eventEndDate) {
        this.eventEndDate = eventEndDate;
    }
    public String getRegistrationStartDate() {
        return registrationStartDate;
    }
    public void setRegistrationStartDate(String registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }
    public String getRegistrationEndDate() {
        return registrationEndDate;
    }
    public void setRegistrationEndDate(String registrationEndDate) {
        this.registrationEndDate = registrationEndDate;
    }
    


}
