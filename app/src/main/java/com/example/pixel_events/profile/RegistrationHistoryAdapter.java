package com.example.pixel_events.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixel_events.R;
import com.example.pixel_events.utils.ImageConversion;

import java.util.List;

/**
 * RegistrationHistoryAdapter
 *
 * RecyclerView adapter for displaying entrant's event registration history.
 * Shows events with lottery outcome indicators (won, lost, accepted, declined).
 * Enables navigation to event details from history.
 *
 * Implements:
 * - US 01.02.03 (View registration history and lottery outcomes)
 *
 * Collaborators:
 * - Event: Historical event data
 * - RegistrationHistoryFragment: Parent fragment
 * - ImageConversion: Event poster display
 */
public class RegistrationHistoryAdapter
        extends RecyclerView.Adapter<RegistrationHistoryAdapter.RegistrationHistoryViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(RegistrationHistoryFragment.EventHistoryItem item);
    }

    private final List<RegistrationHistoryFragment.EventHistoryItem> items;
    private final OnItemClickListener listener;

    public RegistrationHistoryAdapter(List<RegistrationHistoryFragment.EventHistoryItem> items,
            OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RegistrationHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item_small, parent, false);
        return new RegistrationHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistrationHistoryViewHolder holder, int position) {
        RegistrationHistoryFragment.EventHistoryItem item = items.get(position);

        holder.eventTitle.setText(item.event.getTitle());
        holder.eventTime.setText(item.event.getEventStartTime());
        holder.host.setText(""); // Could fetch organizer name if needed

        // Display status with color
        String statusText;
        int statusColor;
        switch (item.status) {
            case 0:
                statusText = "Waiting";
                statusColor = android.R.color.darker_gray;
                break;
            case 1:
                statusText = "Selected";
                statusColor = android.R.color.white;
                break;
            case 2:
                statusText = "Accepted";
                statusColor = android.R.color.holo_green_light;
                break;
            case 3:
                statusText = "Declined";
                statusColor = android.R.color.holo_red_light;
                break;
            default:
                statusText = "Unknown";
                statusColor = android.R.color.darker_gray;
        }

        holder.status.setText(statusText);
        holder.status.setTextColor(holder.itemView.getContext().getResources().getColor(statusColor, null));

        // Load event image
        if (item.event.getImageUrl() != null && !item.event.getImageUrl().isEmpty()) {
            holder.eventImage.setImageBitmap(ImageConversion.base64ToBitmap(item.event.getImageUrl()));
        } else {
            holder.eventImage.setImageResource(R.drawable.sample_image);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RegistrationHistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventTitle;
        TextView eventTime;
        TextView host;
        TextView status;

        RegistrationHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.event_small_image);
            eventTitle = itemView.findViewById(R.id.event_small_title);
            eventTime = itemView.findViewById(R.id.event_small_time);
            host = itemView.findViewById(R.id.event_small_host);
            status = itemView.findViewById(R.id.event_small_status);
        }
    }
}
