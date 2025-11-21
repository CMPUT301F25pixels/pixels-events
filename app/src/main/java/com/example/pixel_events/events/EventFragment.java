package com.example.pixel_events.events;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.utils.ImageConversion;
import com.example.pixel_events.waitinglist.WaitingList;
import com.example.pixel_events.waitinglist.WaitingListFragment;

public class EventFragment extends Fragment {
    private Event event;
    private WaitingList waitList;
    private int eventId = -1;
    private DatabaseHandler db;
    private Button previewButton, notificationPreferencesButton, viewWaitlistButton,
            deleteEventButton;
    private TextView eventTextView;
    private ImageView eventImageView;
    private ImageButton backButton;

    public EventFragment(int eventId) {
        this.eventId = eventId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        db = DatabaseHandler.getInstance();
        if (eventId >= 0) {
            db.getEvent(eventId,
                    evt -> {
                        event = evt;
                        // If the fragment's view is already created, populate the UI
                        if (isAdded() && getView() != null) {
                            requireActivity().runOnUiThread(this::populateEventUI);
                        }
                    },
                    error -> {
                        Log.e("Event Fragment", "Error getting event: " + error);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load event", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        }
                    });

            // Load waitlist state
            db.getWaitingList(eventId, wlst -> {
                if (wlst != null) {
                    waitList = wlst;
                    if (isAdded() && getView() != null) {
                        requireActivity().runOnUiThread(this::populateEventUI);
                    }
                }
            }, e -> {
                Log.e("Event Fragment", "Failed to fetch waitlist", e);
            });
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        previewButton = view.findViewById(R.id.event_fragment_preview);
        notificationPreferencesButton = view.findViewById(R.id.event_fragment_notificationspreferences);
        viewWaitlistButton = view.findViewById(R.id.event_fragment_waitinglist);
        deleteEventButton = view.findViewById(R.id.event_fragment_deleteevent);
        eventTextView = view.findViewById(R.id.event_fragment_title);
        eventImageView = view.findViewById(R.id.event_fragment_poster);
        backButton = view.findViewById(R.id.event_fragment_backbutton);

        setButtonsEnabled(false);

        // Populate UI if event already loaded
        populateEventUI();

        previewButton.setOnClickListener(v -> {
            if (event != null)
                replaceFragment(new EventDetailedFragment(event, waitList));
        });

        viewWaitlistButton.setOnClickListener(v -> {
            if (waitList != null)
                replaceFragment(new WaitingListFragment(waitList));
        });

        notificationPreferencesButton.setOnClickListener(v -> {
            replaceFragment(new EventNotificationFragment());
        });

        deleteEventButton.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete event")
                    .setMessage("Are you sure you want to delete this event?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (event != null)
                            DatabaseHandler.getInstance().deleteEvent(event.getEventId());
                        requireActivity().getSupportFragmentManager().popBackStack();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    // Populate the UI fields from the loaded event (safe to call repeatedly)
    private void populateEventUI() {
        if (event == null || eventTextView == null || eventImageView == null)
            return;

        eventTextView.setText(event.getTitle());
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Bitmap bitmap = ImageConversion.base64ToBitmap(event.getImageUrl());
            if (bitmap != null) {
                eventImageView.setImageBitmap(bitmap);
            } else {
                eventImageView.setImageResource(R.drawable.sample_image);
            }
        } else {
            eventImageView.setImageResource(R.drawable.sample_image);
        }

        // Enable buttons now that we have an event
        setButtonsEnabled(true);
    }

    private void setButtonsEnabled(boolean enabled) {
        if (previewButton != null)
            previewButton.setEnabled(enabled);
        if (notificationPreferencesButton != null)
            notificationPreferencesButton.setEnabled(enabled);
        if (viewWaitlistButton != null)
            viewWaitlistButton.setEnabled(enabled);
        if (deleteEventButton != null)
            deleteEventButton.setEnabled(enabled);
    }

    private void replaceFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment_activity_dashboard, fragment)
                .addToBackStack(null)
                .commit();
    }
}
