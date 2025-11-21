package com.example.pixel_events.login;

/**
 * Class exposing authenticated user details to the UI.
 */
public class LoggedInUserView {
    private String displayName;
    private String userId;

    LoggedInUserView(String displayName, String userId) {
        this.displayName = displayName;
        this.userId = userId;
    }

    String getDisplayName() {
        return displayName;
    }

    public String getUserId() {
        return userId;
    }
}
