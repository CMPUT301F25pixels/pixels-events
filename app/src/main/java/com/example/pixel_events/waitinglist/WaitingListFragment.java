package com.example.pixel_events.waitinglist;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.profile.ViewProfileFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying a list of users (Waiting List OR Enrolled List).
 * Allows toggling between a List view and a Map view.
 */
public class WaitingListFragment extends Fragment implements OnMapReadyCallback {

    // Enum to define which list we are showing
    public enum ListType {
        WAITING_LIST,
        ENROLLED_LIST
    }

    private ListType currentListType = ListType.WAITING_LIST; // Default
    private WaitingList waitingList;
    private ImageButton backButton;
    private int eventId = -1;
    private DatabaseHandler db;

    // UI Components
    private RecyclerView recyclerView;
    private FragmentContainerView mapContainer;
    private MaterialButtonToggleGroup toggleGroup;
    private TextView titleTextView; // Optional: to change title based on mode

    private WaitingListAdapter adapter;
    private final List<Profile> profiles = new ArrayList<>();
    private GoogleMap googleMap;

    public WaitingListFragment() {
        // Required empty constructor
    }

    // Constructor for specific Event ID and Type
    public WaitingListFragment(int eventId, ListType type) {
        this.eventId = eventId;
        this.currentListType = type;
    }

    // Backward compatibility constructor (defaults to Waiting List)
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
        View view = inflater.inflate(R.layout.fragment_waitinglist, container, false);
        db = DatabaseHandler.getInstance();

        // Initialize UI
        backButton = view.findViewById(R.id.waitinglist_backbutton);
        toggleGroup = view.findViewById(R.id.toggle_group);
        mapContainer = view.findViewById(R.id.map_container);
        recyclerView = view.findViewById(R.id.waitinglist_recyclerview);

        // Optional: You could find a TextView title here to say "Enrolled" vs "Waiting"

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

        // Setup Toggle Logic (List vs Map)
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_list) {
                    showListView();
                } else if (checkedId == R.id.btn_map) {
                    showMapView();
                }
            }
        });

        // Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_container);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Load Data based on Type
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
        mapContainer.setVisibility(View.GONE);
    }

    private void showMapView() {
        recyclerView.setVisibility(View.GONE);
        mapContainer.setVisibility(View.VISIBLE);
        loadMapMarkers();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        if (mapContainer.getVisibility() == View.VISIBLE) {
            loadMapMarkers();
        }
    }

    private void loadMapMarkers() {
        if (googleMap == null || profiles.isEmpty()) return;

        googleMap.clear();
        Geocoder geocoder = new Geocoder(requireContext());

        for (Profile profile : profiles) {
            String locationQuery = profile.getPostalcode();
            if (locationQuery == null || locationQuery.isEmpty()) {
                // Fallback to city if postal code is missing
                // Assuming getCity() exists or replace with null check
                locationQuery = profile.getCity();
            }

            if (locationQuery == null || locationQuery.isEmpty()) continue;

            try {
                List<Address> addresses = geocoder.getFromLocationName(locationQuery, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    googleMap.addMarker(new MarkerOptions().position(latLng).title(profile.getUserName()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * LOADS WAITING LIST (Existing Logic)
     */
    private void loadWaitingListData() {
        if (waitingList == null && eventId >= 0) {
            db.getWaitingList(eventId, wlst -> {
                if (wlst != null) {
                    waitingList = wlst;
                    List<Integer> ids = waitingList.getWaitList();
                    fetchProfilesFromIds(ids);
                }
            }, e -> Log.e("WaitlistFragment", "Failed to fetch waitlist", e));
        } else if (waitingList != null) {
            fetchProfilesFromIds(waitingList.getWaitList());
        }
    }

    /**
     * LOADS ENROLLED LIST (Now implemented!)
     * Uses the 'selected' list from the WaitingList object.
     */
    private void loadEnrolledListData() {
        if (waitingList == null && eventId >= 0) {
            // 1. Fetch the WaitingList object from DB just like we do for the normal list
            db.getWaitingList(eventId, wlst -> {
                if (wlst != null) {
                    waitingList = wlst;
                    // 2. BUT... pass the 'selected' list instead of 'waitList'
                    List<Integer> selectedIds = waitingList.getSelected();
                    fetchProfilesFromIds(selectedIds);
                }
            }, e -> Log.e("WaitlistFragment", "Failed to fetch enrolled list", e));
        } else if (waitingList != null) {
            // If we already have the object, just load the selected IDs
            fetchProfilesFromIds(waitingList.getSelected());
        }
    }


    /**
     * Shared helper to fetch Profile objects from a list of User IDs
     */
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
                            if (mapContainer.getVisibility() == View.VISIBLE) {
                                loadMapMarkers();
                            }
                        });
                    }
                }
            }, e -> Log.e("WaitlistFragment", "Failed to load profile " + pid, e));
        }
    }
}
