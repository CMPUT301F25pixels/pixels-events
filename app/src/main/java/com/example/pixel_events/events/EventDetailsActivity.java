package com.example.pixel_events.events;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.notifications.LotteryNotificationService;
import com.example.pixel_events.qr.QRCode;
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
    private static final String KEY_ROLE = "current_role";
    
    private TextView titleText;
    private TextView locationText;
    private TextView datesText;
    private TextView timesText;
    private TextView descriptionText;
    private TextView capacityText;
    private TextView feeText;
    private ImageView posterImage;
    private Button joinButton;
    private Button leaveButton;
    private Button editButton;
    private Button deleteButton;
    private Button viewQrButton;
    private Button testNotificationButton;
    private Button backButton;

    private String eventId;
    private String currentUserId;
    private String userRole;
    private Event currentEvent;
    private WaitingList waitingList;
    private boolean isOnWaitlist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Get event id from intent
        eventId = getIntent().getStringExtra("eventId");

        // Get current user ID and role
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserId = prefs.getString(KEY_PROFILE_ID, "0");
        userRole = prefs.getString(KEY_ROLE, "user");

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
        posterImage = findViewById(R.id.event_poster);
        joinButton = findViewById(R.id.join_button);
        leaveButton = findViewById(R.id.leave_button);
        editButton = findViewById(R.id.edit_button);
        deleteButton = findViewById(R.id.delete_button);
        viewQrButton = findViewById(R.id.view_qr_button);
        testNotificationButton = findViewById(R.id.test_notification_button);
        backButton = findViewById(R.id.back_button);

        // Load event from database
        loadEvent();
        checkWaitlistStatus();

        // Button listeners
        joinButton.setOnClickListener(v -> joinWaitlist());
        leaveButton.setOnClickListener(v -> leaveWaitlist());
        editButton.setOnClickListener(v -> openEditEvent());
        deleteButton.setOnClickListener(v -> confirmDeleteEvent());
        viewQrButton.setOnClickListener(v -> showQRCode());
        testNotificationButton.setOnClickListener(v -> testNotification());
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, EventsListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
        // Check if current user is the organizer or admin
        boolean isOrganizer = currentEvent != null && 
                              String.valueOf(currentEvent.getOrganizerId()).equals(currentUserId);
        boolean isAdmin = "admin".equals(userRole);
        
        // Admins can edit/delete any event, organizers can only edit/delete their own
        boolean canModifyEvent = isAdmin || isOrganizer;
        
        if (canModifyEvent) {
            // Show edit and delete buttons for organizers/admins
            joinButton.setVisibility(Button.GONE);
            leaveButton.setVisibility(Button.GONE);
            editButton.setVisibility(Button.VISIBLE);
            deleteButton.setVisibility(Button.VISIBLE);
            
            Log.d(TAG, "User can modify event - Role: " + userRole + ", IsOrganizer: " + isOrganizer + ", IsAdmin: " + isAdmin);
        } else {
            // Regular users see join/leave buttons based on waitlist status
            editButton.setVisibility(Button.GONE);
            deleteButton.setVisibility(Button.GONE);
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
    
    private void showQRCode() {
        if (currentEvent == null) {
            Toast.makeText(this, "Event data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create QR code data with event ID
        String qrData = "EVENT:" + eventId;
        
        // Generate QR code bitmap
        Bitmap qrBitmap = QRCode.generateQRCodeBitmap(qrData, 800, 800);
        
        if (qrBitmap == null) {
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show QR code in dialog
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_qr_code);
        
        TextView title = dialog.findViewById(R.id.qr_dialog_title);
        TextView subtitle = dialog.findViewById(R.id.qr_dialog_subtitle);
        ImageView qrImageView = dialog.findViewById(R.id.qr_code_image);
        Button closeButton = dialog.findViewById(R.id.qr_dialog_close);
        
        title.setText(currentEvent.getTitle());
        subtitle.setText("Scan this QR code to join the event");
        qrImageView.setImageBitmap(qrBitmap);
        
        closeButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
        Log.d(TAG, "Showing QR code for event: " + eventId);
    }
    
    private void confirmDeleteEvent() {
        if (currentEvent == null) {
            return;
        }
        
        // Show confirmation dialog
        new android.app.AlertDialog.Builder(this)
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete \"" + currentEvent.getTitle() + "\"? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> deleteEvent())
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void deleteEvent() {
        if (currentEvent == null) {
            return;
        }
        
        DatabaseHandler db = DatabaseHandler.getInstance();
        
        // Delete the event from Firebase
        db.deleteEvent(eventId, 
            () -> {
                Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Event " + eventId + " deleted successfully");
                
                // Navigate back to events list
                Intent intent = new Intent(EventDetailsActivity.this, EventsListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            },
            error -> {
                Toast.makeText(this, "Failed to delete event: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to delete event", error);
            }
        );
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
                    
                    // Load poster image if available
                    if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                        try {
                            android.graphics.Bitmap posterBitmap = EventActivity.base64ToBitmap(event.getImageUrl());
                            if (posterBitmap != null) {
                                posterImage.setImageBitmap(posterBitmap);
                                Log.d(TAG, "Poster image loaded successfully");
                            } else {
                                posterImage.setImageResource(R.drawable.sample_image);
                                Log.d(TAG, "Failed to decode poster, using default");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error loading poster image", e);
                            posterImage.setImageResource(R.drawable.sample_image);
                        }
                    } else {
                        posterImage.setImageResource(R.drawable.sample_image);
                        Log.d(TAG, "No poster uploaded for this event");
                    }
                    
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
