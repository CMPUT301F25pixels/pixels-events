package com.example.pixel_events.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.profile.Profile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.function.Consumer;

/**
 * Singleton class to manage user authentication and session
 * Stores the current logged-in user's profile for global access
 */
public class AuthManager {
    private static final String TAG = "AuthManager";
    private static AuthManager instance;
    private static final String PREF_NAME = "PixelEventsPrefs";
    private static final String KEY_USER_ID = "logged_in_user_id";
    private Profile currentUserProfile;

    private AuthManager() {
        // No Firebase initialization
    }

    public static synchronized AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    public Profile getCurrentUserProfile() {
        return currentUserProfile;
    }

    public void setCurrentUserProfile(Profile profile) {
        this.currentUserProfile = profile;
    }

    public void setCurrentEmail(String email) {
        this.currentUserProfile.setEmail(email);
    }

    /**
     * Sign up a new user.
     * 
     * @param context         App context
     * @param profile         The profile to create
     * @param successCallback Callback on success
     * @param failureCallback Callback on failure
     */
    public void signup(Context context, Profile profile, Runnable successCallback,
            Consumer<Exception> failureCallback) {
        // Save to DB
        DatabaseHandler.getInstance().addAcc(profile);

        // Set as current user
        this.currentUserProfile = profile;

        // Save session
        saveSession(context, profile.getUserId());

        if (successCallback != null) {
            successCallback.run();
        }
    }

    /**
     * Log in a user by email and role.
     * 
     * @param context         App context
     * @param email           User email
     * @param role            User role
     * @param successCallback Callback on success
     * @param failureCallback Callback on failure
     */
    public void login(Context context, String email, String role, Runnable successCallback,
            Consumer<Exception> failureCallback) {
        DatabaseHandler.getInstance().getProfileByEmail(email, profile -> {
            if (profile != null) {
                // Check if role matches
                if (profile.getRole().equalsIgnoreCase(role)) {
                    this.currentUserProfile = profile;
                    saveSession(context, profile.getUserId());
                    if (successCallback != null)
                        successCallback.run();
                } else {
                    if (failureCallback != null)
                        failureCallback.accept(new Exception("Role mismatch. Please login as " + profile.getRole()));
                }
            } else {
                if (failureCallback != null)
                    failureCallback.accept(new Exception("User not found"));
            }
        }, e -> {
            if (failureCallback != null)
                failureCallback.accept(e);
        });
    }

    /**
     * Auto-login using SharedPreferences.
     * 
     * @param context         App context
     * @param successCallback Callback on success
     * @param failureCallback Callback on failure
     */
    public void autoLogin(Context context, Runnable successCallback, Runnable failureCallback) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt(KEY_USER_ID, -1);

        if (userId != -1) {
            DatabaseHandler.getInstance().getProfile(userId, profile -> {
                if (profile != null) {
                    this.currentUserProfile = profile;
                    if (successCallback != null)
                        successCallback.run();
                } else {
                    // Invalid session
                    clearSession(context);
                    if (failureCallback != null)
                        failureCallback.run();
                }
            }, e -> {
                if (failureCallback != null)
                    failureCallback.run();
            });
        } else {
            if (failureCallback != null)
                failureCallback.run();
        }
    }

    /**
     * Sign out the current user.
     * 
     * @param context App context
     */
    public void signOut(Context context) {
        this.currentUserProfile = null;
        clearSession(context);
    }

    private void saveSession(Context context, int userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_USER_ID, userId).apply();
    }

    private void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_USER_ID).apply();
    }

    public Boolean isUserLoggedIn(){
        return currentUserProfile != null;
    }
}
