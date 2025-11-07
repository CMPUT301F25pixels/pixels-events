package com.example.pixel_events.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.pixel_events.MainActivity;
import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.EventsListActivity;
import com.example.pixel_events.qr.QRScannerActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "pixels_prefs";
    private static final String KEY_PROFILE_ID = "current_profile_id";
    
    private Button editButton;
    private ImageButton backButton;
    private TextView usn, dob, gender, email, num, city, prov, role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_profile);

        // Setup Buttons
        editButton = findViewById(R.id.profileEditButton);
        backButton = findViewById(R.id.profileBackButton);

        // Get TextView
        usn = findViewById(R.id.profileUsernameText);
        dob = findViewById(R.id.profileDOBText);
        gender = findViewById(R.id.profileGenderText);
        email = findViewById(R.id.profileEmailText);
        num = findViewById(R.id.profilePhoneText);
        city = findViewById(R.id.profileCityText);
        prov = findViewById(R.id.profileProvinceText);
        role = findViewById(R.id.profileRoleText);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userID = prefs.getString(KEY_PROFILE_ID, "0");
        
        DatabaseHandler.getInstance().getAcc(userID, profile -> {
            if (profile != null) {
                 usn.setText(profile.getUserName());

                Date date = profile.getDOB();
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String formattedDate = formatter.format(date);

                role.setText(profile.getAccType());
                dob.setText(formattedDate);
                gender.setText(profile.getGender());
                email.setText(profile.getEmail());
                num.setText(String.valueOf(profile.getPhoneNum()));
                city.setText(profile.getCity());
                prov.setText(profile.getProvince());

             } else {
                // Handle case where profile does not exist
            }
         }, error -> {
             // Handle error
        });

        // Go back to main settings
        backButton.setOnClickListener(v -> finish());

        // Go to edit Page
        editButton.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class)));
        
        setupBottomNav();
    }
    
    private void setupBottomNav() {
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navEvents = findViewById(R.id.nav_events);
        LinearLayout navScanner = findViewById(R.id.nav_scanner);
        LinearLayout navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        navEvents.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventsListActivity.class);
            startActivity(intent);
        });

        navScanner.setOnClickListener(v -> {
            Intent intent = new Intent(this, QRScannerActivity.class);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Already on Profile", Toast.LENGTH_SHORT).show();
        });
    }
}
