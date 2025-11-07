package com.example.pixel_events.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.login.LoginActivity;
import com.example.pixel_events.MainActivity;
import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.EventsListActivity;
import com.example.pixel_events.qr.QRCode;
import com.example.pixel_events.qr.QRScannerActivity;


public class MainSettingsActivity extends AppCompatActivity {
    private static final String TAG = "MainSettingsActivity";
    private static final String PREFS_NAME = "pixels_prefs";
    private static final String KEY_PROFILE_ID = "current_profile_id";
    private static final String KEY_ROLE = "current_role";
    
    // Setup buttons
    private Button viewProfileButton, regHistButton, notifPrefButton, logoutButton, delAccButton;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_main);

        // Get current user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserId = prefs.getString(KEY_PROFILE_ID, "0");
        Log.d(TAG, "Current user ID: " + currentUserId);

        // Initialize all the buttons
        viewProfileButton = findViewById(R.id.settingsViewProfButton);
        regHistButton = findViewById(R.id.settingsRegHisButton);
        notifPrefButton = findViewById(R.id.settingsNotiPrefButton);
        logoutButton = findViewById(R.id.settingsLogOutButton);
        delAccButton = findViewById(R.id.settingsDelAccButton);

        // Set onClickListeners for all buttons

        // Go to Profile activity
        viewProfileButton.setOnClickListener(v -> startActivity(new Intent(MainSettingsActivity.this, ProfileActivity.class)));

        // Go to registration History Activity TODO
//        regHistButton.setOnClickListener(v -> startActivity(new Intent(MainSettingsActivity.this, )));

        // Go to Notification Preferences Activity
        notifPrefButton.setOnClickListener(v -> startActivity(new Intent(MainSettingsActivity.this, NotifPrefActivity.class)));

        // Show Logout Popup
        logoutButton.setOnClickListener(v-> showLogoutDialogue());

        // Show Delete Account Popup
        delAccButton.setOnClickListener(v -> showDeleteDialogue());
        
        setupBottomNav();
    }
    
    private void setupBottomNav() {
        android.widget.LinearLayout navHome = findViewById(R.id.nav_home);
        android.widget.LinearLayout navEvents = findViewById(R.id.nav_events);
        android.widget.LinearLayout navScanner = findViewById(R.id.nav_scanner);
        android.widget.LinearLayout navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        navEvents.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventsListActivity.class);
            startActivity(intent);
            finish();
        });

        navScanner.setOnClickListener(v -> {
            Intent intent = new Intent(this, QRScannerActivity.class);
            startActivity(intent);
            finish();
        });

        navProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Already on Profile", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Show a pop-up Dialogue box with custom .xml file and ask user once again if they want to logout.
     */
    public void showLogoutDialogue() {
        Dialog dialog = new Dialog(MainSettingsActivity.this);

        // Set view to the custom xml
        dialog.setContentView(R.layout.dialogue_logout_acc);

        // Get the positive and negative buttons
        Button confirmButton = dialog.findViewById(R.id.logoutConfirmButton);
        Button cancelButton = dialog.findViewById(R.id.logoutCancelButton);

        // Set Positive
        confirmButton.setOnClickListener(v -> {
            // Clear SharedPreferences to log out
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            
            Log.d(TAG, "User logged out, SharedPreferences cleared");
            
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            
            // Return to Login Page
            Intent intent = new Intent(MainSettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            
            dialog.dismiss();
            finish();
        });

        // Set Negative
        cancelButton.setOnClickListener(v -> dialog.dismiss());                // Close the popup

        // Display the Pop-up
        dialog.show();

    }

    /**
     * Show a pop-up Dialogue box with custom .xml file and ask user once again if they want to delete account data.
     */
    private void showDeleteDialogue() {
        Dialog dialog = new Dialog(MainSettingsActivity.this);

        // Set view to the custom xml
        dialog.setContentView(R.layout.dialogue_delete_acc);

        // Get positive and negative
        Button confirmButton = dialog.findViewById(R.id.deletePopupConfirmButton);
        Button cancelButton = dialog.findViewById(R.id.deletePopupCancelButton);

        // Set positive
        confirmButton.setOnClickListener(v -> {
            try {
                int userIDInt = Integer.parseInt(currentUserId);
                
                // Delete account from database
                DatabaseHandler.getInstance().deleteAcc(userIDInt);
                
                // Clear SharedPreferences
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                
                Log.d(TAG, "Account deleted for user: " + userIDInt);
                
                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                
                // Return to Login Page
                Intent intent = new Intent(MainSettingsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                
                dialog.dismiss();
                finish();
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid user ID: " + currentUserId, e);
                Toast.makeText(this, "Error deleting account", Toast.LENGTH_SHORT).show();
            }
        });

        // set negative
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Display the Pop-up
        dialog.show();
    }
}
