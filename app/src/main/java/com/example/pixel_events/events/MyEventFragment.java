package com.example.pixel_events.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.profile.Profile;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;

public class MyEventFragment extends Fragment {
    private static final String TAG = "MyEventFragment";
    private RecyclerView eventsRecyclerView;
    private MyEventAdapter adapter;
    private MaterialButtonToggleGroup toggleGroup;
    private Profile currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_myevents, container, false);

        currentUser = AuthManager.getInstance().getCurrentUserProfile();
        if (currentUser == null) {
            if (AuthManager.getInstance().isUserLoggedIn()) {
                Toast.makeText(getContext(), "Loading profile...", Toast.LENGTH_SHORT).show();
                view.postDelayed(() -> {
                    currentUser = AuthManager.getInstance().getCurrentUserProfile();
                    if (currentUser == null) {
                        Toast.makeText(getContext(), "Please log in", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    initWithProfile(view);
                }, 500);
                return view;
            } else {
                Toast.makeText(getContext(), "Please log in", Toast.LENGTH_SHORT).show();
                return view;
            }
        }

        initWithProfile(view);

        return view;
    }

    private void initWithProfile(View view) {
        eventsRecyclerView = view.findViewById(R.id.myevents_RecyclerView);
        toggleGroup = view.findViewById(R.id.myevents_selection);

        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter
        adapter = new MyEventAdapter(new ArrayList<>(), event -> {
            if (!isAdded())
                return;
            Fragment detail;
            if (Objects.equals(currentUser.getRole(), "user")) {
                detail = new EventDetailedFragment(event.getEventId());
            } else if (Objects.equals(currentUser.getRole(), "org")) {
                detail = new EventFragment(event.getEventId());
            } else {
                return;
            }
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_dashboard, detail)
                    .addToBackStack(null)
                    .commit();
        });

        eventsRecyclerView.setAdapter(adapter);

        // Set default selection
        toggleGroup.check(R.id.myevents_upcoming);

        // Set up toggle listener
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.myevents_upcoming) {
                    loadEvents(true);
                } else if (checkedId == R.id.myevents_previous) {
                    loadEvents(false);
                }
            }
        });
        loadEvents(true);
    }

    private void loadEvents(boolean isUpcoming) {
        if (currentUser == null)
            return;

        String role = currentUser.getRole();

        if ("org".equalsIgnoreCase(role)) {
            // For organizers, show their created upcoming events
            loadOrganizerEvents(isUpcoming);
        } else {
            // For users, show events they're signed up for (in any waitlist)
            loadUserEvents(isUpcoming);
        }
    }

    private void loadOrganizerEvents(boolean isUpcoming) {
        DatabaseHandler db = DatabaseHandler.getInstance();
        int organizerId = currentUser.getUserId();

        // Get all events and filter by organizer ID and date
        db.getAllEvents(
                allEvents -> {
                    List<Event> filteredEvents = new ArrayList<>();
                    Date today = getTodayDate();
                    
                    for (Event event : allEvents) {
                        if (event.getOrganizerId() == organizerId) {
                            Date eventEndDate = parseDate(event.getEventEndDate());
                            if (eventEndDate != null) {
                                if (isUpcoming && !eventEndDate.before(today)) {
                                    filteredEvents.add(event);
                                } else if (!isUpcoming && eventEndDate.before(today)) {
                                    filteredEvents.add(event);
                                }
                            }
                        }
                    }
                    adapter.updateEvents(filteredEvents);
                    Log.d(TAG, "Loaded " + filteredEvents.size() + " " + (isUpcoming ? "upcoming" : "previous") + " events for organizer");
                },
                e -> {
                    Log.e(TAG, "Error loading organizer events", e);
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserEvents(boolean isUpcoming) {
        DatabaseHandler db = DatabaseHandler.getInstance();
        int userId = currentUser.getUserId();

        // Get all events user is part of (from waitlists) and filter by date
        db.getEventsForUser(userId,
                userEvents -> {
                    List<Event> filteredEvents = new ArrayList<>();
                    Date today = getTodayDate();
                    
                    for (Event event : userEvents) {
                        Date eventEndDate = parseDate(event.getEventEndDate());
                        if (eventEndDate != null) {
                            if (isUpcoming && !eventEndDate.before(today)) {
                                filteredEvents.add(event);
                            } else if (!isUpcoming && eventEndDate.before(today)) {
                                filteredEvents.add(event);
                            }
                        }
                    }
                    adapter.updateEvents(filteredEvents);
                    Log.d(TAG, "Loaded " + filteredEvents.size() + " " + (isUpcoming ? "upcoming" : "previous") + " events for user");
                },
                e -> {
                    Log.e(TAG, "Error loading user events", e);
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    private Date getTodayDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.parse(dateStr);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date: " + dateStr, e);
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void replaceFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment_activity_dashboard, fragment)
                .addToBackStack(null)
                .commit();
    }
}
