package com.example.pixel_events.events;

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
import com.example.pixel_events.utils.ImageConversion;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * InvitationAdapter
 *
 * RecyclerView adapter for displaying event invitations to entrants.
 * Shows accept/decline buttons for lottery-won events.
 * Handles invitation interaction callbacks.
 *
 * Implements:
 * - US 01.05.02 (Accept invitation UI)
 * - US 01.05.03 (Decline invitation UI)
 *
 * Collaborators:
 * - EventInvitation: Data model
 * - NotificationFragment: Parent fragment
 */
public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.InvitationViewHolder> {

    private List<EventInvitation> invitations;
    private OnInvitationInteractionListener listener;

    public interface OnInvitationInteractionListener {
        void onAccept(EventInvitation invitation);

        void onDecline(EventInvitation invitation);
    }

    public InvitationAdapter(List<EventInvitation> invitations, OnInvitationInteractionListener listener) {
        this.invitations = invitations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InvitationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item_invitations, parent, false);
        return new InvitationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvitationViewHolder holder, int position) {
        EventInvitation invitation = invitations.get(position);
        Event event = invitation.getEvent();

        holder.eventName.setText(event.getTitle());
        int orgID = event.getOrganizerId();
        DatabaseHandler.getInstance().getProfile(orgID, profile -> {
            holder.organizerName.setText(profile.getUserName());
        }, e -> {
            holder.organizerName.setText("Organizer");
        });
        holder.location.setText(event.getLocation());

        // Format date and time strings
        String startDate = event.getEventStartDate();
        String endDate = event.getEventEndDate();
        String startTime = event.getEventStartTime();
        String endTime = event.getEventEndTime();

        if (startDate != null && endDate != null) {
            if (startTime != null && endTime != null) {
                holder.date.setText(String.format("%s %s - %s %s", startDate, startTime, endDate, endTime));
            } else {
                holder.date.setText(String.format("%s - %s", startDate, endDate));
            }
        }

        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            holder.eventImage.setImageBitmap(ImageConversion.base64ToBitmap(event.getImageUrl()));
        } else {
            holder.eventImage.setImageResource(R.drawable.sample_image); // A default placeholder
        }

        holder.acceptButton.setOnClickListener(v -> listener.onAccept(invitation));
        holder.declineButton.setOnClickListener(v -> listener.onDecline(invitation));
    }

    @Override
    public int getItemCount() {
        return invitations.size();
    }

    public void updateInvitations(List<EventInvitation> newInvitations) {
        this.invitations = newInvitations;
        notifyDataSetChanged();
    }

    private void removeInvitationAt(int position) {
        if (position >= 0 && position < invitations.size()) {
            invitations.remove(position);
            notifyItemRemoved(position);
            // Notify range changed to keep positions consistent for subsequent operations
            notifyItemRangeChanged(position, invitations.size() - position);
        }
    }

    public static class InvitationViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView organizerName, eventName, date, location;
        ImageButton acceptButton, declineButton;

        public InvitationViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.invitation_image);
            organizerName = itemView.findViewById(R.id.invitation_org_name);
            eventName = itemView.findViewById(R.id.invitation_event_name);
            date = itemView.findViewById(R.id.invitation_date);
            location = itemView.findViewById(R.id.invitation_location);
            acceptButton = itemView.findViewById(R.id.invitation_accept);
            declineButton = itemView.findViewById(R.id.invitation_decline);
        }
    }
}
