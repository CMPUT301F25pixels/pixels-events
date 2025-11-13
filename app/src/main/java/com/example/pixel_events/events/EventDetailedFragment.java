package com.example.pixel_events.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.waitinglist.WaitingList;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FieldValue;

import android.graphics.Bitmap;
import androidx.appcompat.app.AlertDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.pixel_events.utils.ImageConversion;

public class EventDetailedFragment extends Fragment {
    private static final String TAG = "EventDetailedFragment";
    private Event event;
    private WaitingList waitList;
    private DatabaseHandler db;
    private int userId; // String form of current profile id
    private int eventId = -1;
    private boolean joined = false;
    private int waitingListCount = 0;

    // UI elements
    private ShapeableImageView poster;
    private TextView title, date, description;
    private MaterialButton joinLeaveButton;
    private ImageButton backButton, qrButton;

    public EventDetailedFragment() {
    }

    public EventDetailedFragment(int eventId) {
        this.eventId = eventId;
    }

    public EventDetailedFragment(Event event) {
        this.event = event;
        this.eventId = event.getEventId();
    }

    public EventDetailedFragment(Event event, WaitingList waitingList) {
        this.event = event;
        this.waitList = waitingList;
        this.eventId = event.getEventId();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_detailed, container, false);

        db = DatabaseHandler.getInstance();
        if (AuthManager.getInstance().getCurrentUserProfile() != null) {
            userId = AuthManager.getInstance().getCurrentUserProfile().getUserId();
        } else {
            userId = -1;
        }

        // Initialize UI
        poster = view.findViewById(R.id.event_poster);
        title = view.findViewById(R.id.event_title);
        date = view.findViewById(R.id.event_date);
        description = view.findViewById(R.id.event_description);
        joinLeaveButton = view.findViewById(R.id.event_jlbutton);
        backButton = view.findViewById(R.id.event_backbutton);
        qrButton = view.findViewById(R.id.event_qrcode_button);

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        qrButton.setOnClickListener(v -> showQrDialog());

        // While loading waitlist state, show a disabled loading state to avoid wrong
        // initial text
        if (joinLeaveButton != null) {
            joinLeaveButton.setText("Loading…");
            joinLeaveButton.setEnabled(false);
        }

        // Load event data
        if (eventId >= 0) {
            if (event == null) {
                db.getEvent(eventId,
                        evt -> {
                            event = evt;
                            if (isAdded())
                                updateUI();
                        },
                        error -> {
                            Log.e(TAG, "Error getting event: " + error);
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Failed to load event", Toast.LENGTH_SHORT).show();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            }
                        });
            } else {
                updateUI();
            }

            if (waitList == null) {
                // Load waitlist state
                db.getWaitingList(eventId, waitList -> {
                    if (waitList != null) {
                        List<Integer> ids = waitList.getWaitList();
                        if (ids == null) {
                            ids = java.util.Collections.emptyList();
                        }
                        joined = (userId != -1 && ids.contains(userId));
                        waitingListCount = ids.size();
                    } else {
                        joined = false;
                        waitingListCount = 0;
                    }
                    renderCTA();
                }, e -> {
                    Log.e(TAG, "Failed to fetch waitlist", e);
                    joined = false;
                    waitingListCount = 0;
                    renderCTA();
                });
            } else {
                List<Integer> ids = waitList.getWaitList();
                if (ids == null) {
                    ids = java.util.Collections.emptyList();
                }
                joined = (userId != -1 && ids.contains(userId));
                waitingListCount = ids.size();
                renderCTA();
            }

        }

        joinLeaveButton.setOnClickListener(v -> {
            if (userId == -1) {
                toast("You must be logged in to join the waitlist");
                return;
            }

            joinLeaveButton.setEnabled(false);

            if (joined) {
                leaveWaitlist();
            } else {
                joinWaitlist();
            }
        });

        return view;
    }

    private void updateUI() {
        if (!isAdded() || event == null)
            return;

        title.setText(event.getTitle());
        date.setText(event.getDateString());
        description.setText(event.getDescription());

        // Load poster image
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Bitmap bitmap = ImageConversion.base64ToBitmap(event.getImageUrl());
            if (bitmap != null) {
                poster.setImageBitmap(bitmap);
            }
        } else {
        }
    }

    private void renderCTA() {
        if (!isAdded() || joinLeaveButton == null)
            return;
        if (joined) {
            joinLeaveButton.setText("Leave (" + waitingListCount + ")");
        } else {
            joinLeaveButton.setText("Join (" + waitingListCount + ")");
        }
        joinLeaveButton.setEnabled(true);
    }

    private void joinWaitlist() {
        DatabaseHandler.getInstance()
                .joinWaitingList(eventId, userId)
                .addOnSuccessListener(unused -> {
                    joined = true;
                    waitingListCount++;
                    toast("Joined waitlist");
                    renderCTA();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Join failed", e);
                    toast("Error: " + e.getMessage());
                    if (joinLeaveButton != null)
                        joinLeaveButton.setEnabled(true);
                });
    }

    private void leaveWaitlist() {
        DatabaseHandler.getInstance()
                .leaveWaitingList(eventId, userId)
                .addOnSuccessListener(unused -> {
                    joined = false;
                    if (waitingListCount > 0)
                        waitingListCount--;
                    toast("Left waitlist");
                    renderCTA();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Leave failed", e);
                    toast("Error: " + e.getMessage());
                    if (joinLeaveButton != null)
                        joinLeaveButton.setEnabled(true);
                });
    }

    private void toast(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void showQrDialog() {
        if (!isAdded())
            return;
        if (event == null || event.getQrCode() == null || event.getQrCode().isEmpty()) {
            toast("QR code not available");
            return;
        }

        Bitmap bitmap;
        try {
            bitmap = ImageConversion.base64ToBitmap(event.getQrCode());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid QR base64", e);
            toast("Failed to load QR code");
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_qrcode, null, false);
        android.widget.ImageView imageView = dialogView.findViewById(R.id.qr_code_image);
        imageView.setImageBitmap(bitmap);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        android.widget.Button closeBtn = dialogView.findViewById(R.id.qr_dialog_close);
        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> dialog.dismiss());
        } else {
            dialogView.setOnClickListener(v -> dialog.dismiss());
        }
        dialog.show();
    }
}
