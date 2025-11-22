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
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_admin_images, container, false);

		RecyclerView recycler = view.findViewById(R.id.admin_images_recycler);
		TextView empty = view.findViewById(R.id.admin_images_empty);
		View progress = view.findViewById(R.id.admin_images_progress);

		AdminImageAdapter adapter = new AdminImageAdapter(e -> {
			DatabaseHandler.getInstance().deleteEvent(e.getEventId());
		});
		adapter.setData(filterEventsExcept(null));

		recycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
		recycler.setAdapter(adapter);

		// load events
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
				adapter.setData(withImages);
				empty.setVisibility(withImages.isEmpty() ? View.VISIBLE : View.GONE);
			} );
		}, e -> {
			if (isAdded()) requireActivity().runOnUiThread(() -> {
				if (progress != null) progress.setVisibility(View.GONE);
				empty.setVisibility(View.VISIBLE);
			});
		});

		return view;
	}

	// helper used by adapter delete callback; when passing null returns current events (no-op)
	private List<Event> filterEventsExcept(Event except) {
		// This fragment does not maintain the list as a field; simply reload events from DB and filter out 'except' if provided.
		List<Event> result = new ArrayList<>();
		try {
			CountDownLatch latch = new CountDownLatch(1);
			DatabaseHandler.getInstance().getAllEvents(list -> {
				if (list != null) {
					for (Event ev : list) {
						if (ev == null) continue;
						String img = ev.getImageUrl();
						if (img != null && !img.trim().isEmpty()) {
							if (except == null || ev.getEventId() != except.getEventId()) result.add(ev);
						}
					}
				}
				latch.countDown();
			}, err -> latch.countDown());
			// wait briefly for callback
			try { latch.await(500, TimeUnit.MILLISECONDS); } catch (InterruptedException ignored) {}
		} catch (Exception ignored) {}
		return result;
	}
}
