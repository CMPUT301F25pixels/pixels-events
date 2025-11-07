package com.example.pixel_events.events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixel_events.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventsListAdapter extends RecyclerView.Adapter<EventsListAdapter.EventListViewHolder> {

    private final List<EventModel> events;
    private final OnEventClickListener clickListener;

    public interface OnEventClickListener {
        void onEventClick(EventModel event);
    }

    public EventsListAdapter(List<EventModel> events, OnEventClickListener clickListener) {
        this.events = events;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public EventListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_list_item, parent, false);
        return new EventListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventListViewHolder holder, int position) {
        EventModel event = events.get(position);
        android.util.Log.d("EventsListAdapter", "Binding event at position " + position + ": " + event.getTitle());
        holder.bind(event, clickListener);
    }

    @Override
    public int getItemCount() {
        int count = events.size();
        android.util.Log.d("EventsListAdapter", "getItemCount: " + count);
        return count;
    }

    public void updateEvents(List<EventModel> newEvents) {
        android.util.Log.d("EventsListAdapter", "updateEvents called with " + newEvents.size() + " events");
        this.events.clear();
        this.events.addAll(newEvents);
        notifyDataSetChanged();
        android.util.Log.d("EventsListAdapter", "Events list now has " + events.size() + " items");
    }

    public static class EventListViewHolder extends RecyclerView.ViewHolder {
        private final ImageView eventImage;
        private final TextView eventTitle;
        private final TextView eventLocation;
        private final TextView eventTime;
        private final TextView eventStatus;

        public EventListViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventListImage);
            eventTitle = itemView.findViewById(R.id.eventListTitle);
            eventLocation = itemView.findViewById(R.id.eventListLocation);
            eventTime = itemView.findViewById(R.id.eventListTime);
            eventStatus = itemView.findViewById(R.id.eventListStatus);
        }

        public void bind(EventModel event, OnEventClickListener listener) {
            android.util.Log.d("EventsListAdapter", "bind() called for: " + event.getTitle());
            android.util.Log.d("EventsListAdapter", "View IDs - Image: " + (eventImage != null) + ", Title: " + (eventTitle != null) + ", Location: " + (eventLocation != null));
            
            if (eventImage != null) {
                eventImage.setImageResource(R.drawable.sample_image);
            }
            if (eventTitle != null) {
                eventTitle.setText(event.getTitle());
            }
            if (eventLocation != null) {
                eventLocation.setText(event.getLocation());
            }

            eventTime.setText(event.getFormattedTime());

            String status;
            Date eventDate = event.getDate();
            Date now = new Date();

            if (eventDate != null && eventDate.after(now)) {
                status = "Upcoming";
            } else if (eventDate != null && Math.abs(eventDate.getTime() - now.getTime()) < 3600000) {
                status = "Ongoing";
            } else {
                status = "Past";
            }

            eventStatus.setText(status);

            // Set status color based on event status
            int statusColor;
            if (status.equals("Selected")) {
                statusColor = itemView.getContext().getColor(R.color.status_selected);
            } else {
                statusColor = itemView.getContext().getColor(R.color.status_not_selected);
            }
            eventStatus.setTextColor(statusColor);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });
        }
    }
}