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
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.waitinglist.WaitingList;
import com.example.pixel_events.waitinglist.WaitlistUser;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NotificationFragment combines two features:
 * 1. System Notifications (admin alerts, deletions, lottery results)
 * 2. Event Invitations (accept/decline lottery wins)
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
                    // Show invitations (Krupal's feature)
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
        // Refresh data when fragment becomes visible
        int checkedId = toggleGroup.getCheckedButtonId();
        if (checkedId == R.id.notifications_invitations) {
            loadUserInvitations();
        } else if (checkedId == R.id.notifications_notification) {
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
            currentInvitations.clear();
            if (events.isEmpty()) {
                invitationAdapter.updateInvitations(new ArrayList<>());
                return;
            }

            AtomicInteger eventsProcessed = new AtomicInteger(0);
            for (Event event : events) {
                DatabaseHandler.getInstance().getWaitingList(event.getEventId(), waitingList -> {
                    if (waitingList != null && waitingList.getSelected() != null) {
                        for (WaitlistUser user : waitingList.getSelected()) {
                            if (user.getUserId() == currentUser.getUserId() && user.getStatus() == 0) {
                                currentInvitations.add(new EventInvitation(event, user));
                            }
                        }
                    }
                    if (eventsProcessed.incrementAndGet() == events.size()) {
                        invitationAdapter.updateInvitations(currentInvitations);
                    }
                }, e -> {
                    Log.e(TAG, "Failed to get waitlist for event " + event.getEventId(), e);
                    if (eventsProcessed.incrementAndGet() == events.size()) {
                        invitationAdapter.updateInvitations(currentInvitations);
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
        if (currentUser == null) return;

        DatabaseHandler.getInstance().listenToNotifications(currentUser.getUserId(), (snapshots, error) -> {
            if (error != null) {
                Log.e(TAG, "Error listening to notifications", error);
                return;
            }
            if (snapshots != null) {
                systemNotifications.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                    Notification n = doc.toObject(Notification.class);
                    if (n != null) systemNotifications.add(n);
                }
                
                if (notificationAdapter != null) {
                    notificationAdapter.setNotifications(systemNotifications);
                }
            }
        });
    }

    @Override
    public void onAccept(EventInvitation invitation) {
        updateUserStatus(invitation, 1);
    }

    @Override
    public void onDecline(EventInvitation invitation) {
        updateUserStatus(invitation, 2);
    }

    private void updateUserStatus(EventInvitation invitation, int newStatus) {
        Event event = invitation.getEvent();
        WaitlistUser userToUpdate = invitation.getWaitlistUser();

        DatabaseHandler.getInstance().getWaitingList(event.getEventId(), waitingList -> {
            if (waitingList != null && waitingList.getSelected() != null) {
                for (WaitlistUser u : waitingList.getSelected()) {
                    if (u.getUserId() == userToUpdate.getUserId()) {
                        u.setStatus(newStatus);
                        break;
                    }
                }
                updateUserStatusInDb(event.getEventId(), waitingList.getSelected());
                // Remove from local list and update adapter
                currentInvitations.remove(invitation);
                invitationAdapter.updateInvitations(new ArrayList<>(currentInvitations));
                Toast.makeText(getContext(), "Invitation updated.", Toast.LENGTH_SHORT).show();
            }
        }, e -> Log.e(TAG, "Failed to get waitlist for status update", e));
    }

    private void updateUserStatusInDb(int eventId, List<WaitlistUser> updatedSelectedList) {
        DatabaseHandler.getInstance().getWaitListCollection()
                .document(String.valueOf(eventId))
                .update("selected", updatedSelectedList)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Waitlist status updated for user in event " + eventId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update waitlist status", e));
    }
}
