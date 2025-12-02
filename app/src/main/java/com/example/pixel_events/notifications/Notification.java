package com.example.pixel_events.notifications;

import com.google.firebase.Timestamp;
import java.util.UUID;

/**
 * Notification
 *
 * Model class for user notifications in the event lottery system.
 * Supports multiple notification types including lottery results, admin actions, and organizer messages.
 * Stored per-user in Firestore subcollections and globally in NotificationLogs for admin review.
 * Tracks read/unread status and respects user notification preferences.
 *
 * Implements:
 * - US 01.04.01 (Notify lottery winners)
 * - US 01.04.02 (Notify lottery losers)
 * - US 02.05.01 (Send notifications to selected entrants)
 * - US 02.07.01 (Notify all waitlist entrants)
 * - US 02.07.02 (Notify selected entrants)
 * - US 02.07.03 (Notify cancelled entrants)
 * - US 03.08.01 (Admin review notification logs)
 *
 * Collaborators:
 * - DatabaseHandler: Stores and retrieves notifications
 * - Profile: Notification preference filtering
 * - WaitingList: Triggered by lottery draws
 */
public class Notification {
    private String notificationId;
    private String title;
    private String message;
    private Timestamp timestamp;
    private boolean read;
    private String type; // "LOTTERY_WIN", "LOTTERY_LOSS", "ADMIN_DELETE", "ORGANIZER_MESSAGE", "GENERAL"
    private int relatedEventId; // -1 if not applicable
    private int recipientId;
    private int senderId; // For organizer messages

    public Notification() {
        // Empty constructor for Firestore
    }

    public Notification(String title, String message, String type, int relatedEventId, int recipientId) {
        this(title, message, type, relatedEventId, recipientId, -1);
    }

    public Notification(String title, String message, String type, int relatedEventId, int recipientId, int senderId) {
        this.notificationId = UUID.randomUUID().toString();
        this.title = title;
        this.message = message;
        this.timestamp = Timestamp.now();
        this.read = false;
        this.type = type;
        this.relatedEventId = relatedEventId;
        this.recipientId = recipientId;
        this.senderId = senderId;
    }

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getRelatedEventId() { return relatedEventId; }
    public void setRelatedEventId(int relatedEventId) { this.relatedEventId = relatedEventId; }

    public int getRecipientId() { return recipientId; }
    public void setRecipientId(int recipientId) { this.recipientId = recipientId; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
}
