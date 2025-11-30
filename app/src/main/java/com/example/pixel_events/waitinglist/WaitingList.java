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
    private String status;     // Status of the event ("waiting", "drawn")
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
                // status == 1 represents 'chosen' (selected by lottery, awaiting response)
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
                // status == 3 represents declined
                if (user.getStatus() == 3) {
                    cancelled.add(user);
                }
            }
        }
        return cancelled;
    }
    public ArrayList<WaitlistUser> getWaitList() {
        return waitList == null ? null : new ArrayList<>(waitList);
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

    public interface OnLotteryDrawnListener {
        void onSuccess(int numberDrawn);
        void onFailure(Exception e);
    }

    public void drawLottery(OnLotteryDrawnListener listener) {
        DatabaseHandler.getInstance().getEvent(eventId, e -> {
            int capacity = e.getCapacity();

            if (waitList == null) waitList = new ArrayList<>();

            if (capacity <= 0) {
                if (listener != null) listener.onFailure(new IllegalArgumentException("Capacity must be positive"));
                return;
            }
            // Count users who are already chosen (1) or already accepted (2)
            int occupied = 0;
            java.util.List<Integer> waitingIndices = new java.util.ArrayList<>();
            for (int i = 0; i < waitList.size(); i++) {
                WaitlistUser user = waitList.get(i);
                int st = user.getStatus();
                if (st == 1 || st == 2) {
                    occupied++;
                } else if (st == 0) {
                    waitingIndices.add(i);
                }
            }

            if (occupied >= capacity) {
                Log.d("WaitingList", "Lottery already drawn or capacity full.");
                if (listener != null) listener.onFailure(new IllegalStateException("Lottery already drawn or capacity full"));
                return;
            }

            int slotsAvailable = capacity - occupied;

            if (waitingIndices.isEmpty()) {
                if (listener != null) listener.onFailure(new IllegalStateException("No participants in the waiting list to draw from"));
                return;
            }

            int numberToDraw = Math.min(slotsAvailable, waitingIndices.size());

            // Shuffle indices to pick random waiting users and update in-place
            Collections.shuffle(waitingIndices);
            for (int i = 0; i < numberToDraw; i++) {
                int idx = waitingIndices.get(i);
                WaitlistUser winner = waitList.get(idx);
                winner.setStatus(1); // mark as chosen
            }

            // Update database
            updateDatabase("waitList", waitList);

            status = "drawn";
            updateDatabase("status", status);
            
            // Send notifications to winners and losers (US 01.04.01, 01.04.02)
            sendLotteryNotifications(e.getTitle(), waitingIndices, numberToDraw);
            
            if (listener != null) listener.onSuccess(numberToDraw);
        }, e -> {
            Log.e("WaitingList", "Failed to get event", e);
            if (listener != null) listener.onFailure(e);
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

    /**
     * Send win/loss notifications after lottery draw
     * US 01.04.01 - Notify winners
     * US 01.04.02 - Notify losers
     */
    private void sendLotteryNotifications(String eventTitle, java.util.List<Integer> waitingIndices, int numberDrawn) {
        DatabaseHandler db = DatabaseHandler.getInstance();
        
        for (int i = 0; i < waitingIndices.size(); i++) {
            int idx = waitingIndices.get(i);
            WaitlistUser user = waitList.get(idx);
            
            if (i < numberDrawn) {
                // Winner
                db.sendWinNotification(eventId, eventTitle, user.getUserId());
            } else {
                // Loser
                db.sendLossNotification(eventId, eventTitle, user.getUserId());
            }
        }
    }
}
