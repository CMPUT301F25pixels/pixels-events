package com.example.pixel_events.waitingList;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class WaitingList {
    private String eventId;    // Identifier of the event associated with this waitlist
    private String status;     // Status of the entrant in the waitlist (e.g., "waiting", "selected")
    private ArrayList<String> waitList = new ArrayList<>(); // The WaitingList
    private ArrayList<String> selected = new ArrayList<>();
    private int maxWaitlistSize = 1000000;

    public WaitingList() {
        this.waitList = new ArrayList<>();
        this.selected = new ArrayList<>();
    }

    // Constructor with waitlist max size
    public WaitingList(String eventId, int maxWaitlistSize) {
        this.eventId = eventId;
        this.status = "waiting";
        this.maxWaitlistSize = maxWaitlistSize;
    }

    // Constructor without waitlist max size
    public WaitingList(String eventId) {
        this.eventId = eventId;
        this.status = "waiting";
    }

    public String getEventId() {
        return eventId;
    }

    public String getStatus() {
        return status;
    }


    public ArrayList<String> getSelected() {
        return selected;
    }

    public ArrayList<String> getWaitList() {
        return waitList;
    }

    public int getMaxWaitlistSize() {
        return maxWaitlistSize;
    }

    /**
     * Check if an entrant is on the waitlist
     * @param entrantId The ID of the entrant to check
     * @return true if the entrant is on the waitlist, false otherwise
     */
    public boolean isEntrantOnList(String entrantId) {
        return waitList.contains(entrantId);
    }

    /**
     * Add an entrant to the waitlist
     * @param entrantId The ID of the entrant to add
     * @return true if added successfully, false if already on list or list is full
     */
    public boolean addEntrant(String entrantId) {
        if (waitList.contains(entrantId)) {
            return false; // Already on the list
        }
        if (waitList.size() >= maxWaitlistSize) {
            return false; // Waitlist is full
        }
        waitList.add(entrantId);
        // Save to database
        saveToDatabase();
        return true;
    }

    /**
     * Remove an entrant from the waitlist
     * @param entrantId The ID of the entrant to remove
     * @return true if removed successfully, false if not on list
     */
    public boolean removeEntrant(String entrantId) {
        boolean removed = waitList.remove(entrantId);
        if (removed) {
            // Save to database
            saveToDatabase();
        }
        return removed;
    }

    /**
     * Save this waiting list to the database
     */
    private void saveToDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("WaitListData")
                .document(eventId)
                .set(this)
                .addOnSuccessListener(unused -> {
                    android.util.Log.d("WaitingList", "Saved waitlist for event " + eventId);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("WaitingList", "Error saving waitlist", e);
                });
    }

    /**
     * Get the number of entrants on the waitlist
     * @return The size of the waitlist
     */
    public int getWaitlistSize() {
        return waitList.size();
    }
}
