package com.example.pixel_events.waitinglist;

import android.location.Address;
import android.location.Geocoder;
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
import com.google.android.material.button.MaterialButtonToggleGroup;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * WaitingListFragment
 *
 * Fragment displaying and managing entrants for a specific event.
 * Supports filtering by status (waiting, selected, accepted, declined).
 * Allows organizers to export lists, send notifications, and remove entrants.
 * Displays entrant profiles with status indicators.
 *
 * Implements:
 * - US 02.02.01 (View list of entrants)
 * - US 02.06.01 (View chosen entrants)
 * - US 02.06.02 (View cancelled entrants)
 * - US 02.06.03 (View enrolled entrants)
 * - US 02.06.04 (Cancel entrants)
 * - US 02.06.05 (Export CSV)
 * - US 02.07.01, 02.07.02, 02.07.03 (Send notifications)
 *
 * Collaborators:
 * - WaitingList: Source data for entrants
 * - WaitlistUser: Individual entrant status
 * - Profile: Entrant information display
 * - SavingData: CSV export functionality
 * - OrganizerNotificationDialog: Send custom messages
 */
public class WaitingListFragment extends Fragment {
    private WaitingList waitingList;
    // Filter which statuses to include (e.g., {0} for waiting, {1,0} for
    // selected+waiting, {2} for accepted, {3} for declined)
    private int[] filterStatuses = null;
    // If showing selected + waiting, sort selected first
    private boolean sortSelectedFirst = false;
    private ImageButton backButton, shareButton, notificationButton;
    private int eventId = -1;
    private DatabaseHandler db;

    private RecyclerView recyclerView;
    private MapView map; // OSM MapView
    private MaterialButtonToggleGroup toggleGroup;

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
        notificationButton = view.findViewById(R.id.waitinglist_notificationButton);
        toggleGroup = view.findViewById(R.id.waitinglist_togglelayout);

        recyclerView = view.findViewById(R.id.waitinglist_recyclerview);
        map = view.findViewById(R.id.waitinglist_map);

        // Configure OSMDroid if present
        if (map != null) {
            try {
                Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
                map.setTileSource(TileSourceFactory.MAPNIK);
                map.setMultiTouchControls(true);
                map.setVisibility(View.GONE); // default to list view
                // Default view: center on Edmonton at a city-level zoom
                map.getController().setCenter(new GeoPoint(53.5461, -113.4938));
                map.getController().setZoom(11.0);
            } catch (Exception e) {
                Log.e("WaitingListFragment", "Failed to configure MapView", e);
            }
        }
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

        notificationButton.setOnClickListener(v -> {
            if (waitingList == null) {
                showInfoDialog("Waiting list not loaded");
                return;
            }
            // Get event title
            db.getEvent(eventId, event -> {
                if (event != null && isAdded()) {
                    com.example.pixel_events.notifications.OrganizerNotificationDialog dialog = com.example.pixel_events.notifications.OrganizerNotificationDialog
                            .newInstance(eventId, event.getTitle());
                    dialog.show(getParentFragmentManager(), "organizer_notification");
                }
            }, e -> showInfoDialog("Failed to load event"));
        });

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_list) {
                    showListView();
                } else if (checkedId == R.id.btn_map) {
                    showMapView();
                }
            }
        });

        return view;
    }

    private void showListView() {
        recyclerView.setVisibility(View.VISIBLE);
        if (map != null)
            map.setVisibility(View.GONE);
    }

    private void showMapView() {
        recyclerView.setVisibility(View.GONE);
        if (map != null) {
            map.setVisibility(View.VISIBLE);
            loadMapMarkers();
        } else {
            Log.w("WaitingListFragment", "MapView is null; cannot show map");
            showInfoDialog("Map view not available on this device");
            // revert to list selection to avoid stuck state
            if (toggleGroup != null)
                toggleGroup.check(R.id.btn_list);
        }
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
        List<WaitlistUser> filtered = new ArrayList<>();
        if (filterStatuses == null || filterStatuses.length == 0) {
            // Default: if lottery drawn, show selected+waiting; else show waiting only
            if ("drawn".equals(waitingList.getStatus())) {
                filterStatuses = new int[] { 1, 0 };
                sortSelectedFirst = true;
            } else {
                filterStatuses = new int[] { 0 };
            }
        }

        Set<Integer> allowed = new HashSet<>();
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
            filtered.sort(Comparator.comparingInt(a -> a.getStatus() == 1 ? 0 : 1));
        }

        // Fetch profiles sequentially to preserve filtered order
        loadProfilesSorted(filtered);
    }

    private void loadProfilesSorted(List<WaitlistUser> filtered) {
        db.getAllProfile(allProfiles -> {
            // Map <id, Profile>
            Map<Integer, Profile> map = new HashMap<>();
            for (Profile p : allProfiles) {
                map.put(p.getUserId(), p);
            }

            profiles.clear();

            // Sort by filtered list order
            for (WaitlistUser w : filtered) {
                Profile p = map.get(w.getUserId());
                if (p != null) {
                    profiles.add(p);
                }
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
            }

        }, e -> {
            Log.e("WaitingListFragment", "Failed to load profiles", e);
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

    private void loadMapMarkers() {
        if (map == null)
            return;

        map.getOverlays().clear();
        Geocoder geocoder = new Geocoder(requireContext());
        GeoPoint lastPoint = new GeoPoint(53.5461, -113.4938);
        boolean anyPointFound = false;

        for (Profile profile : profiles) {
            GeoPoint point = null;

            // 1. NEW: Check if precise location exists
            if (profile.getLatitude() != null && profile.getLongitude() != null) {
                point = new GeoPoint(profile.getLatitude(), profile.getLongitude());
            }
            // 2. OLD: Fallback to Geocoding text (Postal/City)
            else {
                String locationQuery = profile.getPostalcode();
                if (locationQuery == null || locationQuery.isEmpty())
                    locationQuery = profile.getCity();

                if (locationQuery != null && !locationQuery.isEmpty()) {
                    try {
                        List<Address> addresses = geocoder.getFromLocationName(locationQuery, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            point = new GeoPoint(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // 3. If we found a point (either way), add marker
            if (point != null) {
                lastPoint = point;
                anyPointFound = true;
                Marker marker = new Marker(map);
                marker.setPosition(point);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle(profile.getUserName());
                marker.setSnippet(profile.getCity());
                map.getOverlays().add(marker);
            }
        }

        map.invalidate();
        // Zoom closer if we have markers; otherwise keep a city-level default
        map.getController().setCenter(lastPoint);
        if (anyPointFound) {
            map.getController().setZoom(12.0);
        } else {
            map.getController().setZoom(11.0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null)
            map.onResume();
    }

    @Override
    public void onPause() {
        if (map != null)
            map.onPause();
        super.onPause();
    }

}
