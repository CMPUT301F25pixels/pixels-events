package com.example.pixel_events.waitinglist;

import android.util.Log;

import com.example.pixel_events.database.DatabaseHandler;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class WaitingList {
    private int eventId;    // Identifier of the event associated with this waitlist
    private String status;     // Status of the entrant in the waitlist (e.g., "waiting", "selected")
    private ArrayList<Integer> waitList; // The WaitingList
    private ArrayList<Integer> selected;
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
        this.maxWaitlistSize = 1000000;
        this.waitList = new ArrayList<>();
        this.selected = new ArrayList<>();
    }

    public void setAutoUpdateDatabase(boolean autoUpdate) { this.autoUpdateDatabase = autoUpdate; }

    public boolean isUserInWaitlist(int userId) {
        return waitList.contains(userId);
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
            throw new IllegalArgumentException(userId + " already in waitlist");
        }
        if (waitList.size() >= maxWaitlistSize) {
            throw new IllegalArgumentException("Waitlist is full. Try again later.");
        }
        // Use DatabaseHandler to update remote; update local list after success
        return DatabaseHandler.getInstance()
                .joinWaitingList(eventId, userId)
                .addOnSuccessListener(unused -> waitList.add(userId))
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
            throw new IllegalArgumentException(userId + " not present in waitlist");
        }
        return DatabaseHandler.getInstance()
                .leaveWaitingList(eventId, userId)
                .addOnSuccessListener(unused -> waitList.remove(Integer.valueOf(userId)))
                .addOnFailureListener(e -> Log.e("WaitingList", "Failed to leave waitlist", e));
    }


    // Getters
    public int getEventId() {
        return eventId;
    }
    public String getStatus() {
        return status;
    }
    public ArrayList<Integer> getSelected() {
        return selected;
    }
    public ArrayList<Integer> getWaitList() {
        return waitList;
    }
    public int getMaxWaitlistSize() {
        return maxWaitlistSize;
    }
}
