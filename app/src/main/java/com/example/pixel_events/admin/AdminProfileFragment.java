package com.example.pixel_events.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.profile.Profile;
import java.util.ArrayList;
import java.util.List;

public class AdminProfileFragment extends Fragment {
	private androidx.recyclerview.widget.RecyclerView recyclerView;
	private ProfileAdapter adapter;
	private final List<Profile> entrants = new ArrayList<>();
	private final List<Profile> organizers = new ArrayList<>();
	private com.google.android.material.button.MaterialButtonToggleGroup toggleGroup;
	private com.google.android.material.button.MaterialButton btnEntrants, btnOrganizers;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_admin_profiles, container, false);
		recyclerView = view.findViewById(R.id.myevents_RecyclerView);
		toggleGroup = view.findViewById(R.id.admin_profile_selection);
		btnEntrants = view.findViewById(R.id.admin_profiles_entrants);
		btnOrganizers = view.findViewById(R.id.admin_profiles_organizers);

		if (recyclerView != null) {
			recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
			adapter = new ProfileAdapter(new java.util.ArrayList<>(), new ProfileAdapter.Listener() {
				@Override public void onClick(Profile p) { openProfile(p); }
				@Override public void onDelete(Profile p) { deleteProfile(p, entrants.contains(p)); }
			});
			recyclerView.setAdapter(adapter);
		}

		// Toggle behavior: show entrants by default
		if (toggleGroup != null) {
			toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
				if (!isChecked) return; // only handle checked events
				if (checkedId == R.id.admin_profiles_entrants) {
					// show entrants
					if (isAdded()) requireActivity().runOnUiThread(() -> adapter.updateData(entrants));
				} else if (checkedId == R.id.admin_profiles_organizers) {
					if (isAdded()) requireActivity().runOnUiThread(() -> adapter.updateData(organizers));
				}
			});
		}

		loadProfiles();
		return view;
	}

	private void loadProfiles() {
		try {
			DatabaseHandler.getInstance().getAllProfile(list -> {
				entrants.clear(); organizers.clear();
				if (list != null) {
					for (Profile p : list) {
						if (p == null) continue;
						String role = p.getRole();
						if (role == null) role = "";
						role = role.trim();
						if ("admin".equalsIgnoreCase(role)) continue; // ignore admins
						if ("org".equalsIgnoreCase(role) || "organizer".equalsIgnoreCase(role)) organizers.add(p);
						else entrants.add(p);
					}
				}
				if (isAdded()) requireActivity().runOnUiThread(() -> {
					// Default selection: entrants
					if (toggleGroup != null) {
						// set checked button to entrants if none selected
						if (toggleGroup.getCheckedButtonId() == View.NO_ID) {
							toggleGroup.check(R.id.admin_profiles_entrants);
						}
					}
					adapter.updateData(entrants);
				});
			}, e -> Log.e("AdminProfileFragment", "Failed to load profiles", e));
		} catch (Exception e) {
			Log.e("AdminProfileFragment", "listAllProfiles not implemented", e);
		}
	}

	private void openProfile(Profile p) {
		View overlay = requireActivity().findViewById(R.id.overlay_fragment_container);
		if (overlay != null) overlay.setVisibility(View.VISIBLE);
		requireActivity().getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.overlay_fragment_container, new com.example.pixel_events.profile.ViewProfileFragment(p, false))
				.addToBackStack(null)
				.commit();
	}

	private void deleteProfile(Profile p, boolean entrant) {
		DatabaseHandler db = DatabaseHandler.getInstance();
		int userId = p.getUserId();
		db.deleteAcc(userId);
		(entrant ? entrants : organizers).remove(p);

		// Cleanup: Remove user from all waiting lists and delete events if organizer
		db.getAllEvents(events -> {
			if (events != null) {
				for (Event e : events) {
					if (e == null) continue;
					// If user is the organizer, delete the event
					if (e.getOrganizerId() == userId) {
						db.deleteEvent(e.getEventId());
					} else {
						// Otherwise, remove user from the waiting list AND selected list of this event
						db.getWaitListCollection().document(String.valueOf(e.getEventId()))
								.update("waitList", com.google.firebase.firestore.FieldValue.arrayRemove(userId),
										"selected", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
								.addOnFailureListener(err -> Log.e("AdminProfileFragment", "Failed to remove user from waitlist " + e.getEventId(), err));
					}
				}
			}
		}, err -> Log.e("AdminProfileFragment", "Failed to cleanup events for user " + userId, err));

		if (isAdded()) requireActivity().runOnUiThread(() -> {
			int checked = (toggleGroup != null) ? toggleGroup.getCheckedButtonId() : R.id.admin_profiles_entrants;
			if (checked == R.id.admin_profiles_organizers) adapter.updateData(organizers); else adapter.updateData(entrants);
		});
	}

	// Simple adapter
	static class ProfileAdapter extends RecyclerView.Adapter<ProfileViewHolder> {
		interface Listener { void onClick(Profile p); void onDelete(Profile p); }
		private final List<Profile> data; private final Listener listener;
		ProfileAdapter(List<Profile> data, Listener l){ this.data = data; this.listener = l; }
		@NonNull @Override public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int vt){
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
			return new ProfileViewHolder(v);
		}
		@Override public void onBindViewHolder(@NonNull ProfileViewHolder h,int pos){
			Profile p = data.get(pos);
			h.name.setText(p.getUserName());
			h.email.setText(p.getEmail());
			h.delete.setVisibility(View.VISIBLE);

			h.itemView.setOnClickListener(v -> listener.onClick(p));
			h.delete.setOnClickListener(v -> listener.onDelete(p));
		}
		@Override public int getItemCount(){ return data.size(); }

		public void updateData(List<Profile> newData) {
			this.data.clear();
			if (newData != null) this.data.addAll(newData);
			notifyDataSetChanged();
		}
	}
	static class ProfileViewHolder extends RecyclerView.ViewHolder {
		TextView name; TextView email; View delete;
		ProfileViewHolder(@NonNull View itemView){
			super(itemView);
			name = itemView.findViewById(R.id.item_profile_title);
			email = itemView.findViewById(R.id.item_profile_email);
			delete = itemView.findViewById(R.id.item_profile_delete);
		}
	}
}
