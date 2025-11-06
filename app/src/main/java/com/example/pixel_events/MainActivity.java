package com.example.pixel_events;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.EventActivity;
import com.example.pixel_events.organizer.OrganizerActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private DatabaseHandler db;
    private Button entrantButton, organizerButton, adminButton;

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

        // Verify Firebase initialization (already done in PixelEventsApplication)
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            Log.d(TAG, "Firebase is initialized: " + (app != null));
            
            // Verify Firestore connection
            FirebaseFirestore.getInstance();
            Log.d(TAG, "Firestore instance retrieved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization error", e);
        }

        // Init DatabaseHandler (singleton pattern)
        db = DatabaseHandler.getInstance();
        Log.d(TAG, "DatabaseHandler initialized");

        // Initialize buttons
        entrantButton = findViewById(R.id.EntrantButton);
        organizerButton = findViewById(R.id.OrganizerButton);
        adminButton = findViewById(R.id.AdminButton);

        organizerButton.setOnClickListener(v -> {
            Log.d(TAG, "Organizer button clicked");
            Intent intent = new Intent(MainActivity.this, OrganizerActivity.class);
            startActivity(intent);
        });
    }
}