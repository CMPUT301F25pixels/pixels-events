package com.example.pixel_events.login;

import android.util.Log;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.profile.Profile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Singleton class to manage user authentication and session
 * Stores the current logged-in user's profile for global access
 */
public class AuthManager {
    private static final String TAG = "AuthManager";
    private static AuthManager instance;

    private FirebaseAuth auth;
    private Profile currentUserProfile;

    private AuthManager() {
        auth = FirebaseAuth.getInstance();
        // Listen for auth state changes and load profile when a user is present
        auth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                loadProfileForUser(user);
            } else {
                currentUserProfile = null;
            }
        });

        // If a user is already signed in when AuthManager is created, load their
        // profile
        if (auth.getCurrentUser() != null) {
            loadProfileForUser(auth.getCurrentUser());
        }
    }

    /**
     * Force refresh of the current user profile from Firestore.
     */
    public void refreshCurrentUserProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            loadProfileForUser(user);
        }
    }

    /**
     * Load profile using Firebase UID mapped to integer id via hashCode.
     * This uses DatabaseHandler.getProfile(...) so we reuse existing DB access
     * methods.
     */
    private void loadProfileForUser(FirebaseUser user) {
        if (user == null || user.getUid() == null) {
            currentUserProfile = null;
            return;
        }

        int id = user.getUid().hashCode();
        if (id <= 0) {
            currentUserProfile = null;
            return;
        }

        DatabaseHandler.getInstance().getProfile(id,
                (OnSuccessListener<Profile>) profile -> {
                    currentUserProfile = profile;
                    Log.d(TAG, "Loaded profile for uid=" + user.getUid() + " id="
                            + (profile != null ? profile.getUserId() : "null"));
                },
                (OnFailureListener) e -> {
                    Log.e(TAG, "Failed to load profile for uid=" + user.getUid(), e);
                    currentUserProfile = null;
                });
    }

    public static synchronized AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return auth.getCurrentUser();
    }

    public Profile getCurrentUserProfile() {
        return currentUserProfile;
    }

    public void setCurrentUserProfile(Profile profile) {
        this.currentUserProfile = profile;
    }

    public void setCurrentUserEmail(String email) {
        if (auth.getCurrentUser() != null) {
            auth.getCurrentUser().updateEmail(email);
        }
    }

    /**
     * Sign out the current user
     */
    public void signOut() {
        auth.signOut();
        currentUserProfile = null;
    }

    /**
     * Check if user is logged in
     */
    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }
}
