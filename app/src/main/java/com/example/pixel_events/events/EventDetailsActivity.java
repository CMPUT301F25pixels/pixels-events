package com.example.pixel_events.events;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;

/**
 * EventDetailsActivity
 *
 * Displays detailed information about an event loaded from Firebase.
 * Allows users to view event details and join the waiting list.
 * 
 * Implements US 01.06.01 (view event details) and US 01.06.02 (signup from details).
 */
public class EventDetailsActivity extends AppCompatActivity {
    private TextView titleText;
    private TextView locationText;
    private TextView datesText;
    private TextView timesText;
    private TextView descriptionText;
    private TextView capacityText;
    private TextView feeText;
    private Button joinButton;
    private Button backButton;

    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // get event id from intent
        eventId = getIntent().getStringExtra("eventId");

        // find views
        titleText = findViewById(R.id.event_title);
        locationText = findViewById(R.id.event_location);
        datesText = findViewById(R.id.event_dates);
        timesText = findViewById(R.id.event_times);
        descriptionText = findViewById(R.id.event_description);
        capacityText = findViewById(R.id.event_capacity);
        feeText = findViewById(R.id.event_fee);
        joinButton = findViewById(R.id.join_button);
        backButton = findViewById(R.id.back_button);

        // load event from database
        loadEvent();

        // button listeners
        joinButton.setOnClickListener(v -> {
            // TODO: Implement actual waitlist joining once WaitingList class is available
            // For now, show confirmation message
            Toast.makeText(this, "Successfully joined waiting list for event: " + eventId, Toast.LENGTH_LONG).show();
            
            // Uncomment when WaitingList integration is ready:
            /*
            WaitingList waitingList = new WaitingList(eventId);
            waitingList.addEntrant(currentUserId);
            Toast.makeText(this, "Successfully joined waiting list!", Toast.LENGTH_SHORT).show();
            */
        });

        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Load event details from Firebase using event ID
     * Populates all UI elements with event information
     * Shows error message and closes activity if event not found
     */
    private void loadEvent() {
        DatabaseHandler db = DatabaseHandler.getInstance();
        
        db.getEvent(eventId, 
            event -> {
                if (event != null) {
                    titleText.setText(event.getTitle());
                    locationText.setText(event.getLocation());
                    datesText.setText(event.getEventStartDate() + " to " + event.getEventEndDate());
                    timesText.setText(event.getEventStartTime() + " - " + event.getEventEndTime());
                    descriptionText.setText(event.getDescription());
                    capacityText.setText("Capacity: " + event.getCapacity());
                    feeText.setText("Fee: " + event.getFee());
                } else {
                    Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            },
            error -> {
                Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show();
                finish();
            }
        );
    }
}

/*
 * Class:
 *      EventDetailsActivity
 *
 * Responsibilities:
 *      Display event details from database (US 01.06.01)
 *      Allow user to join waiting list (US 01.06.02)
 *      Load event information from Firebase
 *      Handle event not found scenarios
 *
 * Collaborators:
 *      DatabaseHandler
 *      Event
 *      WaitingList (future integration)
 */
