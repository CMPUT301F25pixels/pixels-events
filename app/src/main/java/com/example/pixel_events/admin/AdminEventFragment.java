package com.example.pixel_events.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminEventFragment
 *
 * Fragment for administrators to browse and delete events.
 * Displays all events in the system with delete functionality.
 * Triggers admin deletion notifications to organizers and entrants.
 *
 * Implements:
 * - US 03.01.01 (Remove events)
 * - US 03.04.01 (Browse events)
 *
 * Collaborators:
 * - Event: Displayed event data
 * - DatabaseHandler: Delete operations
 * - Notification: Deletion alerts
 */
public class AdminEventFragment extends Fragment {
	private RecyclerView recyclerView;
	private AdminAdapter adapter;
	private final List<Event> events = new ArrayList<>();

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
		recyclerView = view.findViewById(R.id.admin_events_recycler);
		if (recyclerView != null) {
			recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
			adapter = new AdminAdapter(events, new AdminAdapter.OnEventClickListener() {
				@Override
				public void onEventClick(Event event) {
					// Open detail overlay
					View overlay = requireActivity().findViewById(R.id.overlay_fragment_container);
					if (overlay != null) overlay.setVisibility(View.VISIBLE);
					requireActivity().getSupportFragmentManager()
							.beginTransaction()
							.add(R.id.overlay_fragment_container, new com.example.pixel_events.events.EventDetailedFragment(event))
							.addToBackStack(null)
							.commit();
				}

				@Override
				public void onDeleteClick(Event event) {
					DatabaseHandler.getInstance().deleteEvent(event.getEventId());
					events.remove(event);
					adapter.notifyDataSetChanged();
				}
			});
			recyclerView.setAdapter(adapter);
			loadEvents();
		}
		return view;
	}

	private void loadEvents() {
		try {
			DatabaseHandler.getInstance().getAllEvents(list -> {
				events.clear();
				if (list != null) events.addAll(list);
				if (isAdded()) requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
			}, e -> Log.e("AdminEventFragment", "Failed to load events", e));
		} catch (Exception e) {
			Log.e("AdminEventFragment", "listAllEvents not implemented", e);
		}
	}
}
