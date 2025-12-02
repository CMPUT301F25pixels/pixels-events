package com.example.pixel_events.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.events.EventDetailedFragment;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.waitinglist.WaitlistUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RegistrationHistoryFragment
 *
 * Fragment displaying an entrant's event registration history.
 * Shows all events where user joined waiting lists with selection outcomes.
 * Indicates lottery status (selected, not selected, accepted, declined).
 *
 * Implements:
 * - US 01.02.03 (View registration history and lottery outcomes)
 *
 * Collaborators:
 * - Event: Historical event data
 * - WaitingList: Lottery status information
 * - WaitlistUser: User status in each event
 * - DatabaseHandler: Fetches event and waitlist data
 */
public class RegistrationHistoryFragment extends Fragment  {
    private static final String TAG = "RegistrationHistory";
    private ImageButton backButton;
    private RecyclerView recyclerView;
    private RegistrationHistoryAdapter adapter;
    private List<EventHistoryItem> historyItems = new ArrayList<>();
    private Profile currentUser;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registration_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        backButton = view.findViewById(R.id.registration_backButton);
        recyclerView = view.findViewById(R.id.registration_history_recyclerview);
        
        currentUser = AuthManager.getInstance().getCurrentUserProfile();
        
        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RegistrationHistoryAdapter(historyItems, item -> {
            // Open event details when clicked
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_dashboard, new EventDetailedFragment(item.event))
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setAdapter(adapter);

        loadRegistrationHistory();
    }

    private void loadRegistrationHistory() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to view history", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseHandler db = DatabaseHandler.getInstance();
        
        // Get all events
        db.getAllEvents(events -> {
            if (events.isEmpty()) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "No events found", Toast.LENGTH_SHORT).show();
                    });
                }
                return;
            }

            historyItems.clear();
            AtomicInteger remaining = new AtomicInteger(events.size());

            for (Event event : events) {
                // Check if user is in this event's waitlist
                db.getWaitingList(event.getEventId(), waitingList -> {
                    if (waitingList != null && waitingList.getWaitList() != null) {
                        for (WaitlistUser user : waitingList.getWaitList()) {
                            if (user.getUserId() == currentUser.getUserId()) {
                                // User participated in this event
                                EventHistoryItem item = new EventHistoryItem();
                                item.event = event;
                                item.status = user.getStatus();
                                historyItems.add(item);
                                break;
                            }
                        }
                    }
                    
                    if (remaining.decrementAndGet() == 0) {
                        // All events processed
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();
                                if (historyItems.isEmpty()) {
                                    Toast.makeText(getContext(), "No registration history found", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }, e -> {
                    Log.e(TAG, "Failed to get waitlist for event " + event.getEventId(), e);
                    if (remaining.decrementAndGet() == 0) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                        }
                    }
                });
            }
        }, e -> {
            Log.e(TAG, "Failed to get all events", e);
            Toast.makeText(getContext(), "Error loading events", Toast.LENGTH_SHORT).show();
        });
    }

    // Helper class to hold event + status
    static class EventHistoryItem {
        Event event;
        int status; // 0-waiting, 1-selected, 2-accepted, 3-declined
    }
}
