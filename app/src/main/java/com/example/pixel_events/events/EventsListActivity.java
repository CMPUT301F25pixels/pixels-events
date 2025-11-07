package com.example.pixel_events.events;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixel_events.MainActivity;
import com.example.pixel_events.R;
import com.example.pixel_events.qr.QRScannerActivity;
import com.example.pixel_events.settings.ProfileActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventsListActivity extends AppCompatActivity {
    private static final String TAG = "EventsListActivity";

    private FirebaseFirestore firestore;
    private Button upcomingButton;
    private Button previousButton;
    private RecyclerView eventsRecyclerView;

    private EventsListAdapter adapter;
    private List<Event> upcomingEvents;
    private List<Event> previousEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_list);

        firestore = FirebaseFirestore.getInstance();

        initViews();
        setupTabButtons();
        setupRecyclerView();
        loadEvents();
    }

    private void initViews() {
        upcomingButton = findViewById(R.id.upcomingButton);
        previousButton = findViewById(R.id.previousButton);
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        setupBottomNav();
    }
    
    private void setupBottomNav() {
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navEvents = findViewById(R.id.nav_events);
        LinearLayout navScanner = findViewById(R.id.nav_scanner);
        LinearLayout navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(EventsListActivity.this, MainActivity.class);
            startActivity(intent);
        });

        navEvents.setOnClickListener(v -> {
            Toast.makeText(this, "Already on Events", Toast.LENGTH_SHORT).show();
        });

        navScanner.setOnClickListener(v -> {
            Intent intent = new Intent(EventsListActivity.this, QRScannerActivity.class);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(EventsListActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setupTabButtons() {
        upcomingButton.setOnClickListener(v -> {
            showUpcomingEvents();
            updateTabButtonStyles(true);
        });

        previousButton.setOnClickListener(v -> {
            showPreviousEvents();
            updateTabButtonStyles(false);
        });

        // Set initial state
        updateTabButtonStyles(true);
    }

    private void updateTabButtonStyles(boolean isUpcomingSelected) {
        if (isUpcomingSelected) {
            upcomingButton.setTextColor(ContextCompat.getColor(this, R.color.accent_blue_light ));
            previousButton.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        } else {
            upcomingButton.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            previousButton.setTextColor(ContextCompat.getColor(this, R.color.accent_blue_light));
        }
    }

    private void setupRecyclerView() {
        upcomingEvents = new ArrayList<>();
        previousEvents = new ArrayList<>();

        adapter = new EventsListAdapter(upcomingEvents, this::onEventClick);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsRecyclerView.setAdapter(adapter);
    }

    private void loadEvents() {
        Log.d(TAG, "Starting to load events from Firestore...");
        
        firestore.collection("EventData")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Firestore query successful. Total documents: " + queryDocumentSnapshots.size());
                    
                    upcomingEvents.clear();
                    previousEvents.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d(TAG, "Processing document: " + document.getId());
                        Log.d(TAG, "Document data: " + document.getData());
                        
                        try {
                            Event event = document.toObject(Event.class);
                            if (event != null && event.getTitle() != null) {
                                Log.d(TAG, "Converted event: " + event.getTitle() + ", Date: " + event.getEventStartDate());
                                if (isFutureEvent(event)) {
                                    upcomingEvents.add(event);
                                    Log.d(TAG, "Added to upcoming events");
                                } else {
                                    previousEvents.add(event);
                                    Log.d(TAG, "Added to previous events");
                                }
                            } else {
                                Log.e(TAG, "Event is null or has no title");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error converting document to Event: " + e.getMessage(), e);
                        }
                    }

                    // Sort by event start date
                    try {
                        Collections.sort(upcomingEvents, (e1, e2) -> {
                            if (e1.getEventStartDate() == null) return 1;
                            if (e2.getEventStartDate() == null) return -1;
                            return e1.getEventStartDate().compareTo(e2.getEventStartDate());
                        });
                        Collections.sort(previousEvents, (e1, e2) -> {
                            if (e1.getEventStartDate() == null) return 1;
                            if (e2.getEventStartDate() == null) return -1;
                            return e2.getEventStartDate().compareTo(e1.getEventStartDate());
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error sorting events: " + e.getMessage(), e);
                    }

                    Log.d(TAG, "Loaded " + upcomingEvents.size() + " upcoming, " +
                            previousEvents.size() + " previous events");

                    // Update adapter based on current tab
                    runOnUiThread(() -> {
                        adapter.updateEvents(upcomingEvents);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Loaded " + (upcomingEvents.size() + previousEvents.size()) + " events", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading events", e);
                    Toast.makeText(this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showUpcomingEvents() {
        adapter.updateEvents(upcomingEvents);
    }

    private void showPreviousEvents() {
        adapter.updateEvents(previousEvents);
    }

    private boolean isFutureEvent(Event event) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date eventDate = sdf.parse(event.getEventStartDate());
            return eventDate != null && eventDate.after(new Date());
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date", e);
            return true; // Default to upcoming if parsing fails
        }
    }

    private void onEventClick(Event event) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("eventId", String.valueOf(event.getEventId()));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }
}