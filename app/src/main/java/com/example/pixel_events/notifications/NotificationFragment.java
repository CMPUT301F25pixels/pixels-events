package com.example.pixel_events.notifications;

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
import com.example.pixel_events.events.Event;
import com.example.pixel_events.events.EventInvitation;
import com.example.pixel_events.events.InvitationAdapter;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.waitinglist.WaitingList;
import com.example.pixel_events.waitinglist.WaitlistUser;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NotificationFragment extends Fragment implements InvitationAdapter.OnInvitationInteractionListener {
    private static final String TAG = "NotificationFragment";
    private RecyclerView notificationRecyclerView;
    private Profile currentUser;
    private MaterialButtonToggleGroup toggleGroup;
    private InvitationAdapter invitationAdapter;
    private List<EventInvitation> currentInvitations = new ArrayList<>();

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = AuthManager.getInstance().getCurrentUserProfile();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationRecyclerView = view.findViewById(R.id.notifications_recycler_view);
        toggleGroup = view.findViewById(R.id.notification_selection);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter
        invitationAdapter = new InvitationAdapter(new ArrayList<>(), this);
        notificationRecyclerView.setAdapter(invitationAdapter);

        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to see notifications.", Toast.LENGTH_SHORT).show();
            return;
        }

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.notifications_invitations) {
                    // Ensure the invitations adapter is attached before loading
                    notificationRecyclerView.setAdapter(invitationAdapter);
                    loadUserInvitations();
                } else {
                    // Placeholder for notifications - detach the invitations adapter
                    notificationRecyclerView.setAdapter(null); // Or a different adapter for notifications
                }
            }
        });

        // Set default selection
        toggleGroup.check(R.id.notifications_invitations);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        if (toggleGroup.getCheckedButtonId() == R.id.notifications_invitations) {
            // Reattach adapter in case it was detached
            notificationRecyclerView.setAdapter(invitationAdapter);
            loadUserInvitations();
        }
    }

    private void loadUserInvitations() {
        if (currentUser == null) {
            invitationAdapter.updateInvitations(new ArrayList<>());
            return;
        }

        DatabaseHandler.getInstance().getAllEvents(events -> {
            // Use a local list and set to deduplicate while building results to avoid race conditions
            List<EventInvitation> newInvitations = new ArrayList<>();
            java.util.Set<Integer> seenEventIds = new java.util.HashSet<>();

            if (events.isEmpty()) {
                invitationAdapter.updateInvitations(newInvitations);
                return;
            }

            AtomicInteger eventsProcessed = new AtomicInteger(0);
            for (Event event : events) {
                DatabaseHandler.getInstance().getWaitingList(event.getEventId(), waitingList -> {
                    if (waitingList != null) {
                        List<WaitlistUser> selectedUsers = waitingList.getSelected();
                        if (selectedUsers != null) {
                            for (WaitlistUser user : selectedUsers) {
                                if (user.getUserId() == currentUser.getUserId() && user.getStatus() == 1) { // selected
                                    // Avoid adding the same event twice
                                    if (!seenEventIds.contains(event.getEventId())) {
                                        seenEventIds.add(event.getEventId());
                                        newInvitations.add(new EventInvitation(event, user));
                                    }
                                }
                            }
                        }
                    }
                    if (eventsProcessed.incrementAndGet() == events.size()) {
                        currentInvitations = newInvitations; // replace backing field
                        invitationAdapter.updateInvitations(new ArrayList<>(currentInvitations));
                    }
                }, e -> {
                    Log.e(TAG, "Failed to get waitlist for event " + event.getEventId(), e);
                    if (eventsProcessed.incrementAndGet() == events.size()) {
                        currentInvitations = newInvitations;
                        invitationAdapter.updateInvitations(new ArrayList<>(currentInvitations));
                    }
                });
            }
        }, e -> {
            Log.e(TAG, "Failed to get all events", e);
            Toast.makeText(getContext(), "Error loading events.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onAccept(EventInvitation invitation) {
        // 2 -> accepted in new mapping
        updateInvitationStatus(invitation, 2);
    }

    @Override
    public void onDecline(EventInvitation invitation) {
        // 3 -> declined in new mapping
        updateInvitationStatus(invitation, 3);
        DatabaseHandler.getInstance().getWaitingList(invitation.getEvent().getEventId(), waitingList -> {
            if (waitingList != null) {
                waitingList.drawLottery(new WaitingList.OnLotteryDrawnListener() {
                    @Override
                    public void onSuccess(int numberDrawn) {
                        Log.e(TAG, "Lottery drawn: " + numberDrawn);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to draw lottery", e);
                    }
                });
            }
        }, e -> {
            Log.e(TAG, "Failed to get waitlist for event " + invitation.getEvent().getEventId(), e);
        });
    }

    private void updateInvitationStatus(EventInvitation invitation, int newStatus) {
        invitation.getWaitlistUser().updateStatusInDb(invitation.getEvent().getEventId(), newStatus, new WaitlistUser.OnStatusUpdateListener() {
            @Override
            public void onSuccess() {
                currentInvitations.remove(invitation);
                invitationAdapter.updateInvitations(new ArrayList<>(currentInvitations));
                Toast.makeText(getContext(), "Invitation updated.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to update status", e);
                Toast.makeText(getContext(), "Failed to update invitation.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}