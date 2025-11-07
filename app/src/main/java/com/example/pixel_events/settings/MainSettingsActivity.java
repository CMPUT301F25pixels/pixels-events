package com.example.pixel_events.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.LoginActivity;
import com.example.pixel_events.MainActivity;
import com.example.pixel_events.R;
import com.example.pixel_events.SessionManager;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.qr.QRCode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainSettingsActivity extends AppCompatActivity {
    // Setup buttons
    private Button viewProfileButton, regHistButton, notifPrefButton, logoutButton, delAccButton;
    private ImageButton homeButton, QRButton, myEventsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_main);

        // Initialize all the buttons
        viewProfileButton = findViewById(R.id.settingsViewProfButton);
        regHistButton = findViewById(R.id.settingsRegHisButton);
        notifPrefButton = findViewById(R.id.settingsNotiPrefButton);
        logoutButton = findViewById(R.id.settingsLogOutButton);
        delAccButton = findViewById(R.id.settingsDelAccButton);

        homeButton = findViewById(R.id.settingsHomeButton);
        QRButton = findViewById(R.id.settingsQRButton);
        myEventsButton = findViewById(R.id.settingsMyEventsButton);

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

        // Go to Home Page Activity
        homeButton.setOnClickListener(v -> startActivity((new Intent(MainSettingsActivity.this, MainActivity.class))));

        // Go to QR Scanner Activity
        QRButton.setOnClickListener(v -> startActivity(new Intent(MainSettingsActivity.this, QRCode.class)));

        // Go to My Events Activity TODO
//        myEventsButton.setOnClickListener(v -> startActivity(new Intent(MainSettingsActivity.this, )));

        // DO nothing for Settings
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
            // Log the user out TODO
            dialog.dismiss();
//            startActivity(new Intent(SettingsActivity.this, ));            // Return to Login Page TODO
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
            // TODO: getProfileId will return userID
            // String a = SessionManager.getProfileId(this);
            int userID = 1593347960;                 // TEMP VALUE
            DatabaseHandler.getInstance().deleteAcc(userID);
            startActivity(new Intent(MainSettingsActivity.this, LoginActivity.class));
            dialog.dismiss();
        });

        // set negative
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Display the Pop-up
        dialog.show();
    }
}
