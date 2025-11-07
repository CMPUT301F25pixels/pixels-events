package com.example.pixel_events.entrant;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.se.omapi.Session;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.login.SessionManager;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class EventDetailsFragment extends Fragment {
    private String TAG = "EventDetailsFragment";
    public static final String ARG_EVENT_ID = "eventId";

    private String eventId;
    private DocumentReference eventRef;

    private ImageView poster;
    private TextView title, dateView, description, wlCount, status;
    private Button cta;

    private boolean joined = false;
    private Long waitingListCap = null;
    private long waitingListCount = 0L;
    private String startTime;
    private String endTime;
    private TextView infoHeader, aboutHeader, aboutBody, lotteryHeader, lotteryStep1, lotteryStep2, lotteryStep3;
    public static final String ARG_EVENT_IDS = "eventId";
    private static final String HARDCODED_EVENT_ID = "1762485069";

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_details_fragment, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        eventId = getArguments() != null
                ? getArguments().getString(ARG_EVENT_ID, HARDCODED_EVENT_ID)
                : HARDCODED_EVENT_ID;
        String userId = "56789";  // or whatever user key you're using

        DatabaseHandler db = DatabaseHandler.getInstance();

        poster = v.findViewById(R.id.poster);
        title = v.findViewById(R.id.title);
        dateView = v.findViewById(R.id.date);
        description = v.findViewById(R.id.description);
        cta = v.findViewById(R.id.cta);
        infoHeader = v.findViewById(R.id.info_header);
        status = v.findViewById(R.id.status);
        aboutHeader = v.findViewById(R.id.about_header);
        aboutBody = v.findViewById(R.id.about_body);
        lotteryHeader = v.findViewById(R.id.lottery_header);
        lotteryStep1 = v.findViewById(R.id.lottery_step1);
        lotteryStep2 = v.findViewById(R.id.lottery_step2);
        lotteryStep3 = v.findViewById(R.id.lottery_step3);
        if (TextUtils.isEmpty(eventId)) {
            toast("Missing event id"); requireActivity().finish(); return;
        }
        db.getEvent(eventId, event -> {
            if (event == null) { toast("Event not found"); requireActivity().finish(); return; }
            bindEvent(event);
        }, e -> { toast(e.getMessage()); requireActivity().finish();
        });

        // Get waiting list info
        db.getWaitingList(eventId, waitList -> {
            if (waitList != null) {
                Log.d(TAG, "Loaded waitlist for event: " + waitList.getEventId());

                List<String> ids = waitList.getWaitList();
                if (ids == null) {
                    Log.d(TAG, "Waitlist list is null; treating as empty.");
                    ids = java.util.Collections.emptyList();
                }

                if (userId == null) {
                    Log.w(TAG, "userId is null; cannot determine joined state.");
                    joined = false;
                } else {
                    joined = ids.contains(userId);
                }

                Log.d(TAG, "Waitlist size=" + ids.size() + ", joined=" + joined);
                // If you want, update UI now
                renderCTA();
            } else {
                Log.w(TAG, "waitList is null for this event.");
                joined = false;
                renderCTA();
            }
        }, e -> Log.e("WAITLIST", "Failed to fetch waitlist", e));


        cta.setOnClickListener(view -> {
            Log.d(TAG, "User ID: " + userId);

            if (joined) {
                db.leaveWaitingList(eventId, userId)
                    .addOnSuccessListener(u -> {
                        joined = false;
                        if (waitingListCount > 0) waitingListCount--;
                        renderCTA();
                    })
                    .addOnFailureListener(e -> toast("Error: " + e.getMessage()))
                    .addOnCompleteListener(t -> cta.setEnabled(true));
            } else {
                db.joinWaitingList(eventId, userId)
                    .addOnSuccessListener(u -> {
                        joined = true;
                        waitingListCount++;
                        renderCTA();
                    })
                    .addOnFailureListener(e -> toast("Error: " + e.getMessage()))
                    .addOnCompleteListener(t -> cta.setEnabled(true));
            }
        });
    }


    private void bindEvent(Event event) {
        String name = event.getTitle();
        String desc = event.getDescription();
        String posterUrl = event.getImageUrl();
        String startDate = event.getEventStartDate();
        String endDate = event.getEventEndDate();
        String startTime = event.getEventStartTime();
        String endTime = event.getEventEndTime();

        String dateText = startDate + " " + startTime + " - " + endDate + " " + endTime;
        dateView.setText(dateText);
        title.setText(name);
        aboutBody.setText(desc);
        description.setText(desc);
        ((TextView) requireView().findViewById(R.id.about_body)).setText(desc);

        if (!TextUtils.isEmpty(posterUrl)) Glide.with(this).load(posterUrl).into(poster);
        else poster.setImageDrawable(null);
    }

    private void renderCTA() {
        cta.setText(joined ? "Leave" : "Join");
        status.setText(joined ? "You’re on the waiting list" : "");
        cta.setEnabled(true);
    }

    private void toast(String msg) {
        if (getContext() != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public static EventDetailsFragment newInstance(String eventId) {
        EventDetailsFragment f = new EventDetailsFragment();
        Bundle b = new Bundle();
        b.putString(ARG_EVENT_ID, eventId);
        f.setArguments(b);
        return f;
    }
}


