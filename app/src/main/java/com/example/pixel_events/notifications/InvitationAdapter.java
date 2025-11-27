package com.example.pixel_events.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.utils.ImageConversion;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.ViewHolder> {

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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item_invitations, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

        if (event.getEventStartDate() != null && event.getEventEndDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            holder.date.setText(String.format("%s - %s", sdf.format(event.getEventStartDate()), sdf.format(event.getEventEndDate())));
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView organizerName, eventName, date, location;
        ImageButton acceptButton, declineButton;

        public ViewHolder(@NonNull View itemView) {
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
