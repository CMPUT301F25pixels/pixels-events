package com.example.pixel_events.waitinglist;

import android.util.Log;

import com.example.pixel_events.database.DatabaseHandler;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList; 
import java.util.HashMap;
import java.util.Map;


public class WaitingList {
    private static final int DEFAULT_MAX_WAITLIST_SIZE = 1000000;
    private int eventId;    // Identifier of the event associated with this waitlist
    private String status;     // Status of the event ("waiting", "selected")
    private ArrayList<WaitlistUser> waitList; // The WaitingList
    private ArrayList<WaitlistUser> selected;
    private int maxWaitlistSize;
    private boolean autoUpdateDatabase = true;

    public WaitingList() {}

    // Constructor with waitlist max size
    public WaitingList(int eventId, int maxWaitlistSize) {
        this.eventId = eventId;
        this.status = "waiting";
        this.maxWaitlistSize = maxWaitlistSize;
        this.waitList = new ArrayList<>();
        this.selected = new ArrayList<>();
    }

    // Constructor without waitlist max size
    public WaitingList(int eventId) {
        this.eventId = eventId;
        this.status = "waiting";
        this.maxWaitlistSize = DEFAULT_MAX_WAITLIST_SIZE;
        this.waitList = new ArrayList<>();
        this.selected = new ArrayList<>();
    }

    public void setAutoUpdateDatabase(boolean autoUpdate) { this.autoUpdateDatabase = autoUpdate; }

    public boolean isUserInWaitlist(int userId) {
        for (WaitlistUser user : waitList) {
            if (user.getUserId() == userId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Asynchronously joins the waitlist in Firestore and updates local list on success.
     * Throws IllegalArgumentException immediately for duplicate or full list conditions.
     */
    public Task<Void> addEntrantInWaitList(int userId) {
        if (waitList == null) {
            waitList = new ArrayList<>();
        }
        if (isUserInWaitlist(userId)) {
            return Tasks.forException(new IllegalArgumentException(userId + " already in waitlist"));
        }
        if (waitList.size() >= maxWaitlistSize) {
            throw new IllegalArgumentException("Waitlist is full. Maximum capacity of " + maxWaitlistSize + " reached.");
        }
        WaitlistUser newUser = new WaitlistUser(userId, 0); // 0 for undecided
        // Use DatabaseHandler to update remote; update local list after success
        return DatabaseHandler.getInstance()
                .joinWaitingList(eventId, userId) // This needs to be adapted for WaitlistUser
                .addOnSuccessListener(unused -> waitList.add(newUser))
                .addOnFailureListener(e -> Log.e("WaitingList", "Failed to join waitlist", e));
    }

    /**
     * Asynchronously removes entrant from waitlist in Firestore and updates local list on success.
     * Throws IllegalArgumentException if user not present.
     */
    public Task<Void> removeEntrantInWaitList(int userId) {
        if (waitList == null) {
            waitList = new ArrayList<>();
        }
        if (!isUserInWaitlist(userId)) {
            return Tasks.forException(new IllegalArgumentException(userId + " not present in waitlist"));
        }
        return DatabaseHandler.getInstance()
                .leaveWaitingList(eventId, userId)
                .addOnSuccessListener(unused -> {
                    for (int i = 0; i < waitList.size(); i++) {
                        if (waitList.get(i).getUserId() == userId) {
                            waitList.remove(i);
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("WaitingList", "Failed to leave waitlist", e));
    }


    // Getters
    public int getEventId() {
        return eventId;
    }
    public String getStatus() {
        return status;
    }
    public ArrayList<WaitlistUser> getSelected() {
        return selected;
    }
    public ArrayList<WaitlistUser> getWaitList() {
        return waitList;
    }
    public int getMaxWaitlistSize() {
        return maxWaitlistSize;
    }

    // Setters / Modify waitlist
    public void setStatus(String status) {
        this.status = status;
        updateDatabase("status", status);
    }

    public void updateUserStatus(int userId, int newStatus) {
        for (WaitlistUser user : waitList) {
            if (user.getUserId() == userId) {
                user.setStatus(newStatus);
                // TODO: Update this specific user in the database
                break;
            }
        }
        for (WaitlistUser user : selected) {
            if (user.getUserId() == userId) {
                user.setStatus(newStatus);
                // TODO: Update this specific user in the database
                break;
            }
        }
    }

    public void setMaxWaitlistSize(int maxWaitlistSize) {
        if (maxWaitlistSize <= 0) {
            throw new IllegalArgumentException("Max waitlist size must be positive");
        }
        this.maxWaitlistSize = maxWaitlistSize;
        updateDatabase("maxWaitlistSize", maxWaitlistSize);
    }

    private void updateDatabase(String fieldName, Object value) {
        // Only update database if auto-update is enabled and event has valid ID
        if (!autoUpdateDatabase || this.eventId <= 0) {
            return;
        }

        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put(fieldName, value);

            DatabaseHandler.getInstance().modify(DatabaseHandler.getInstance().getWaitListCollection(),
                    this.eventId, updates, error -> {
                if (error != null) {
                    Log.e("WaitingList", "Failed to update " + fieldName + ": " + error);
                }
            });
        } catch (Exception e) {
            Log.e("WaitingList", "Failed to access database for update", e);
        }
    }
}
