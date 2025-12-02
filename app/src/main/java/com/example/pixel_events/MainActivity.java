package com.example.pixel_events;

import android.os.Bundle;
import android.util.Log;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.login.LoginFragment;

/**
 * MainActivity
 *
 * Entry point activity for the application.
 * Initializes Firebase and routes users based on authentication state.
 * Hosts the login/signup flow before navigating to role-specific activities.
 *
 * Collaborators:
 * - LoginFragment: Authentication UI
 * - AuthManager: Session check
 * - DatabaseHandler: Firebase initialization
 * - DashboardActivity, AdminActivity: Post-login routing
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private DatabaseHandler db;
    private static final int REQ_LOCATION_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DatabaseHandler.getInstance();
        Log.d(TAG, "DatabaseHandler initialized");

        // Request location permission before showing login flow
        requestLocationPermissionThenShowLogin();
    }

    private void showLoginFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
    }

    private void requestLocationPermissionThenShowLogin() {
        boolean fineGranted = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {
            showLoginFragment();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOCATION_PERMISSION);
            // Proceed to show login; fragment can re-check after permission result
            showLoginFragment();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION_PERMISSION) {
            boolean granted = false;
            for (int res : grantResults) {
                if (res == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }
            Log.d(TAG, "Location permission " + (granted ? "granted" : "denied"));
            // No further action needed here; LoginFragment will read location if granted
        }
    }
}
