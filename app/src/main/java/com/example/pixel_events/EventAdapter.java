package com.example.pixel_events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import com.example.pixel_events.events.Event;


public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DATE_HEADER = 0;
    private static final int TYPE_EVENT = 1;

    private final List<Object> items;
    private final OnEventClickListener clickListener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventAdapter(List<Object> items) {
        this(items, null);
    }

    public EventAdapter(List<Object> items, OnEventClickListener clickListener) {
        this.items = items;
        this.clickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_DATE_HEADER : TYPE_EVENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_DATE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.event_items, parent, false);
            return new EventViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DateHeaderViewHolder) {
            String dateHeader = (String) items.get(position);
            ((DateHeaderViewHolder) holder).bind(dateHeader);
        } else if (holder instanceof EventViewHolder) {
            Event event = (Event) items.get(position);
            ((EventViewHolder) holder).bind(event, clickListener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView dateText;

        public DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(android.R.id.text1);
            dateText.setTextSize(16);
            dateText.setTextColor(itemView.getContext().getColor(android.R.color.white));
        }

        public void bind(String dateHeader) {
            dateText.setText(dateHeader);
        }
    }


    public static class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView organizerName;
        private final TextView type;
        private final TextView time;
        private final TextView location;
        private final ImageView image;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.eventTitle);
            organizerName = itemView.findViewById(R.id.host);
            type = itemView.findViewById(R.id.eventType);
            image = itemView.findViewById(R.id.eventImage);
            time = itemView.findViewById(R.id.eventTime);
            location = itemView.findViewById(R.id.eventLocation);
        }

        public void bind(Event event, OnEventClickListener listener) {
            title.setText(event.getTitle());
            organizerName.setText(event.getOrganizerName());
            type.setText(event.getType());
            image.setImageResource(event.getImageResId());
            time.setText(event.getFormattedTime());
            location.setText(event.getLocation());

            // Set click listener if provided
            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onEventClick(event));
            }
        }
    }
}