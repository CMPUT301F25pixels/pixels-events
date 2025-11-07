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
        setupBottomNav();
        loadEvents();
    }

    private void initViews() {
        upcomingButton = findViewById(R.id.upcomingButton);
        previousButton = findViewById(R.id.previousButton);
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
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
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        eventsRecyclerView.setLayoutManager(layoutManager);
        eventsRecyclerView.setAdapter(adapter);
        eventsRecyclerView.setHasFixedSize(true);
        
        Log.d(TAG, "RecyclerView setup complete with adapter: " + adapter);
        Log.d(TAG, "RecyclerView visibility: " + eventsRecyclerView.getVisibility());
        Log.d(TAG, "RecyclerView height: " + eventsRecyclerView.getHeight());
    }

    private void loadEvents() {
        Log.d(TAG, "Loading events from EventData collection...");
        
        firestore.collection("EventData")
                .orderBy("eventStartDate")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Query successful. Documents: " + queryDocumentSnapshots.size());
                    
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

                    // Prefer upcoming; if none, show previous by default
                    if (!upcomingEvents.isEmpty()) {
                        adapter.updateEvents(upcomingEvents);
                        updateTabButtonStyles(true);
                    } else if (!previousEvents.isEmpty()) {
                        adapter.updateEvents(previousEvents);
                        updateTabButtonStyles(false);
                    } else {
                        adapter.updateEvents(new ArrayList<>());
                    }
                    Toast.makeText(this, "Loaded " + (upcomingEvents.size() + previousEvents.size()) + " events", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading events", e);
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    private EventModel convertToEventModel(QueryDocumentSnapshot document) {
        try {
            // Defensive conversions with logging
            Object eventIdRaw = document.get("eventId");
            Object organizerIdRaw = document.get("organizerId");
            int eventId = parseIntSafely(eventIdRaw, -1);
            int organizerId = parseIntSafely(organizerIdRaw, -1);

            String title = document.getString("title");
            String location = document.getString("location");
            String capacity = toStringSafely(document.get("capacity"));
            String description = document.getString("description");
            String fee = toStringSafely(document.get("fee"));
            String date = document.getString("eventStartDate");
            String time = document.getString("eventStartTime");
            String organizerName = document.contains("organizerName") ?
                    toStringSafely(document.get("organizerName")) : "Organizer";

            Log.d(TAG, "Doc -> id=" + eventId + ", orgId=" + organizerId + ", title=" + title +
                    ", date=" + date + ", time=" + time + ", location=" + location);

            if (title == null || date == null || time == null) {
                Log.w(TAG, "Skipping doc due to missing required fields: " + document.getId());
                return null;
            }

            return new EventModel(
                    eventId,
                    organizerId,
                    title,
                    R.drawable.sample_image,
                    location != null ? location : "",
                    capacity,
                    description != null ? description : "",
                    fee,
                    date,
                    time,
                    organizerName
            );
        } catch (Exception e) {
            Log.e(TAG, "Error converting document", e);
            return null;
        }
    }

    private String toStringSafely(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int parseIntSafely(Object value, int fallback) {
        if (value == null) return fallback;
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            Log.w(TAG, "Failed to parse int from value: " + value);
            return fallback;
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