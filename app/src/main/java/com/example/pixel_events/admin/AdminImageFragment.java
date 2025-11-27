package com.example.pixel_events.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AdminImageFragment extends Fragment {
	private AdminImageAdapter adapter;
	private TextView empty;
	private View progress;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_admin_images, container, false);

		RecyclerView recycler = view.findViewById(R.id.admin_images_recycler);
		empty = view.findViewById(R.id.admin_images_empty);
		progress = view.findViewById(R.id.admin_images_progress);

		adapter = new AdminImageAdapter(this::onDeleteImage);

		recycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
		recycler.setAdapter(adapter);

		loadImages();

		return view;
	}

	private void onDeleteImage(Event e) {
		// Notify Organizer
		int orgId = e.getOrganizerId();
		com.example.pixel_events.notifications.Notification notice = new com.example.pixel_events.notifications.Notification(
			"Image Removed",
			"The poster for your event '" + e.getTitle() + "' has been removed by the Admin.",
			"ADMIN_DELETE",
			e.getEventId(),
			orgId
		);
		DatabaseHandler.getInstance().addNotification(orgId, notice);

		java.util.Map<String, Object> updates = new java.util.HashMap<>();
		updates.put("imageUrl", "");
		DatabaseHandler.getInstance().modify(DatabaseHandler.getInstance().getEventCollection(), e.getEventId(), updates, err -> {
			if (err == null) {
				if (isAdded()) requireActivity().runOnUiThread(this::loadImages);
			}
		});
	}

	private void loadImages() {
		if (progress != null) progress.setVisibility(View.VISIBLE);
		DatabaseHandler.getInstance().getAllEvents(list -> {
			if (isAdded()) requireActivity().runOnUiThread(() -> {
				if (progress != null) progress.setVisibility(View.GONE);
				List<Event> withImages = new ArrayList<>();
				if (list != null) {
					for (Event ev : list) {
						if (ev == null) continue;
						String img = ev.getImageUrl();
						if (img != null && !img.trim().isEmpty()) withImages.add(ev);
					}
				}
				if (adapter != null) adapter.setData(withImages);
				if (empty != null) empty.setVisibility(withImages.isEmpty() ? View.VISIBLE : View.GONE);
			} );
		}, e -> {
			if (isAdded()) requireActivity().runOnUiThread(() -> {
				if (progress != null) progress.setVisibility(View.GONE);
				if (empty != null) empty.setVisibility(View.VISIBLE);
			});
		});
	}
}
