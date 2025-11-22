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
    private final OnItemClick listener;

    public WaitingListAdapter(List<Profile> items, OnItemClick listener) {
        this.items = items;
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

        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.eventImage);
            title = itemView.findViewById(R.id.eventTitle);
            subtitle = itemView.findViewById(R.id.eventTime);
        }
    }
}
