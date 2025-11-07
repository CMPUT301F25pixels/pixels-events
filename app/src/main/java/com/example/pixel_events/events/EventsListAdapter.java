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

    private final List<Event> events;
    private final OnEventClickListener clickListener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventsListAdapter(List<Event> events, OnEventClickListener clickListener) {
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
        Event event = events.get(position);
        holder.bind(event, clickListener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<Event> newEvents) {
        this.events.clear();
        this.events.addAll(newEvents);
        notifyDataSetChanged();
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

        public void bind(Event event, OnEventClickListener listener) {
            eventImage.setImageResource(R.drawable.sample_image);
            eventTitle.setText(event.getTitle());
            eventLocation.setText(event.getLocation());

            eventTime.setText(event.getEventStartTime());

            String status;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date eventDate = sdf.parse(event.getEventStartDate());
                Date now = new Date();

                if (eventDate != null && eventDate.after(now)) {
                    status = "Upcoming";
                } else if (eventDate != null && Math.abs(eventDate.getTime() - now.getTime()) < 3600000) {
                    status = "Ongoing";
                } else {
                    status = "Past";
                }
            } catch (Exception e) {
                status = "Upcoming";
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