package com.example.pixel_events.waitinglist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog.Builder;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.profile.ViewProfileFragment;
import com.example.pixel_events.utils.SavingData;

import java.util.ArrayList;
import java.util.List;

public class WaitingListFragment extends Fragment {
    private WaitingList waitingList;
    // Filter which statuses to include (e.g., {0} for waiting, {1,0} for
    // selected+waiting, {2} for accepted, {3} for declined)
    private int[] filterStatuses = null;
    // If showing selected + waiting, sort selected first
    private boolean sortSelectedFirst = false;
    private ImageButton backButton, shareButton;
    private int eventId = -1;
    private DatabaseHandler db;

    private RecyclerView recyclerView;
    private WaitingListAdapter adapter;
    private final List<Profile> profiles = new ArrayList<>();

    public WaitingListFragment() {
    }

    public WaitingListFragment(int eventId) {
        this.eventId = eventId;
    }

    public WaitingListFragment(WaitingList waitingList) {
        this.waitingList = waitingList;
        this.eventId = waitingList != null ? waitingList.getEventId() : -1;
    }

    public static WaitingListFragment newInstance(WaitingList wl, int[] statuses, boolean sortSelectedFirst) {
        WaitingListFragment f = new WaitingListFragment(wl);
        f.filterStatuses = statuses;
        f.sortSelectedFirst = sortSelectedFirst;
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waitinglist, container, false);
        db = DatabaseHandler.getInstance();
        backButton = view.findViewById(R.id.waitinglist_backbutton);
        shareButton = view.findViewById(R.id.waitinglist_exportButton);

