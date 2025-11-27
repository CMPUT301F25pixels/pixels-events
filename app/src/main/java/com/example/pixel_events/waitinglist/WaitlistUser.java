package com.example.pixel_events.waitinglist;

public class WaitlistUser {
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

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
