package com.example.pixel_events.home;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.events.EventDetailedFragment;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DashboardFragment extends Fragment {
    private static final String TAG = "DashboardFragment";
    private RecyclerView eventsRecyclerView;
    private DashboardAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();

    // Filters UI
    private ChipGroup chipGroup;
    private TextView startDateView;
    private TextView endDateView;
    private Button applyBtn;
    private Button clearBtn;

    private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private Date filterStartDate = null;
    private Date filterEndDate = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        eventsRecyclerView = view.findViewById(R.id.dashboard_eventRecyclerView);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter with empty list. Use overlay container so NavHost remains
        // intact.
        adapter = new DashboardAdapter(new ArrayList<>(), event -> {
            if (!isAdded())
                return;
            Fragment detail = new EventDetailedFragment(event.getEventId());
            View overlay = requireActivity().findViewById(R.id.overlay_fragment_container);
            if (overlay != null && overlay.getVisibility() != View.VISIBLE) {
                overlay.setVisibility(View.VISIBLE);
            }
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.overlay_fragment_container, detail)
                    .addToBackStack("overlay")
                    .commit();
        });
        eventsRecyclerView.setAdapter(adapter);

        chipGroup = view.findViewById(R.id.dashboard_chip_group);
        startDateView = view.findViewById(R.id.dashboard_start_date);
        endDateView = view.findViewById(R.id.dashboard_end_date);
        applyBtn = view.findViewById(R.id.dashboard_apply_filters);
        clearBtn = view.findViewById(R.id.dashboard_clear_filters);

        startDateView.setOnClickListener(v -> pickDate(true));
        endDateView.setOnClickListener(v -> pickDate(false));
        applyBtn.setOnClickListener(v -> applyFilters());
        clearBtn.setOnClickListener(v -> clearFilters());

        return view;
    }

    private void loadAllEvents() {
        DatabaseHandler db = DatabaseHandler.getInstance();
        db.getAllEvents(
                events -> {
                    Log.d(TAG, "Loaded " + events.size() + " events from Firebase");
                    allEvents = events != null ? events : new ArrayList<>();
                    // Filter events to only current events: end date >= today
                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);
                    List<Event> filtered = new ArrayList<>();
                    for (Event e : allEvents) {
                        Date end = parseDateSafe(e.getEventEndDate());
                        if (end != null && !end.before(today.getTime())) {
                            filtered.add(e);
                        }
                    }
                    adapter.updateEvents(filtered);
                    if (filtered.size() == 0) {
                        Toast.makeText(getContext(), "No events found", Toast.LENGTH_SHORT).show();
                    }
                },
                e -> {
                    Log.e(TAG, "Error loading events", e);
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadAllEvents();
    }

    private void pickDate(boolean isStart) {
        final Calendar cal = Calendar.getInstance();
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (DatePicker dp, int year, int month, int day) -> {
                    Calendar chosen = Calendar.getInstance();
                    chosen.set(year, month, day, 0, 0, 0);
                    String str = dateFmt.format(chosen.getTime());
                    if (isStart) {
                        filterStartDate = chosen.getTime();
                        startDateView.setText(str);
                    } else {
                        filterEndDate = chosen.getTime();
                        endDateView.setText(str);
                    }
                }, y, m, d);
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void applyFilters() {
        Set<String> selectedTags = new HashSet<>();
        if (chipGroup != null) {
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                View child = chipGroup.getChildAt(i);
                if (child instanceof Chip) {
                    Chip c = (Chip) child;
                    if (c.isChecked()) {
                        selectedTags.add(c.getText().toString().trim().toLowerCase(Locale.US));
                    }
                }
            }
        }

        List<Event> filtered = new ArrayList<>();
        for (Event e : allEvents) {
            if (!matchesTags(e, selectedTags))
                continue;
            if (!matchesDateRange(e))
                continue;
            filtered.add(e);
        }
        adapter.updateEvents(filtered);
    }

    private boolean matchesTags(Event e, Set<String> selectedTags) {
        if (selectedTags.isEmpty())
            return true; // no tag filter
        List<String> tags = e.getTags();
        if (tags == null || tags.isEmpty())
            return false;
        for (String t : tags) {
            if (t == null)
                continue;
            String lt = t.trim().toLowerCase(Locale.US);
            if (selectedTags.contains(lt))
                return true;
        }
        return false;
    }

    private boolean matchesDateRange(Event e) {
        if (filterStartDate == null && filterEndDate == null)
            return true;
        Date start = parseDateSafe(e.getEventStartDate());
        Date end = parseDateSafe(e.getEventEndDate());
        if (start == null && end == null)
            return true;
        // Use start date if available, else end date
        Date pivot = start != null ? start : end;

        if (filterStartDate != null && pivot.before(filterStartDate))
            return false;
        if (filterEndDate != null && pivot.after(filterEndDate))
            return false;
        return true;
    }

    private Date parseDateSafe(String s) {
        if (s == null || s.trim().isEmpty())
            return null;
        try {
            return dateFmt.parse(s.trim());
        } catch (ParseException ex) {
            Log.w(TAG, "Unparseable date: " + s);
            return null;
        }
    }

    private void clearFilters() {
        // Clear chips
        if (chipGroup != null) {
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                View child = chipGroup.getChildAt(i);
                if (child instanceof Chip) {
                    ((Chip) child).setChecked(false);
                }
            }
        }
        // Clear dates
        filterStartDate = null;
        filterEndDate = null;
        if (startDateView != null)
            startDateView.setText("Start date");
        if (endDateView != null)
            endDateView.setText("End date");
        // Reset list
        adapter.updateEvents(allEvents);
    }
}
