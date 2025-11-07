package com.example.pixel_events;

import android.Manifest;
import android.content.Intent;
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
import com.example.pixel_events.qr.QRScannerActivity;
import com.example.pixel_events.settings.ProfileActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_CODE = 100;
    private DatabaseHandler db;
    private Button addFormButton;
    private Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

        addFormButton = findViewById(R.id.addEvent);
        addFormButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EventActivity.class);
            startActivity(intent);
        });

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
}