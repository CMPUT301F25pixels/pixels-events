package com.example.pixel_events.waitinglist;

import android.util.Log;

import com.example.pixel_events.database.DatabaseHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    }

    // Constructor without waitlist max size
    public WaitingList(int eventId) {
        this.eventId = eventId;
        this.status = "waiting";
        this.maxWaitlistSize = 1000000;
    }

    public void setAutoUpdateDatabase(boolean autoUpdate) { this.autoUpdateDatabase = autoUpdate; }

    public boolean isUserInWaitlist(int userId) {
        return waitList.contains(userId);
    }

    public void addEntrantInWaitList(int userId) {
        if (isUserInWaitlist(userId)) {
            throw new IllegalArgumentException(userId + " already in waitlist");
        }
        if (waitList.size() >= maxWaitlistSize) {
            throw new IllegalArgumentException("Waitlist is full. Try again later.");
        }
        waitList.add(userId);
        updateDatabase("waitList", waitList);
    }

    public void removeEntrantInWaitList(int userId) {
        if (!isUserInWaitlist(Integer.valueOf(userId))) {
            throw new IllegalArgumentException(userId + " not present in waitlist");
        }
        waitList.remove(userId);
        updateDatabase("waitList", waitList);
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

            DatabaseHandler.getInstance().modify(DatabaseHandler.getInstance().getWaitListCollection(),
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
