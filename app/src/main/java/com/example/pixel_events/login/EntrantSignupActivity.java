package com.example.pixel_events.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.MainActivity;
import com.example.pixel_events.R;
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
    private EditText passwordInput;
    private Button saveButton;
    private DatabaseHandler db;
    private int entrantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_signup);

        db = DatabaseHandler.getInstance();

        entrantId = getIntent().getIntExtra("entrant_id", -1);
        if (entrantId == -1) {
            Toast.makeText(this, "Error: no entrant ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        nameInput = findViewById(R.id.edit_entrant_name);
        emailInput = findViewById(R.id.edit_entrant_email);
        passwordInput = findViewById(R.id.edit_entrant_password);
        saveButton = findViewById(R.id.button_entrant_save);

        saveButton.setOnClickListener(v -> onSave());
        
        findViewById(R.id.text_signin).setOnClickListener(v -> {
            finish();
        });
    }

    private void onSave() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        String accType = "user";
        Date dob = new Date();
        String gender = "";
        String city = "";
        String province = "";
        int phoneNum = 0;

        List<Boolean> notifyPrefs = new ArrayList<>();
        notifyPrefs.add(true);
        notifyPrefs.add(true);
        notifyPrefs.add(true);

        db.addAcc(entrantId, accType, name, dob, gender,
                email, city, province, phoneNum, notifyPrefs);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_ROLE, accType)
                .putString(KEY_PROFILE_ID, String.valueOf(entrantId))
                .apply();

        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
