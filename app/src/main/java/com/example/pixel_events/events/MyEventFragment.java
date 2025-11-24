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
import com.example.pixel_events.home.DashboardAdapter;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.profile.Profile;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyEventFragment extends Fragment {
    private static final String TAG = "MyEventFragment";
    private RecyclerView eventsRecyclerView;
    private DashboardAdapter adapter;
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
                AuthManager.getInstance().refreshCurrentUserProfile();
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
        adapter = new DashboardAdapter(new ArrayList<>(), event -> {
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
                    loadUpcomingEvents();
                } else if (checkedId == R.id.myevents_previous) {
                    loadPreviousEvents();
                }
            }
        });
        loadUpcomingEvents();
    }

    private void loadUpcomingEvents() {
        if (currentUser == null)
            return;

        String role = currentUser.getRole();

        if ("org".equalsIgnoreCase(role)) {
            // For organizers, show their created upcoming events
            loadOrganizerEvents(true);
        } else {
            // For users, show events they're signed up for
            List<Integer> upcomingEventIds = currentUser.getEventsUpcoming();
            if (upcomingEventIds == null || upcomingEventIds.isEmpty()) {
                adapter.updateEvents(new ArrayList<>());
                return;
            }
            loadEventsByIds(upcomingEventIds);
        }
    }

    private void loadPreviousEvents() {
        if (currentUser == null)
            return;

        String role = currentUser.getRole();

        if ("org".equalsIgnoreCase(role)) {
            // For organizers, show their created past events
            loadOrganizerEvents(false);
        } else {
            // For users, show past events they participated in
            List<Integer> previousEventIds = currentUser.getEventsPart();
            if (previousEventIds == null || previousEventIds.isEmpty()) {
                adapter.updateEvents(new ArrayList<>());
                return;
            }
            loadEventsByIds(previousEventIds);
        }
    }

    private void loadOrganizerEvents(boolean isUpcoming) {
        DatabaseHandler db = DatabaseHandler.getInstance();
        int organizerId = currentUser.getUserId();

        // Get all events and filter by organizer ID
        db.getAllEvents(
                allEvents -> {
                    List<Event> filteredEvents = new ArrayList<>();
                    for (Event event : allEvents) {
                        if (event.getOrganizerId() == organizerId) {
                            // TODO: Add proper date comparison to determine if event is upcoming or past
                            // For now, show all organizer events
                            filteredEvents.add(event);
                        }
                    }
                    adapter.updateEvents(filteredEvents);
                    Log.d(TAG, "Loaded " + filteredEvents.size() + " events for organizer");
                },
                e -> {
                    Log.e(TAG, "Error loading organizer events", e);
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadEventsByIds(List<Integer> eventIds) {
        DatabaseHandler db = DatabaseHandler.getInstance();

        // Get all events and filter by IDs
        db.getAllEvents(
                allEvents -> {
                    List<Event> filteredEvents = new ArrayList<>();
                    for (Event event : allEvents) {
                        if (eventIds.contains(event.getEventId())) {
                            filteredEvents.add(event);
                        }
                    }
                    adapter.updateEvents(filteredEvents);
                    Log.d(TAG, "Loaded " + filteredEvents.size() + " events from user's list");
                },
                e -> {
                    Log.e(TAG, "Error loading events by IDs", e);
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                });
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
