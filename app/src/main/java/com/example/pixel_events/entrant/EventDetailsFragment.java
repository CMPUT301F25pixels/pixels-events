package com.example.pixel_events.entrant;


import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Locale;



public class EventDetailsFragment extends Fragment {

    public static final String ARG_EVENT_ID = "eventId";

    private FirebaseAuth auth;
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

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_details_fragment, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

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
        eventId = requireArguments().getString(ARG_EVENT_ID, null);
        if (TextUtils.isEmpty(eventId)) {
            toast("Missing event id"); requireActivity().finish(); return;
        }
        db.getEvent(eventId, event -> {
            if (event == null) { toast("Event not found"); requireActivity().finish(); return; }
            bindEvent(event);
        }, e -> { toast(e.getMessage()); requireActivity().finish();
        });

        // Get waiting list info}

        // Live updates

        // Initial joined state


        cta.setOnClickListener(view -> {
            cta.setEnabled(false);
            String userId = FirebaseAuth.getInstance().getUid();  // or whatever user key you're using

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


        renderCTA();
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


