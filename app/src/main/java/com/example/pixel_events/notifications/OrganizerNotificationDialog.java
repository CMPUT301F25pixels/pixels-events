package com.example.pixel_events.notifications;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.login.AuthManager;

/**
 * OrganizerNotificationDialog
 *
 * Dialog fragment allowing organizers to send custom notifications to entrants.
 * Provides radio buttons to select recipient group: all waitlist, selected, or cancelled.
 * Allows custom message composition and tracks sender for admin logs.
 *
 * Implements:
 * - US 02.07.01 (Send notifications to all waitlist)
 * - US 02.07.02 (Send notifications to selected entrants)
 * - US 02.07.03 (Send notifications to cancelled entrants)
 *
 * Collaborators:
 * - DatabaseHandler: Sends notifications
 * - WaitingList: Determines recipient list
 * - Notification: Message delivery
 */
public class OrganizerNotificationDialog extends DialogFragment {
    private int eventId;
    private String eventTitle;

    public static OrganizerNotificationDialog newInstance(int eventId, String eventTitle) {
        OrganizerNotificationDialog fragment = new OrganizerNotificationDialog();
        Bundle args = new Bundle();
        args.putInt("eventId", eventId);
        args.putString("eventTitle", eventTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            eventId = getArguments().getInt("eventId");
            eventTitle = getArguments().getString("eventTitle");
        }

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_organizer_notification, null);

        RadioGroup recipientGroup = view.findViewById(R.id.notification_recipient_group);
        EditText messageInput = view.findViewById(R.id.notification_message_input);

        return new AlertDialog.Builder(requireContext())
                .setTitle("Send Notification")
                .setView(view)
                .setPositiveButton("Send", (dialog, which) -> {
                    String message = messageInput.getText().toString().trim();
                    if (message.isEmpty()) {
                        Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int selectedId = recipientGroup.getCheckedRadioButtonId();
                    int senderId = AuthManager.getInstance().getCurrentUserProfile().getUserId();
                    DatabaseHandler db = DatabaseHandler.getInstance();

                    if (selectedId == R.id.radio_all_waitlist) {
                        // US 02.07.01
                        db.sendNotificationToAllWaitlist(eventId, eventTitle, message, senderId);
                        Toast.makeText(getContext(), "Notification sent to all waitlist", Toast.LENGTH_SHORT).show();
                    } else if (selectedId == R.id.radio_selected) {
                        // US 02.07.02
                        db.sendNotificationToSelected(eventId, eventTitle, message, senderId);
                        Toast.makeText(getContext(), "Notification sent to selected entrants", Toast.LENGTH_SHORT).show();
                    } else if (selectedId == R.id.radio_cancelled) {
                        // US 02.07.03
                        db.sendNotificationToCancelled(eventId, eventTitle, message, senderId);
                        Toast.makeText(getContext(), "Notification sent to cancelled entrants", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
    }
}

