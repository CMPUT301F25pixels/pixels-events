package com.example.pixel_events;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pixel_events.database.DatabaseHandler;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private DatabaseHandler db;

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
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            Log.d(TAG, "Firestore instance retrieved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization error", e);
        }

        // Init DatabaseHandler (singleton pattern)
        db = DatabaseHandler.getInstance();
        Log.d(TAG, "DatabaseHandler initialized");
    }
}