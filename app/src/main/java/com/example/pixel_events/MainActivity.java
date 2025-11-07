package com.example.pixel_events;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.entrant.EventDetailsLauncherActivity;
import com.example.pixel_events.events.EventActivity;
import com.example.pixel_events.events.EventsListActivity;
import com.example.pixel_events.notifications.LotteryNotificationService;
import com.example.pixel_events.qr.QRScannerActivity;
import com.example.pixel_events.settings.ProfileActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final String PREFS_NAME = "pixels_prefs";
    private static final String KEY_ROLE = "current_role";
    
    private DatabaseHandler db;
    private Button addFormButton;
    private Button scanButton;
    private String userRole;

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

        // Get user role from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userRole = prefs.getString(KEY_ROLE, "user");
        Log.d(TAG, "User role: " + userRole);

        // Check for lottery notifications
        checkLotteryNotifications();

        // Setup add event / join event button based on role
        addFormButton = findViewById(R.id.addEvent);
        setupAddEventButton();

        scanButton = findViewById(R.id.scan_qr_button);
        scanButton.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openQRScanner();
            } else {
                requestCameraPermission();
            }
        });
        
        setupBottomNav();
    }
    
    private void setupBottomNav() {
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navEvents = findViewById(R.id.nav_events);
        LinearLayout navScanner = findViewById(R.id.nav_scanner);
        LinearLayout navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show();
        });

        navEvents.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EventsListActivity.class);
            startActivity(intent);
        });

        navScanner.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openQRScanner();
            } else {
                requestCameraPermission();
            }
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, 
                CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openQRScanner();
            } else {
                Toast.makeText(this, "Camera permission needed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openQRScanner() {
        Intent intent = new Intent(this, QRScannerActivity.class);
        startActivity(intent);
    }

    /**
     * Setup the add event button based on user role
     * Organizers and admins see "Create Event" button
     * Entrants see "Join Event" button
     */
    private void setupAddEventButton() {
        if ("org".equals(userRole) || "admin".equals(userRole)) {
            // Organizer or admin - show "Create Event"
            addFormButton.setText("Create Event");
            addFormButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, EventActivity.class);
                startActivity(intent);
            });
            Log.d(TAG, "Setup: Create Event button for organizer/admin");
        } else {
            // Entrant - show "Join Event"
            addFormButton.setText("Join Event");
            addFormButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, EventsListActivity.class);
                startActivity(intent);
            });
            Log.d(TAG, "Setup: Join Event button for entrant");
        }
    }

    /**
     * Check for lottery notifications
     * Called when the app starts to notify users about registration deadlines
     * that have passed for events they're on the waitlist for
     */
    private void checkLotteryNotifications() {
        LotteryNotificationService notificationService = new LotteryNotificationService(this);
        notificationService.checkAndNotifyRegistrationDeadlines();
        Log.d(TAG, "Checked for lottery notifications");
    }
}