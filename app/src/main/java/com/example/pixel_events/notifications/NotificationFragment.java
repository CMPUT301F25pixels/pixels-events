package com.example.pixel_events.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.profile.Profile;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private TextView emptyView;
    private List<Notification> notificationList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.notifications_recycler);
        emptyView = view.findViewById(R.id.notifications_empty);
        view.findViewById(R.id.notifications_back_btn).setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        adapter = new NotificationAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        Profile profile = AuthManager.getInstance().getCurrentUserProfile();
        if (profile == null) return;

        DatabaseHandler.getInstance().listenToNotifications(profile.getUserId(), (snapshots, error) -> {
            if (error != null) {
                Log.e("NotificationFragment", "Error listening to notifications", error);
                return;
            }
            if (snapshots != null) {
                notificationList.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                    Notification n = doc.toObject(Notification.class);
                    if (n != null) notificationList.add(n);
                }
                
                if (adapter != null) {
                    adapter.setNotifications(notificationList);
                }
                if (emptyView != null) {
                    emptyView.setVisibility(notificationList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
        });
    }
}




