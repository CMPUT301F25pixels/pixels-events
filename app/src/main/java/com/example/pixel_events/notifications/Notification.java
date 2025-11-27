package com.example.pixel_events.notifications;

import com.google.firebase.Timestamp;
import java.util.UUID;

/**
 * Model class for User Notifications.
 * Stored in AccountData/{userId}/Notifications
 */
public class Notification {
    private String notificationId;
    private String title;
    private String message;
    private Timestamp timestamp;
    private boolean isRead;
    private String type; // "LOTTERY_WIN", "LOTTERY_LOSS", "ADMIN_DELETE", "INVITE", "GENERAL"
    private int relatedEventId; // -1 if not applicable
    private int recipientId;

    public Notification() {
        // Empty constructor for Firestore
    }

    public Notification(String title, String message, String type, int relatedEventId, int recipientId) {
        this.notificationId = UUID.randomUUID().toString();
        this.title = title;
        this.message = message;
        this.timestamp = Timestamp.now();
        this.isRead = false;
        this.type = type;
        this.relatedEventId = relatedEventId;
        this.recipientId = recipientId;
    }

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getRelatedEventId() { return relatedEventId; }
    public void setRelatedEventId(int relatedEventId) { this.relatedEventId = relatedEventId; }

    public int getRecipientId() { return recipientId; }
    public void setRecipientId(int recipientId) { this.recipientId = recipientId; }
}

