package com.example.pixel_events.waitinglist;

import android.util.Log;
import com.example.pixel_events.database.DatabaseHandler;
import java.util.List;

public class WaitlistUser {
    private static final String TAG = "WaitlistUser";
    private int userId;
    private int status; // 0: undecided, 1: accepted, 2: declined

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
        DatabaseHandler.getInstance().getWaitingList(eventId, waitingList -> {
            if (waitingList != null && waitingList.getSelected() != null) {
                boolean found = false;
                for (WaitlistUser u : waitingList.getSelected()) {
                    if (u.getUserId() == this.userId) {
                        u.setStatus(newStatus);
                        found = true;
                        break;
                    }
                }
                if (found) {
                    DatabaseHandler.getInstance().getWaitListCollection()
                            .document(String.valueOf(eventId))
                            .update("selected", waitingList.getSelected())
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
                    if (listener != null) listener.onFailure(new Exception("User not found in selected list"));
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
