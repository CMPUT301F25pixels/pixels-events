package com.example.pixel_events.waitinglist;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.profile.ViewProfileFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;

// OSM Imports
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WaitingListFragment extends Fragment {

    public enum ListType {
        WAITING_LIST,
        ENROLLED_LIST
    }

    private ListType currentListType = ListType.WAITING_LIST;
    private WaitingList waitingList;
    private ImageButton backButton;
    private int eventId = -1;
    private DatabaseHandler db;

    // UI Components
    private RecyclerView recyclerView;
    private MapView map; // Changed from GoogleMap/Fragment to OSM MapView
    private MaterialButtonToggleGroup toggleGroup;

    private WaitingListAdapter adapter;
    private final List<Profile> profiles = new ArrayList<>();

    public WaitingListFragment() {
        // Required empty constructor
    }

    public WaitingListFragment(int eventId, ListType type) {
        this.eventId = eventId;
        this.currentListType = type;
    }

    public WaitingListFragment(int eventId) {
        this(eventId, ListType.WAITING_LIST);
    }

    public WaitingListFragment(WaitingList waitingList) {
        this.waitingList = waitingList;
        this.eventId = waitingList != null ? waitingList.getEventId() : -1;
        this.currentListType = ListType.WAITING_LIST;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 1. Initialize OSM Configuration (IMPORTANT)
        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        View view = inflater.inflate(R.layout.fragment_waitinglist, container, false);
        db = DatabaseHandler.getInstance();

        backButton = view.findViewById(R.id.waitinglist_backbutton);
        toggleGroup = view.findViewById(R.id.toggle_group);
        recyclerView = view.findViewById(R.id.waitinglist_recyclerview);

        // 2. Setup Map View
        map = view.findViewById(R.id.osm_map);
        map.setTileSource(TileSourceFactory.MAPNIK); // Standard map style
        map.setMultiTouchControls(true); // Pinch to zoom
        map.getController().setZoom(4.0); // Default Zoom

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new WaitingListAdapter(profiles, profile -> {
            if (isAdded()) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_dashboard, new ViewProfileFragment(profile))
                        .addToBackStack(null)
                        .commit();
            }
        });
        recyclerView.setAdapter(adapter);

        // Toggle Logic
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_list) {
                    showListView();
                } else if (checkedId == R.id.btn_map) {
                    showMapView();
                }
            }
        });

        // Load Data
        if (currentListType == ListType.WAITING_LIST) {
            loadWaitingListData();
        } else {
            loadEnrolledListData();
        }

        backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        return view;
    }

    private void showListView() {
        recyclerView.setVisibility(View.VISIBLE);
        map.setVisibility(View.GONE);
    }

    private void showMapView() {
        recyclerView.setVisibility(View.GONE);
        map.setVisibility(View.VISIBLE);
        loadMapMarkers();
    }

    // Lifecycle methods required for OSM to manage resources/battery
    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }

    private void loadMapMarkers() {
        if (map == null || profiles.isEmpty()) return;

        map.getOverlays().clear();
        Geocoder geocoder = new Geocoder(requireContext());
        GeoPoint lastPoint = new GeoPoint(53.5461, -113.4938);

        for (Profile profile : profiles) {
            GeoPoint point = null;

            // 1. NEW: Check if precise location exists
            if (profile.getLatitude() != null && profile.getLongitude() != null) {
                point = new GeoPoint(profile.getLatitude(), profile.getLongitude());
            }
            // 2. OLD: Fallback to Geocoding text (Postal/City)
            else {
                String locationQuery = profile.getPostalcode();
                if (locationQuery == null || locationQuery.isEmpty()) locationQuery = profile.getCity();

                if (locationQuery != null && !locationQuery.isEmpty()) {
                    try {
                        List<Address> addresses = geocoder.getFromLocationName(locationQuery, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            point = new GeoPoint(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                        }
                    } catch (IOException e) { e.printStackTrace(); }
                }
            }

            // 3. If we found a point (either way), add marker
            if (point != null) {
                lastPoint = point;
                Marker marker = new Marker(map);
                marker.setPosition(point);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle(profile.getUserName());
                marker.setSnippet(profile.getCity());
                map.getOverlays().add(marker);
            }
        }

        map.invalidate();
        map.getController().setCenter(lastPoint);
    }

    private void loadWaitingListData() {
        if (waitingList == null && eventId >= 0) {
            db.getWaitingList(eventId, wlst -> {
                if (wlst != null) {
                    waitingList = wlst;
                    fetchProfilesFromIds(waitingList.getWaitList());
                }
            }, e -> Log.e("WaitlistFragment", "Failed to fetch waitlist", e));
        } else if (waitingList != null) {
            fetchProfilesFromIds(waitingList.getWaitList());
        }
    }

    private void loadEnrolledListData() {
        if (waitingList == null && eventId >= 0) {
            db.getWaitingList(eventId, wlst -> {
                if (wlst != null) {
                    waitingList = wlst;
                    fetchProfilesFromIds(waitingList.getSelected());
                }
            }, e -> Log.e("WaitlistFragment", "Failed to fetch enrolled list", e));
        } else if (waitingList != null) {
            fetchProfilesFromIds(waitingList.getSelected());
        }
    }

    private void fetchProfilesFromIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;

        profiles.clear();
        adapter.notifyDataSetChanged();

        for (Integer pid : ids) {
            if (pid == null) continue;
            db.getProfile(pid, prof -> {
                if (prof != null) {
                    profiles.add(prof);
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            adapter.notifyItemInserted(profiles.size() - 1);
                            if (map.getVisibility() == View.VISIBLE) {
                                loadMapMarkers();
                            }
                        });
                    }
                }
            }, e -> Log.e("WaitlistFragment", "Failed to load profile " + pid, e));
        }
    }
}
