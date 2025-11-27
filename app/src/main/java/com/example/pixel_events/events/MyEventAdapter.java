package com.example.pixel_events.events;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.utils.ImageConversion;
import com.example.pixel_events.waitinglist.WaitlistUser;

import java.util.ArrayList;
import java.util.List;

public class MyEventAdapter extends RecyclerView.Adapter<MyEventAdapter.EventViewHolder> {
    private List<Event> events;
    private OnEventClickListener listener;
    private static final DatabaseHandler db = DatabaseHandler.getInstance();

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public MyEventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events != null ? events : new ArrayList<>();
        this.listener = listener;
    }

    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents != null ? newEvents : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item_small, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private ImageView eventImage;
        private TextView eventTitle, eventTime, host, status;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.event_small_image);
            eventTitle = itemView.findViewById(R.id.event_small_title);
            eventTime = itemView.findViewById(R.id.event_small_time);
            host = itemView.findViewById(R.id.event_small_host);
            status = itemView.findViewById(R.id.event_small_status);
        }

        public void bind(Event event, OnEventClickListener listener) {
            eventTitle.setText(event.getTitle());
            eventTime.setText(event.getEventStartTime());
            db.getProfile(event.getOrganizerId(), profile -> {
                    host.setText(profile.getUserName());
                }, e -> {
                    host.setText("Organizer");
                });

            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                eventImage.setImageBitmap(ImageConversion.base64ToBitmap(event.getImageUrl()));
            } else {
                eventImage.setImageResource(R.drawable.sample_image);
            }

            Profile currentUser = AuthManager.getInstance().getCurrentUserProfile();
            if (currentUser != null) {
                db.getWaitingList(event.getEventId(), waitingList -> {
                    if (waitingList != null) {
                        checkAndSetStatus(waitingList.getWaitList(), currentUser.getUserId(), event.getEventId());
                    }
                }, e -> {});
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });
        }

        private void checkAndSetStatus(List<WaitlistUser> list, int userId, int eventId) {
            if (list != null) {
                boolean found = false;
                for (WaitlistUser user : list) {
                    if (user.getUserId() == userId) {
                        int s = user.getStatus();
                        if (s == 0) status.setText("Waiting");
                        else if (s == 1) status.setText("Selected");
                        else if (s == 2) status.setText("Accepted");
                        else if (s == 3) status.setText("Declined");
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    status.setText("Not participating");
                }
            }
        }
    }
}
