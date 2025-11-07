package com.example.pixel_events.events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.notifications.LotteryNotificationService;
import com.example.pixel_events.waitingList.WaitingList;

/**
 * EventDetailsActivity
 *
 * Displays detailed information about an event loaded from Firebase.
 * Allows users to view event details and join the waiting list.
 * 
 * Implements US 01.06.01 (view event details) and US 01.06.02 (signup from details).
 */
public class EventDetailsActivity extends AppCompatActivity {
    private static final String TAG = "EventDetailsActivity";
    private static final String PREFS_NAME = "pixels_prefs";
    private static final String KEY_PROFILE_ID = "current_profile_id";
    
    private TextView titleText;
    private TextView locationText;
    private TextView datesText;
    private TextView timesText;
    private TextView descriptionText;
    private TextView capacityText;
    private TextView feeText;
    private Button joinButton;
    private Button leaveButton;
    private Button editButton;
    private Button testNotificationButton;
    private Button backButton;

    private String eventId;
    private String currentUserId;
    private Event currentEvent;
    private WaitingList waitingList;
    private boolean isOnWaitlist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Get event id from intent
        eventId = getIntent().getStringExtra("eventId");

        // Get current user ID
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserId = prefs.getString(KEY_PROFILE_ID, "0");

        // Initialize waiting list
        waitingList = new WaitingList(eventId);

        // Find views
        titleText = findViewById(R.id.event_title);
        locationText = findViewById(R.id.event_location);
        datesText = findViewById(R.id.event_dates);
        timesText = findViewById(R.id.event_times);
        descriptionText = findViewById(R.id.event_description);
        capacityText = findViewById(R.id.event_capacity);
        feeText = findViewById(R.id.event_fee);
        joinButton = findViewById(R.id.join_button);
        leaveButton = findViewById(R.id.leave_button);
        editButton = findViewById(R.id.edit_button);
        testNotificationButton = findViewById(R.id.test_notification_button);
        backButton = findViewById(R.id.back_button);

        // Load event from database
        loadEvent();
        checkWaitlistStatus();

        // Button listeners
        joinButton.setOnClickListener(v -> joinWaitlist());
        leaveButton.setOnClickListener(v -> leaveWaitlist());
        editButton.setOnClickListener(v -> openEditEvent());
        testNotificationButton.setOnClickListener(v -> testNotification());
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, EventsListActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void checkWaitlistStatus() {
        DatabaseHandler db = DatabaseHandler.getInstance();
        db.getWaitingList(eventId, 
            retrievedWaitlist -> {
                if (retrievedWaitlist != null) {
                    waitingList = retrievedWaitlist;
                    isOnWaitlist = waitingList.isEntrantOnList(currentUserId);
                    updateButtonVisibility();
                }
            },
            error -> {
                Log.e(TAG, "Error checking waitlist status", error);
            }
        );
    }

    private void updateButtonVisibility() {
        // Check if current user is the organizer
        boolean isOrganizer = currentEvent != null && 
                              String.valueOf(currentEvent.getOrganizerId()).equals(currentUserId);
        
        if (isOrganizer) {
            // Organizer sees edit button, not join/leave buttons
            joinButton.setVisibility(Button.GONE);
            leaveButton.setVisibility(Button.GONE);
            editButton.setVisibility(Button.VISIBLE);
        } else {
            // Regular users see join/leave buttons based on waitlist status
            editButton.setVisibility(Button.GONE);
            if (isOnWaitlist) {
                joinButton.setVisibility(Button.GONE);
                leaveButton.setVisibility(Button.VISIBLE);
            } else {
                joinButton.setVisibility(Button.VISIBLE);
                leaveButton.setVisibility(Button.GONE);
            }
        }
    }
    
    private void openEditEvent() {
        Intent intent = new Intent(this, EventActivity.class);
        intent.putExtra("eventId", eventId);
        intent.putExtra("isEditMode", true);
        startActivity(intent);
        finish();
    }
    
    private void testNotification() {
        if (currentEvent != null) {
            LotteryNotificationService notificationService = new LotteryNotificationService(this);
            notificationService.sendTestNotification(currentEvent.getTitle());
            Toast.makeText(this, "Test notification sent!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Test notification sent for event: " + currentEvent.getTitle());
        }
    }

    private void joinWaitlist() {
        waitingList.addEntrant(currentUserId);
        isOnWaitlist = true;
        updateButtonVisibility();
        Toast.makeText(this, "Successfully joined waiting list!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "User " + currentUserId + " joined waitlist for event " + eventId);
    }

    private void leaveWaitlist() {
        waitingList.removeEntrant(currentUserId);
        isOnWaitlist = false;
        updateButtonVisibility();
        Toast.makeText(this, "Left waiting list", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "User " + currentUserId + " left waitlist for event " + eventId);
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
                    currentEvent = event;
                    titleText.setText(event.getTitle());
                    locationText.setText(event.getLocation());
                    datesText.setText(event.getEventStartDate() + " to " + event.getEventEndDate());
                    timesText.setText(event.getEventStartTime() + " - " + event.getEventEndTime());
                    descriptionText.setText(event.getDescription());
                    capacityText.setText("Capacity: " + event.getCapacity());
                    feeText.setText("Fee: " + event.getFee());
                    updateButtonVisibility();
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
