package com.example.pixel_events.settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.profile.Profile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class NotifPrefActivity extends AppCompatActivity {

    private int intUserId = 1593347960; // TODO: Replace with actual user ID (from login/session)
    private String strUserID = "1593347960";
    private Button notifAllButton, notifWinButton, notifLoseButton;
    private ImageButton backButton;
    private Profile currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noti_pref); // ensure your layout file name matches

        notifAllButton = findViewById(R.id.notifAllButton);
        notifWinButton = findViewById(R.id.notifWinButton);
        notifLoseButton = findViewById(R.id.notifLoseButton);
        backButton = findViewById(R.id.notiPrefBackButton);

        // Step 1: Get user profile data
        DatabaseHandler.getInstance().getAcc(strUserID.toString(), profile -> {
            if (profile != null) {
                currentProfile = profile;
            } else {
                Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
            }
        }, e -> Toast.makeText(this, "Failed to load user: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // Step 2: Set click listeners
        notifAllButton.setOnClickListener(v -> toggleNotification(0));
        notifWinButton.setOnClickListener(v -> toggleNotification(1));
        notifLoseButton.setOnClickListener(v -> toggleNotification(2));
        backButton.setOnClickListener(v -> startActivity(new Intent(NotifPrefActivity.this, MainSettingsActivity.class)));
    }

    private void toggleNotification(int index) {
        if (currentProfile == null) return;

        List<Boolean> prefs = currentProfile.getNotify();
        if (prefs == null || prefs.size() < 3) {
            prefs = new ArrayList<>();
            prefs.add(false);
            prefs.add(false);
            prefs.add(false);
        }

        boolean newValue = !prefs.get(index);
        prefs.set(index, newValue);
        currentProfile.setNotify(prefs);

        Map<String, Object> updates = new HashMap<>();
        updates.put("notifPrefs", prefs);

        DatabaseHandler.getInstance().modifyAcc(intUserId, updates, (Consumer<String>) error -> {
            runOnUiThread(() -> {
                if (error == null) {
                    Toast.makeText(this, "Notification preferences updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Update failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
