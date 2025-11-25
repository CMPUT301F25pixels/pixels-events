package com.example.pixel_events.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixel_events.R;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.home.DashboardAdapter;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.utils.ImageConversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.EventViewHolder> {
    private List<Event> events;
    private final OnEventClickListener listener;
    public interface OnEventClickListener {
        void onEventClick(Event event);
        void onDeleteClick(Event event);
    }

    public AdminAdapter(List<Event> events, OnEventClickListener listener) {
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
                .inflate(R.layout.event_item_big, parent, false);
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
        private TextView eventTitle;
        private TextView eventTime;
        private TextView eventLocation;
        private LinearLayout tagsContainer;
        private android.widget.ImageButton deleteButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventListImage);
            eventTitle = itemView.findViewById(R.id.eventListTitle);
            eventTime = itemView.findViewById(R.id.eventListTime);
            eventLocation = itemView.findViewById(R.id.eventListLocation);
            tagsContainer = itemView.findViewById(R.id.eventTagsContainer);
            deleteButton = itemView.findViewById(R.id.admin_event_delete);
        }

        public void bind(Event event, OnEventClickListener listener) {
            eventTitle.setText(event.getTitle());
            eventTime.setText(event.getEventStartTime());
            eventLocation.setText(event.getLocation());
            if (!Objects.equals(event.getImageUrl(), "")) {
                eventImage.setImageBitmap(ImageConversion.base64ToBitmap(event.getImageUrl()));
            } else {
                eventImage.setImageResource(R.drawable.sample_image);
            }

            // Display tags
            displayTags(event);

            if (deleteButton != null) {
                deleteButton.setVisibility(View.VISIBLE);
                deleteButton.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteClick(event);
                });
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onEventClick(event);
            });
        }

        private void displayTags(Event event) {
            if (tagsContainer == null) return;

            tagsContainer.removeAllViews();
            List<String> tags = event.getTags();

            if (tags == null || tags.isEmpty()) {
                return;
            }

            for (String tag : tags) {
                if (tag == null || tag.trim().isEmpty()) continue;

                TextView tagView = new TextView(itemView.getContext());
                tagView.setText(tag);
                tagView.setTextColor(itemView.getContext().getResources().getColor(R.color.white, null));
                tagView.setTextSize(10);
                tagView.setPadding(12, 6, 12, 6);
                tagView.setBackground(itemView.getContext().getResources().getDrawable(R.drawable.view_tag_outline, null));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMarginEnd(6);
                tagView.setLayoutParams(params);

                tagsContainer.addView(tagView);
            }
        }
    }
}
