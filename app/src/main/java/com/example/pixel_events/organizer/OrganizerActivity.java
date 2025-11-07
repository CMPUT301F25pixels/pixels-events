package com.example.pixel_events.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pixel_events.MainActivity;
import com.example.pixel_events.R;
import com.example.pixel_events.events.EventActivity;

/**
 * OrganizerActivity
 * 
 * This activity provides the organizer interface where organizers can manage events.
 * It includes navigation to create new events and a back button to return to MainActivity.
 */
public class OrganizerActivity extends AppCompatActivity {
    private static final String TAG = "OrganizerActivity";
    private Button addEventButton, backButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer);
        
        // Apply window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.d(TAG, "OrganizerActivity created");

        // Initialize buttons
        addEventButton = findViewById(R.id.organizerAddEvent);
        backButton = findViewById(R.id.organizerBackButton);

        // Set up Add Event button click listener
        addEventButton.setOnClickListener(v -> {
            Log.d(TAG, "Add Event button clicked");
            Intent intent = new Intent(OrganizerActivity.this, EventActivity.class);
            startActivity(intent);
        });

        // Set up Back button click listener
        backButton.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            // Navigate back to MainActivity
            Intent intent = new Intent(OrganizerActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Handle the hardware back button press
     */
//    @Override
//    public void onBackPressed() {
//        Log.d(TAG, "Hardware back button pressed");
//        super.onBackPressed();
//        finish();
//    }
}
