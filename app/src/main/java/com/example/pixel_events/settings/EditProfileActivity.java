package com.example.pixel_events.settings;

import android.content.Intent;
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
    private Button confirmButton, cancelButton;
    private EditText usn, dob, gender, email, num, city, prov;
    private TextView role;
    private String userID = "1593347960"; // Make it a class member so it's accessible in all methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_editprofile);

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
            if (profile != null) {
                runOnUiThread(() -> {
                    try {
                        usn.setHint(profile.getUserName());
                        role.setText(profile.getAccType());

                        Date date = profile.getDOB();
                        if (date != null) {
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            String formattedDate = formatter.format(date);
                            dob.setHint(formattedDate);
                        }

                        gender.setHint(profile.getGender());
                        email.setHint(profile.getEmail());
                        num.setHint(String.valueOf(profile.getPhoneNum())); // ← FIXED: Convert int to String
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
            Log.d("EditProfileActivity", "Confirm button clicked");
            Map<String, Object> updates = new HashMap<>();

            try {
                // Username
                if (!usn.getText().toString().trim().isEmpty()) {
                    String userName = usn.getText().toString().trim();
                    updates.put("userName", userName);
                    Log.d("EditProfileActivity", "Adding userName: " + userName);
                }

                // Date of Birth
                if (!dob.getText().toString().trim().isEmpty()) {
                    String dobInput = dob.getText().toString().trim();
                    Log.d("EditProfileActivity", "Processing DOB: " + dobInput);
                    try {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        formatter.setLenient(false);
                        Date parsedDate = formatter.parse(dobInput);
                        updates.put("DOB", parsedDate);
                        Log.d("EditProfileActivity", "Added DOB: " + parsedDate);
                    } catch (ParseException e) {
                        Log.e("EditProfileActivity", "Date parse error", e);
                        dob.setError("Invalid date format. Use dd/MM/yyyy");
                        return;
                    }
                }

                // Gender
                if (!gender.getText().toString().trim().isEmpty()) {
                    String genderValue = gender.getText().toString().trim();
                    updates.put("gender", genderValue);
                    Log.d("EditProfileActivity", "Adding gender: " + genderValue);
                }

                // Email
                if (!email.getText().toString().trim().isEmpty()) {
                    String emailValue = email.getText().toString().trim();
                    updates.put("email", emailValue);
                    Log.d("EditProfileActivity", "Adding email: " + emailValue);
                }

                // Phone Number
                if (!num.getText().toString().trim().isEmpty()) {
                    String numText = num.getText().toString().trim();
                    Log.d("EditProfileActivity", "Processing phone: " + numText);
                    try {
                        int phoneNumber = Integer.parseInt(numText);
                        updates.put("phoneNum", phoneNumber);
                        Log.d("EditProfileActivity", "Added phoneNum: " + phoneNumber);
                    } catch (NumberFormatException e) {
                        Log.e("EditProfileActivity", "Phone parse error", e);
                        num.setError("Invalid phone number");
                        Toast.makeText(EditProfileActivity.this,
                                "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // City
                if (!city.getText().toString().trim().isEmpty()) {
                    String cityValue = city.getText().toString().trim();
                    updates.put("city", cityValue);
                    Log.d("EditProfileActivity", "Adding city: " + cityValue);
                }

                // Province
                if (!prov.getText().toString().trim().isEmpty()) {
                    String provValue = prov.getText().toString().trim();
                    updates.put("province", provValue);
                    Log.d("EditProfileActivity", "Adding province: " + provValue);
                }

                // Log all updates
                Log.d("EditProfileActivity", "Total updates to send: " + updates.size());
                Log.d("EditProfileActivity", "Updates map: " + updates.toString());

                // If there are updates, push to Firebase
                if (!updates.isEmpty()) {
                    Log.d("EditProfileActivity", "Calling modifyAcc with userID: " + userID);
                    DatabaseHandler.getInstance().modifyAcc(
                            Integer.parseInt(userID),
                            updates,
                            error -> {
                                Log.d("EditProfileActivity", "modifyAcc callback received");
                                if (!isFinishing() && !isDestroyed()) {
                                    runOnUiThread(() -> {
                                        if (error != null) {
                                            Log.e("EditProfileActivity", "Update error: " + error);
                                            Toast.makeText(EditProfileActivity.this,
                                                    "Error updating profile: " + error, Toast.LENGTH_LONG).show();
                                        } else {
                                            Log.d("EditProfileActivity", "Update successful");
                                            Toast.makeText(EditProfileActivity.this,
                                                    "Profile updated successfully", Toast.LENGTH_SHORT).show();
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
                    Log.d("EditProfileActivity", "No updates to send, finishing");
                    finish();
                }
            } catch (Exception e) {
                Log.e("EditProfileActivity", "Unexpected error in setupButtonListeners", e);
                Toast.makeText(EditProfileActivity.this,
                        "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }
}