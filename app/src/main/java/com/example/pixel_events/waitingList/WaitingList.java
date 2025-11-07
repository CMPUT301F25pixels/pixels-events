package com.example.pixel_events.waitingList;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class WaitingList {
    private String eventId;    // Identifier of the event associated with this waitlist
    private String status;     // Status of the entrant in the waitlist (e.g., "waiting", "selected")
    private ArrayList<String> waitList = new ArrayList<>(); // The WaitingList
    private ArrayList<String> selected = new ArrayList<>();
    private int maxWaitlistSize = 1000000;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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
}
