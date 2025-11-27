package com.example.pixel_events.waitinglist;

import android.util.Log;
import com.example.pixel_events.database.DatabaseHandler;
import java.util.List;

public class WaitlistUser {
    private static final String TAG = "WaitlistUser";
    private int userId;
    // Status mapping:
    // 0 - waiting (not chosen yet)
    // 1 - chosen (selected by lottery, awaiting response)
    // 2 - accepted
    // 3 - declined
    private int status;

    public WaitlistUser() {}

    public WaitlistUser(int userId, int status) {
        this.userId = userId;
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }
    public int getStatus() {
        return status;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public interface OnStatusUpdateListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void updateStatusInDb(int eventId, int newStatus, OnStatusUpdateListener listener) {
        // Load the waitlist document, update the existing waitList array in-place
        DatabaseHandler.getInstance().getWaitingList(eventId, waitingList -> {
            if (waitingList != null && waitingList.getWaitList() != null) {
                boolean found = false;
                for (WaitlistUser u : waitingList.getWaitList()) {
                    if (u.getUserId() == this.userId) {
                        u.setStatus(newStatus);
                        found = true;
                        break;
                    }
                }
                if (found) {
                    DatabaseHandler.getInstance().getWaitListCollection()
                            .document(String.valueOf(eventId))
                            .update("waitList", waitingList.getWaitList())
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Waitlist status updated for user in event " + eventId);
                                this.status = newStatus;
                                if (listener != null) listener.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to update waitlist status", e);
                                if (listener != null) listener.onFailure(e);
                            });
                } else {
                    if (listener != null) listener.onFailure(new Exception("User not found in waitList"));
                }
            } else {
                if (listener != null) listener.onFailure(new Exception("Waitlist not found or empty"));
            }
        }, e -> {
            Log.e(TAG, "Failed to get waitlist for status update", e);
            if (listener != null) listener.onFailure(e);
        });
    }
}
