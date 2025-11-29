package com.example.pixel_events.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.profile.Profile;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Admin fragment to review all notifications sent by organizers
 * US 03.08.01
 */
public class AdminNotificationLogFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationLogAdapter adapter;
    private TextView emptyView;
    private ImageButton backButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_notification_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.notification_log_recycler);
        emptyView = view.findViewById(R.id.notification_log_empty);
        backButton = view.findViewById(R.id.notification_log_back_btn);

        adapter = new NotificationLogAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        loadNotificationLogs();
    }

    private void loadNotificationLogs() {
        DatabaseHandler.getInstance().getFirestore()
                .collection("NotificationLogs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<NotificationLog> logs = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                        Notification n = doc.toObject(Notification.class);
                        if (n != null) {
                            NotificationLog log = new NotificationLog(n);
                            logs.add(log);
                        }
                    }

                    if (adapter != null) {
                        adapter.setLogs(logs);
                    }
                    if (emptyView != null) {
                        emptyView.setVisibility(logs.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    // Load sender and recipient names
                    for (NotificationLog log : logs) {
                        loadUserNames(log);
                    }
                })
                .addOnFailureListener(e -> Log.e("AdminNotificationLog", "Error loading logs", e));
    }

    private void loadUserNames(NotificationLog log) {
        // Load recipient name
        DatabaseHandler.getInstance().getProfile(log.notification.getRecipientId(), 
            recipient -> {
                if (recipient != null) {
                    log.recipientName = recipient.getUserName();
                    adapter.notifyDataSetChanged();
                }
            }, 
            e -> Log.e("AdminNotificationLog", "Failed to load recipient", e));

        // Load sender name if available
        if (log.notification.getSenderId() > 0) {
            DatabaseHandler.getInstance().getProfile(log.notification.getSenderId(), 
                sender -> {
                    if (sender != null) {
                        log.senderName = sender.getUserName();
                        adapter.notifyDataSetChanged();
                    }
                }, 
                e -> Log.e("AdminNotificationLog", "Failed to load sender", e));
        }
    }

    static class NotificationLog {
        Notification notification;
        String senderName = "System";
        String recipientName = "Unknown";

        NotificationLog(Notification notification) {
            this.notification = notification;
        }
    }

    static class NotificationLogAdapter extends RecyclerView.Adapter<NotificationLogAdapter.ViewHolder> {
        private List<NotificationLog> logs = new ArrayList<>();

        void setLogs(List<NotificationLog> logs) {
            this.logs = logs;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_log, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NotificationLog log = logs.get(position);
            Notification n = log.notification;

            holder.title.setText(n.getTitle());
            holder.message.setText(n.getMessage());
            holder.from.setText("From: " + log.senderName);
            holder.to.setText("To: " + log.recipientName);
            holder.type.setText("Type: " + n.getType());
            
            if (n.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
                holder.date.setText(sdf.format(n.getTimestamp().toDate()));
            }
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, message, from, to, type, date;

            ViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.notif_log_title);
                message = itemView.findViewById(R.id.notif_log_message);
                from = itemView.findViewById(R.id.notif_log_from);
                to = itemView.findViewById(R.id.notif_log_to);
                type = itemView.findViewById(R.id.notif_log_type);
                date = itemView.findViewById(R.id.notif_log_date);
            }
        }
    }
}


