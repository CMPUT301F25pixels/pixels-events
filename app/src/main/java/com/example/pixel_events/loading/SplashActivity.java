package com.example.pixel_events.loading;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.pixel_events.MainActivity;
import com.example.pixel_events.R;

/**
 * SplashActivity
 *
 * Initial loading screen displayed on app launch.
 * Shows branding and transitions to MainActivity after brief delay.
 *
 * Collaborators:
 * - MainActivity: Post-splash navigation
 */
public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int REQ_LOCATION_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Ensure permissions are requested before transitioning to MainActivity
        requestLocationPermissionThenProceed();
    }

    private void proceedToMain() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 500); // shorter delay now that splash is configured
    }

    private void requestLocationPermissionThenProceed() {
        boolean fineGranted = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {
            proceedToMain();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION },
                    REQ_LOCATION_PERMISSION);
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
            // Regardless of grant/deny, proceed to Main; MainActivity can adapt features
            // based on permission state
            proceedToMain();
        }
    }
}
