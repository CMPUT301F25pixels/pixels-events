package com.example.pixel_events;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.home.DashboardActivity;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.login.LoginFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

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

        // ✅ Firebase Auth check - Auto login
        checkAuthState();
    }

    private void checkAuthState() {
        AuthManager authManager = AuthManager.getInstance();
        FirebaseUser user = authManager.getCurrentFirebaseUser();
        if (user == null) {
            // ... existing code ...
            showLoginFragment();
        } else {
            // ... existing code ...
            db.getProfile(
                    user.getUid().hashCode(),
                    profile -> {
                        if (profile != null) {
                            Log.d(TAG, "Profile loaded successfully");

                            // CRITICAL: Set the profile in AuthManager immediately
                            AuthManager.getInstance().setCurrentUserProfile(profile);

                            // NEW: Update Location automatically
                            fetchUserLocation();

                            showDashboardActivity();
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

    private void fetchUserLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted? Request it.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        com.example.pixel_events.profile.Profile p =
                                com.example.pixel_events.login.AuthManager.getInstance().getCurrentUserProfile();

                        if (p != null) {
                            // This automatically updates Firebase because of Profile.java logic
                            p.setLatitude(location.getLatitude());
                            p.setLongitude(location.getLongitude());
                            Log.d(TAG, "Location updated: " + location.getLatitude() + ", " + location.getLongitude());
                        }
                    }
                });
    }

    private void showLoginFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
    }

    private void showDashboardActivity() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish(); // Close MainActivity so the user can't go back to it
    }
}
