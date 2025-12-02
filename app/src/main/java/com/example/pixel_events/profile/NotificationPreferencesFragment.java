package com.example.pixel_events.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.profile.Profile;

import android.app.AlertDialog;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ToggleButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NotificationPreferencesFragment
 *
 * Fragment for users to manage notification opt-in/opt-out preferences.
 * Allows toggling notifications for: all messages, lottery wins, lottery losses.
 * Preferences are checked before sending notifications.
 *
 * Implements:
 * - US 01.04.03 (Opt out of receiving notifications)
 *
 * Collaborators:
 * - Profile: Stores notification preferences
 * - DatabaseHandler: Preference persistence
 * - Notification: Respects preferences when sending
 */
public class NotificationPreferencesFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification_preferences, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views and set up listeners
        Profile profile = AuthManager.getInstance().getCurrentUserProfile();
        ToggleButton allBtn = view.findViewById(R.id.notification_all_button);
        ToggleButton winBtn = view.findViewById(R.id.notification_win_button);
        ToggleButton loseBtn = view.findViewById(R.id.notifications_lose_button);
        Button saveBtn = view.findViewById(R.id.notification_save_button);
        ImageButton backBtn = view.findViewById(R.id.notifications_back_button);


        // Prefill
        List<Boolean> notify = new ArrayList<>();
        if (profile != null && profile.getNotify() != null) {
            notify = profile.getNotify();
        }
        // ensure size 3
        while (notify.size() < 3)
            notify.add(false);

        allBtn.setChecked(notify.get(0));
        winBtn.setChecked(notify.get(1));
        loseBtn.setChecked(notify.get(2));

        saveBtn.setOnClickListener(v -> {
            List<Boolean> newNotify = new ArrayList<>();
            newNotify.add(allBtn.isChecked());
            newNotify.add(winBtn.isChecked());
            newNotify.add(loseBtn.isChecked());

            Map<String, Object> updates = new HashMap<>();
            updates.put("notify", newNotify);

            if (profile == null) {
                Toast.makeText(requireContext(), "Profile not loaded", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHandler.getInstance().modify(DatabaseHandler.getInstance().getAccountCollection(),
                    profile.getUserId(), updates, error -> {
                        if (error == null) {
                            // update local profile
                            profile.setNotify(newNotify);
                            AuthManager.getInstance().setCurrentUserProfile(profile);
                            requireActivity().runOnUiThread(() -> Toast
                                    .makeText(requireContext(), "Preferences saved", Toast.LENGTH_SHORT).show());
                            requireActivity().getSupportFragmentManager().popBackStack();
                        } else {
                            requireActivity().runOnUiThread(() -> Toast
                                    .makeText(requireContext(), "Failed to save: " + error, Toast.LENGTH_LONG).show());
                        }
                    });
        });

        backBtn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }
}
