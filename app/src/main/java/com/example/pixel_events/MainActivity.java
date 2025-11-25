package com.example.pixel_events;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.admin.AdminActivity;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.home.DashboardActivity;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.login.LoginFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            FirebaseApp app = FirebaseApp.getInstance();
            Log.d(TAG, "Firebase is initialized: " + (app != null));

            FirebaseFirestore.getInstance();
            Log.d(TAG, "Firestore instance retrieved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization error", e);
        }

        db = DatabaseHandler.getInstance();
        Log.d(TAG, "DatabaseHandler initialized");

        try {
            AuthManager.getInstance().getAuth().getFirebaseAuthSettings()
                    .setAppVerificationDisabledForTesting(true);
            Log.d(TAG, "App verification disabled for testing");
        } catch (Exception e) {
            Log.e(TAG, "Failed to disable app verification", e);
        }

        checkAuthState();
    }

    private void checkAuthState() {
        AuthManager authManager = AuthManager.getInstance();
        FirebaseUser user = authManager.getCurrentFirebaseUser();
        if (user == null) {
            Log.d(TAG, "No user logged in — navigating to LoginFragment");
            showLoginFragment();
        } else {
            Log.d(TAG, "User already logged in: " + user.getEmail() + " — loading profile");

            // Load user profile from database
            db.getProfile(
                    DatabaseHandler.uidToId(user.getUid()),
                    profile -> {
                        if (profile != null) {
                            Log.d(TAG, "Profile loaded successfully for: " + profile.getUserId());
                            if (profile.getRole().equals("admin")) showAdminActivity();
                            else showDashboardActivity();
                        }
                    },
                    e -> {
                        Log.e(TAG, "Failed to load profile: " + e);
                        Toast.makeText(MainActivity.this,
                                "Failed to load profile. Please login again.",
                                Toast.LENGTH_LONG).show();

                        // Sign out and show login
                        authManager.signOut();
                        showLoginFragment();
                    });
        }
    }

    private void showLoginFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
    }

    private void showAdminActivity() {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
        finish(); // Close MainActivity
    }

    private void showDashboardActivity() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish(); // Close MainActivity so the user can't go back to it
    }
}