        recyclerView = view.findViewById(R.id.waitinglist_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new WaitingListAdapter(profiles, waitingList, new WaitingListAdapter.OnItemClick() {
            @Override
            public void onClick(Profile profile) {
                // Open ViewProfileFragment when user taps a profile
                if (isAdded()) {
                    int containerId;
                    if (requireActivity().findViewById(R.id.overlay_fragment_container) != null) {
                        containerId = R.id.overlay_fragment_container;
                    } else if (requireActivity().findViewById(R.id.nav_host_fragment_activity_admin) != null) {
                        containerId = R.id.nav_host_fragment_activity_admin;
                    } else if (requireActivity().findViewById(R.id.nav_host_fragment_activity_dashboard) != null) {
                        containerId = R.id.nav_host_fragment_activity_dashboard;
                    } else {
                        containerId = android.R.id.content;
                    }

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(containerId, new ViewProfileFragment(profile))
                            .addToBackStack(null)
                            .commit();

                    if (containerId == R.id.overlay_fragment_container) {
                        View overlay = requireActivity().findViewById(R.id.overlay_fragment_container);
                        if (overlay != null)
                            overlay.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onDelete(Profile profile) {
                // Confirm remove and mark user as declined (status = 3)
                if (!isAdded())
                    return;
                new Builder(requireContext())
                        .setTitle("Remove from waitlist")
                        .setMessage("Remove this user from the waitlist and mark them as declined?")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            if (waitingList == null || waitingList.getWaitList() == null) {
                                showInfoDialog("Waitlist not loaded");
                                return;
                            }
                            // Find corresponding WaitlistUser
                            WaitlistUser target = null;
                            for (WaitlistUser u : waitingList.getWaitList()) {
                                if (u.getUserId() == profile.getUserId()) {
                                    target = u;
                                    break;
                                }
                            }
                            if (target == null) {
                                showInfoDialog("User not found in waitlist");
                                return;
                            }

                            // Update status to declined (3)
                            target.updateStatusInDb(eventId, 3, new WaitlistUser.OnStatusUpdateListener() {
                                @Override
                                public void onSuccess() {
                                    // Reload waitlist and refresh UI
                                    db.getWaitingList(eventId, wl -> {
                                        if (wl != null) {
                                            waitingList = wl;
                                            // refresh profiles list
                                            requireActivity().runOnUiThread(() -> {
                                                profiles.clear();
                                                adapter.notifyDataSetChanged();
                                                loadProfilesFromWaitingList();
                                                showInfoDialog("User removed and marked as declined.");
                                            });
                                        }
                                    }, e -> {
                                        Log.e("WaitingListFragment", "Failed to reload waitlist after remove", e);
                                        if (isAdded())
                                            requireActivity().runOnUiThread(() -> showInfoDialog(
                                                    "Removed user but failed to reload waitlist."));
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e("WaitingListFragment", "Failed to update user status", e);
                                    if (isAdded())
                                        requireActivity().runOnUiThread(
                                                () -> showInfoDialog("Failed to remove user: " + e.getMessage()));
                                }
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);

        // Load waitlist state if needed
        if (waitingList == null && eventId >= 0) {
            db.getWaitingList(eventId, wlst -> {
                if (wlst != null) {
                    waitingList = wlst;
                    loadProfilesFromWaitingList();
                }
            }, e -> Log.e("WaitingListFragment", "Failed to fetch waitlist", e));
        } else {
            loadProfilesFromWaitingList();
        }

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        shareButton.setOnClickListener(v -> {
            if (waitingList == null || waitingList.getWaitList() == null || waitingList.getWaitList().isEmpty()) {
                showInfoDialog("Nothing to export");
                return;
            }

            // Filter users based on lottery status
            List<WaitlistUser> usersToExport;
            if ("drawn".equals(waitingList.getStatus())) {
                // If lottery drawn, only export accepted people (status == 2)
                usersToExport = new ArrayList<>();
                for (WaitlistUser user : waitingList.getWaitList()) {
                    if (user.getStatus() == 2) {
                        usersToExport.add(user);
                    }
                }
                if (usersToExport.isEmpty()) {
                    showInfoDialog("No accepted participants to export");
                    return;
                }
            } else {
                // If lottery not drawn, export all people in waitlist
                usersToExport = waitingList.getWaitList();
            }

            // Use utility to export
            new SavingData(usersToExport)
                    .exportProfiles(requireContext(), eventId, message -> {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> showInfoDialog(message));
                        }
                    });
        });

        return view;
    }

    private void loadProfilesFromWaitingList() {
        if (waitingList == null) {
            return;
        }

        List<WaitlistUser> ids = waitingList.getWaitList();
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // Clear existing
        profiles.clear();
        adapter.notifyDataSetChanged();

        // Build filtered list of user IDs based on filterStatuses
        java.util.List<WaitlistUser> filtered = new java.util.ArrayList<>();
        if (filterStatuses == null || filterStatuses.length == 0) {
            // Default: if lottery drawn, show selected+waiting; else show waiting only
            if ("drawn".equals(waitingList.getStatus())) {
                filterStatuses = new int[] { 1, 0 };
                sortSelectedFirst = true;
            } else {
                filterStatuses = new int[] { 0 };
            }
        }

        java.util.Set<Integer> allowed = new java.util.HashSet<>();
        for (int s : filterStatuses)
            allowed.add(s);

        for (WaitlistUser user : ids) {
            if (user == null)
                continue;
            if (!allowed.contains(user.getStatus()))
                continue;
            filtered.add(user);
        }

        if (sortSelectedFirst) {
            filtered.sort((a, b) -> Integer.compare(a.getStatus() == 1 ? 0 : 1, b.getStatus() == 1 ? 0 : 1));
        }

        // Fetch profiles sequentially to preserve filtered order
        fetchProfilesSequentially(filtered, 0);
    }

    // Helper that fetches profiles one-by-one so that the adapter list order
    // matches filtered order
    private void fetchProfilesSequentially(java.util.List<WaitlistUser> filtered, int idx) {
        if (idx >= filtered.size())
            return;
        int pid = filtered.get(idx).getUserId();
        db.getProfile(pid, prof -> {
            if (prof != null) {
                profiles.add(prof);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> adapter.notifyItemInserted(profiles.size() - 1));
                }
            }
            // fetch next
            fetchProfilesSequentially(filtered, idx + 1);
        }, e -> {
            Log.e("WaitingListFragment", "Failed to load profile " + pid, e);
            // continue even on error
            fetchProfilesSequentially(filtered, idx + 1);
        });
    }

    private void showInfoDialog(String message) {
        if (!isAdded())
            return;
        new Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
