package com.example.pixel_events.waitinglist;

import android.media.Image;
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
        adapter = new WaitingListAdapter(profiles, profile -> {
            // Open ViewProfileFragment when user taps a profile
            if (isAdded()) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_dashboard, new ViewProfileFragment(profile))
                        .addToBackStack(null)
                        .commit();
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

        backButton.setOnClickListener(v ->
            requireActivity().getSupportFragmentManager().popBackStack()
        );

        shareButton.setOnClickListener(v -> {
            if (waitingList == null || waitingList.getWaitList() == null || waitingList.getWaitList().isEmpty()) {
                showInfoDialog("Nothing to export");
                return;
            }
            // Use utility to export
            new SavingData(waitingList.getWaitList())
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
            // Nothing to show
            return;
        }

        List<Integer> ids = waitingList.getWaitList();
        if (ids == null || ids.isEmpty()) {
            // nothing to load
            return;
        }

        // Clear existing
        profiles.clear();
        adapter.notifyDataSetChanged();

        for (Integer pid : ids) {
            if (pid == null)
                continue;
            db.getProfile(pid,
                    prof -> {
                        if (prof != null) {
                            profiles.add(prof);
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> adapter.notifyItemInserted(profiles.size() - 1));
                            }
                        }
                    }, e -> Log.e("WaitingListFragment", "Failed to load profile " + pid, e));
        }
    }

    private void showInfoDialog(String message) {
        if (!isAdded()) return;
        new Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
