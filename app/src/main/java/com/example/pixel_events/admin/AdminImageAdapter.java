package com.example.pixel_events.admin;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixel_events.R;
import com.example.pixel_events.events.Event;
import android.util.Log;

import com.example.pixel_events.utils.ImageConversion;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminImageAdapter
 *
 * RecyclerView adapter for administrators to browse and delete event poster images.
 * Displays image thumbnails with delete buttons.
 * Triggers organizer notifications upon image removal.
 *
 * Implements:
 * - US 03.03.01 (Remove images)
 * - US 03.06.01 (Browse images)
 *
 * Collaborators:
 * - Event: Source of image data
 * - AdminImageFragment: Parent fragment
 * - ImageConversion: Image display
 */
public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.Holder> {
    public interface Listener { void onDelete(Event e); }
    private final List<Event> data = new ArrayList<>();
    private final Listener listener;

    public AdminImageAdapter(Listener l) { this.listener = l; }

    public void setData(List<Event> events) {
        data.clear();
        if (events != null) data.addAll(events);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_image, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Event e = data.get(position);
        String img = e.getImageUrl();
        String s = img.trim();
        if (!s.isEmpty()) {
            try {
                Bitmap bm = ImageConversion.base64ToBitmap(s);
                if (bm != null) {
                    holder.image.setImageBitmap(bm);
                } else {
                    Log.w("AdminImageAdapter", "decoded bitmap is null for event=" + e.getEventId());
                    holder.image.setImageResource(R.drawable.sample_image);
                }
            } catch (Exception ex) {
                Log.w("AdminImageAdapter", "failed to decode base64 image for event=" + e.getEventId(), ex);
                holder.image.setImageResource(R.drawable.sample_image);
            }
        } else {
            holder.image.setImageResource(R.drawable.sample_image);
        }

        holder.delete.setVisibility(View.VISIBLE);
        
        holder.delete.setOnClickListener(v -> listener.onDelete(e));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView image;  ImageButton delete;
        Holder(@NonNull View view) {
            super(view);
            image = view.findViewById(R.id.admin_image_poster);
            delete = view.findViewById(R.id.admin_image_delete);
        }
    }
}
