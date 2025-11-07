package com.example.pixel_events.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "pixels_prefs";
    private static final String KEY_PROFILE_ID = "current_profile_id";
    
    private Button confirmButton, cancelButton;
    private EditText usn, dob, gender, email, num, city, prov;
    private TextView role;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_editprofile);

        // Get current user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userID = prefs.getString(KEY_PROFILE_ID, "0");
        
        Log.d("EditProfileActivity", "Loading profile for user ID: " + userID);

        // Setup Buttons
        confirmButton = findViewById(R.id.profileEditConfirmButton);
        cancelButton = findViewById(R.id.profileEditCancelButton);

        // Get editTexts
        usn = findViewById(R.id.profileInputUSN);
        dob = findViewById(R.id.profileInputDOBText);
        gender = findViewById(R.id.profileInputGenderText);
        email = findViewById(R.id.profileInputEmailText);
        num = findViewById(R.id.profileInputPhoneText);
        city = findViewById(R.id.profileInputCityText);
        prov = findViewById(R.id.profileInputProvinceText);

        // Get role
        role = findViewById(R.id.profileEditRoleText);

        // Load profile data
        loadProfileData();

        // Setup button listeners
        setupButtonListeners();
    }

    private void loadProfileData() {
        DatabaseHandler.getInstance().getAcc(userID, profile -> {
            // Check if activity is still valid
            if (isFinishing() || isDestroyed()) {
                return;
            }

            if (profile != null) {
                runOnUiThread(() -> {
                    try {
                        usn.setHint(profile.getUserName());
                        role.setText((profile.getAccType()));
                        Date date = profile.getDOB();
                        if (date != null) {
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            String formattedDate = formatter.format(date);
                            dob.setHint(formattedDate);
                        }

                        gender.setHint(profile.getGender());
                        email.setHint(profile.getEmail());
                        num.setHint(profile.getPhoneNum());
                        city.setHint(profile.getCity());
                        prov.setHint(profile.getProvince());
                    } catch (Exception e) {
                        Log.e("EditProfileActivity", "Error setting hints: ", e);
                    }
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this,
                            "Profile not found", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }, error -> {
            if (!isFinishing() && !isDestroyed()) {
                runOnUiThread(() -> {
                    Log.e("EditProfileActivity", "Error loading profile: " + error);
                    Toast.makeText(EditProfileActivity.this,
                            "Error loading profile", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void setupButtonListeners() {
        confirmButton.setOnClickListener(v -> {
            Map<String, Object> updates = new HashMap<>();

            // Only include non-empty fields
            if (!usn.getText().toString().trim().isEmpty()) {
                updates.put("userName", usn.getText().toString().trim());
            }

            if (!dob.getText().toString().trim().isEmpty()) {
                String dobInput = dob.getText().toString().trim();
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    formatter.setLenient(false);
                    Date parsedDate = formatter.parse(dobInput);
                    updates.put("DOB", parsedDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                    dob.setError("Invalid date format. Use dd/MM/yyyy");
                    return;
                }
            }

            if (!gender.getText().toString().trim().isEmpty()) {
                updates.put("gender", gender.getText().toString().trim());
            }

            if (!email.getText().toString().trim().isEmpty()) {
                updates.put("email", email.getText().toString().trim());
            }

            if (!num.getText().toString().trim().isEmpty()) {
                updates.put("phoneNum", num.getText().toString().trim());
            }

            if (!city.getText().toString().trim().isEmpty()) {
                updates.put("city", city.getText().toString().trim());
            }

            if (!prov.getText().toString().trim().isEmpty()) {
                updates.put("province", prov.getText().toString().trim());
            }

            // If there are updates, push to Firebase
            if (!updates.isEmpty()) {
                DatabaseHandler.getInstance().modifyAcc(
                        Integer.parseInt(userID),
                        updates,
                        error -> {
                            if (!isFinishing() && !isDestroyed()) {
                                runOnUiThread(() -> {
                                    if (error != null) {
                                        Toast.makeText(EditProfileActivity.this,
                                                "Error updating profile", Toast.LENGTH_SHORT).show();
                                        Log.e("EditProfileActivity", "Update error: " + error);
                                    } else {
                                        Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                        }
                );
            } else {
                // No updates made, just go back
                finish();
            }
        });

        cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }
}