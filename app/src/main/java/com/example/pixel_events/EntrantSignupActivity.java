package com.example.pixel_events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.database.DatabaseHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EntrantSignupActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "pixels_prefs";
    private static final String KEY_ROLE = "current_role";
    private static final String KEY_PROFILE_ID = "current_profile_id";

    private EditText nameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private Button saveButton;
    private Button cancelButton;

    private DatabaseHandler db;
    private int entrantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_signup);

        db = DatabaseHandler.getInstance();

        // Get the entrantId passed from LoginActivity
        entrantId = getIntent().getIntExtra("entrant_id", -1);
        if (entrantId == -1) {
            Toast.makeText(this, "Error: no entrant ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        nameInput = findViewById(R.id.edit_entrant_name);
        emailInput = findViewById(R.id.edit_entrant_email);
        phoneInput = findViewById(R.id.edit_entrant_phone);
        saveButton = findViewById(R.id.button_entrant_save);
        cancelButton = findViewById(R.id.button_entrant_cancel);

        saveButton.setOnClickListener(v -> onSave());
        cancelButton.setOnClickListener(v -> onCancel());
    }

    private void onSave() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phoneStr = phoneInput.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please enter name and email", Toast.LENGTH_SHORT).show();
            return;
        }

        String accType = "user";
        Date dob = new Date();     // placeholder for now
        String gender = "";
        String city = "";
        String province = "";

        List<Boolean> notifyPrefs = new ArrayList<>();
        notifyPrefs.add(true); // all notif
        notifyPrefs.add(true); // win notif
        notifyPrefs.add(true); // lose notif

        db.addAcc(entrantId, accType, name, dob, gender,
                email, city, province, phoneStr, notifyPrefs);

        // Save session so we don't ask again
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_ROLE, accType)
                .putString(KEY_PROFILE_ID, String.valueOf(entrantId))
                .apply();

        Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();

        // Go to main entrant screen
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void onCancel() {
        // Just go back to login men no profile created
        finish();
    }
}
