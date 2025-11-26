package com.example.pixel_events.waitinglist;

import android.util.Log;

import com.example.pixel_events.database.DatabaseHandler;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class WaitingList {
    private static final int DEFAULT_MAX_WAITLIST_SIZE = 1000000;
    private int eventId;    // Identifier of the event associated with this waitlist
    private String status;     // Status of the event ("waiting", "selected")
    private ArrayList<WaitlistUser> waitList; // The WaitingList
    private int maxWaitlistSize;
    private boolean autoUpdateDatabase = true;

    public WaitingList() {}

    // Constructor with waitlist max size
    public WaitingList(int eventId, int maxWaitlistSize) {
        this.eventId = eventId;
        this.status = "waiting";
        this.maxWaitlistSize = maxWaitlistSize;
        this.waitList = new ArrayList<>();
    }

    // Constructor without waitlist max size
    public WaitingList(int eventId) {
        this.eventId = eventId;
        this.status = "waiting";
        this.maxWaitlistSize = DEFAULT_MAX_WAITLIST_SIZE;
        this.waitList = new ArrayList<>();
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

    // Getters
    public int getEventId() {
        return eventId;
    }
    public String getStatus() {
        return status;
    }
    @Exclude
    public ArrayList<WaitlistUser> getSelected() {
        ArrayList<WaitlistUser> selected = new ArrayList<>();
        if (waitList != null) {
            for (WaitlistUser user : waitList) {
                if (user.getStatus() == 1) {
                    selected.add(user);
                }
            }
        }
        return selected;
    }
    @Exclude
    public ArrayList<WaitlistUser> getWaiting() {
        ArrayList<WaitlistUser> waiting = new ArrayList<>();
        if (waitList != null) {
            for (WaitlistUser user : waitList) {
                if (user.getStatus() == 0) {
                    waiting.add(user);
                }
            }
        }
        return waiting;
    }
    @Exclude
    public ArrayList<WaitlistUser> getCancelled() {
        ArrayList<WaitlistUser> cancelled = new ArrayList<>();
        if (waitList != null) {
            for (WaitlistUser user : waitList) {
                if (user.getStatus() == 2) {
                    cancelled.add(user);
                }
            }
        }
        return cancelled;
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
        if (waitList != null) {
            for (WaitlistUser user : waitList) {
                if (user.getUserId() == userId) {
                    user.setStatus(newStatus);
                    updateDatabase("waitList", waitList);
                    break;
                }
            }
        }
    }

    public void drawLottery() {
        DatabaseHandler.getInstance().getEvent(eventId, e -> {
            int capacity = e.getCapacity();

            if (waitList == null) waitList = new ArrayList<>();

            if (capacity <= 0) {
                throw new IllegalArgumentException("Capacity must be positive");
            }

            ArrayList<WaitlistUser> selected = getSelected();
            int currentSelectedSize = selected.size();
            if (currentSelectedSize >= capacity) {
                Log.d("WaitingList", "Lottery already drawn or capacity full.");
                return;
            }

            int slotsAvailable = capacity - currentSelectedSize;

            ArrayList<WaitlistUser> waiting = getWaiting();
            if (waiting.isEmpty()) {
                throw new IllegalStateException("No participants in the waiting list to draw from.");
            }

            int numberToDraw = Math.min(slotsAvailable, waiting.size());

            // Shuffle to ensure random selection
            Collections.shuffle(waiting);

            for (int i = 0; i < numberToDraw; i++) {
                WaitlistUser winner = waiting.get(i);
                // Find this user in the main waitList and update status
                for (WaitlistUser user : waitList) {
                    if (user.getUserId() == winner.getUserId()) {
                        user.setStatus(1); // Set to selected
                        break;
                    }
                }
            }

            // Update database
            updateDatabase("waitList", waitList);

            status = "selected";
            updateDatabase("status", status);
        }, e -> {
            Log.e("WaitingList", "Failed to get event", e);
        });
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
