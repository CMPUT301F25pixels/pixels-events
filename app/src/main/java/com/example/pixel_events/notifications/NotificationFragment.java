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

/**
 * NotificationFragment
 *
 * Fragment combining notification display and invitation management for
 * entrants.
 * Toggles between system notifications (admin alerts, lottery results) and
 * event invitations.
 * Allows entrants to accept or decline lottery invitations.
 * Automatically updates invitation status in database.
 *
 * Implements:
 * - US 01.04.01 (Receive win notifications)
 * - US 01.04.02 (Receive loss notifications)
 * - US 01.05.02 (Accept invitation)
 * - US 01.05.03 (Decline invitation)
 * - Notification history viewing
 *
 * Collaborators:
 * - Notification: Displays system alerts
 * - EventInvitation: Displays lottery invitations
 * - WaitlistUser: Updates acceptance status
 * - DatabaseHandler: Data operations
 */
public class NotificationFragment extends Fragment implements InvitationAdapter.OnInvitationInteractionListener {
    private static final String TAG = "NotificationFragment";
    private RecyclerView notificationRecyclerView;
    private Profile currentUser;
    private MaterialButtonToggleGroup toggleGroup;
    private InvitationAdapter invitationAdapter;
    private NotificationAdapter notificationAdapter;
    private List<EventInvitation> currentInvitations = new ArrayList<>();
    private List<Notification> systemNotifications = new ArrayList<>();

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

        // Initialize both adapters
        invitationAdapter = new InvitationAdapter(new ArrayList<>(), this);
        notificationAdapter = new NotificationAdapter();

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
                } else if (checkedId == R.id.notifications_notification) {
                    // Show system notifications (my feature)
                    notificationRecyclerView.setAdapter(notificationAdapter);
                    loadSystemNotifications();
                }
            }
        });

        // Set default selection to invitations
        toggleGroup.check(R.id.notifications_invitations);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Reattach adapter based on which button is checked
        int checkedId = toggleGroup.getCheckedButtonId();

        if (checkedId == R.id.notifications_invitations) {
            notificationRecyclerView.setAdapter(invitationAdapter);
            loadUserInvitations();
        } else if (checkedId == R.id.notifications_notification) {
            // If you have a separate adapter for notifications, attach it here
            // notificationRecyclerView.setAdapter(notificationAdapter);
            loadSystemNotifications();
        }
    }

    /**
     * Load event invitations (Krupal's feature - lottery accept/decline)
     */
    private void loadUserInvitations() {
        if (currentUser == null) {
            invitationAdapter.updateInvitations(new ArrayList<>());
            return;
        }

        DatabaseHandler.getInstance().getAllEvents(events -> {
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

    /**
     * Load system notifications (my feature - admin alerts, lottery results, etc.)
     */
    private void loadSystemNotifications() {
        if (currentUser == null)
            return;

        DatabaseHandler.getInstance().listenToNotifications(currentUser.getUserId(), (snapshots, error) -> {
            if (error != null) {
                Log.e(TAG, "Error listening to notifications", error);
                return;
            }
            if (snapshots != null) {
                systemNotifications.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                    Notification n = doc.toObject(Notification.class);
                    if (n != null)
                        systemNotifications.add(n);
                }
            }
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
                        Log.d(TAG, "Lottery drawn: " + numberDrawn);
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
        invitation.getWaitlistUser().updateStatusInDb(invitation.getEvent().getEventId(), newStatus,
                new WaitlistUser.OnStatusUpdateListener() {
                    @Override
                    public void onSuccess() {
                        // Ensure UI updates happen on main thread and refresh from source to avoid
                        // stale references
                        if (getActivity() == null)
                            return;
                        getActivity().runOnUiThread(() -> {
                            // Rebuild list to guarantee consistency in case objects mutated or references
                            // changed
                            loadUserInvitations();
                            Toast.makeText(getContext(), "Invitation updated.", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to update status", e);
                        Toast.makeText(getContext(), "Failed to update invitation.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
