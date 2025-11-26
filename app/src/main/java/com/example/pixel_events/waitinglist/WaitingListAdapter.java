package com.example.pixel_events.waitinglist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixel_events.R;
import com.example.pixel_events.profile.Profile;

import java.util.List;

public class WaitingListAdapter extends RecyclerView.Adapter<WaitingListAdapter.VH> {
    public interface OnItemClick {
        void onClick(Profile profile);
    }

    private final List<Profile> items;
    private final WaitingList waitingList;
    private final OnItemClick listener;

    public WaitingListAdapter(List<Profile> items, WaitingList waitingList, OnItemClick listener) {
        this.items = items;
        this.waitingList = waitingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Profile p = items.get(position);
        holder.title.setText(p.getUserName() != null ? p.getUserName() : "Unknown");
        holder.subtitle.setText(p.getEmail() != null ? p.getEmail() : "");
        holder.avatar.setImageResource(R.drawable.ic_launcher_foreground);

        // Find status for this user
        int status = 0;
        if (waitingList != null && waitingList.getWaitList() != null) {
            for (WaitlistUser user : waitingList.getWaitList()) {
                if (user.getUserId() == p.getUserId()) {
                    status = user.getStatus();
                    break;
                }
            }
        }

        // Show status with color: 1-Selected(white), 2-Accepted(green),
        // 3-Declined(red). 0=Waiting (gray)
        if (status == 1) {
            holder.statusText.setText("Selected");
            holder.statusText
                    .setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white, null));
            holder.statusText.setVisibility(View.VISIBLE);
        } else if (status == 2) {
            holder.statusText.setText("Accepted");
            holder.statusText.setTextColor(
                    holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_light, null));
            holder.statusText.setVisibility(View.VISIBLE);
        } else if (status == 3) {
            holder.statusText.setText("Declined");
            holder.statusText.setTextColor(
                    holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_light, null));
            holder.statusText.setVisibility(View.VISIBLE);
        } else if (status == 0) {
            holder.statusText.setText("Waiting");
            holder.statusText.setTextColor(
                    holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray, null));
            holder.statusText.setVisibility(View.VISIBLE);
        } else {
            holder.statusText.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(p));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView avatar;
        final TextView title;
        final TextView subtitle;
        final TextView statusText;

        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.item_profile_image);
            title = itemView.findViewById(R.id.item_profile_title);
            subtitle = itemView.findViewById(R.id.item_profile_email);
            statusText = itemView.findViewById(R.id.item_profile_status);
        }
    }
}
