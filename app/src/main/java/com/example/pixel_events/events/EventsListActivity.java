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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class EventsListActivity extends AppCompatActivity {
    private static final String TAG = "EventsListActivity";

    private FirebaseFirestore firestore;
    private Button upcomingButton;
    private Button previousButton;
    private RecyclerView eventsRecyclerView;

    private EventsListAdapter adapter;
    private List<EventModel> upcomingEvents;
    private List<EventModel> previousEvents;

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
        firestore.collection("EventData")
                .orderBy("eventStartDate")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    upcomingEvents.clear();
                    previousEvents.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        EventModel event = convertToEventModel(document);
                        if (event != null) {
                            if (isFutureEvent(event)) {
                                upcomingEvents.add(event);
                            } else {
                                previousEvents.add(event);
                            }
                        }
                    }

                    // Sort upcoming (earliest first), previous (latest first)
                    Collections.sort(upcomingEvents, (e1, e2) -> e1.getDate().compareTo(e2.getDate()));
                    Collections.sort(previousEvents, (e1, e2) -> e2.getDate().compareTo(e1.getDate()));

                    Log.d(TAG, "Loaded " + upcomingEvents.size() + " upcoming, " +
                            previousEvents.size() + " previous events");

                    adapter.updateEvents(upcomingEvents);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading events", e);
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    private EventModel convertToEventModel(QueryDocumentSnapshot document) {
        try {
            return new EventModel(
                    Math.toIntExact((Long) document.get("eventId")),
                    Math.toIntExact((Long) document.get("organizerId")),
                    (String) document.get("title"),
                    R.drawable.sample_image,
                    (String) document.get("location"),
                    (String) document.get("capacity"),
                    (String) document.get("description"),
                    (String) document.get("fee"),
                    (String) document.get("eventStartDate"),
                    (String) document.get("eventStartTime"),
                    (String) document.get("organizerName")
            );
        } catch (Exception e) {
            Log.e(TAG, "Error converting document", e);
            return null;
        }
    }


    private void showUpcomingEvents() {
        adapter.updateEvents(upcomingEvents);
    }

    private void showPreviousEvents() {
        adapter.updateEvents(previousEvents);
    }

    private boolean isFutureEvent(EventModel event) {
        return event.getDate().after(new Date());
    }

    private void onEventClick(EventModel event) {
        Toast.makeText(this, "Clicked: " + event.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }
}