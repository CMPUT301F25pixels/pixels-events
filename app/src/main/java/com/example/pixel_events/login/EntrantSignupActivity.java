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

        int phoneNum = 0;
        if (!phoneStr.isEmpty()) {
            try {
                phoneNum = Integer.parseInt(phoneStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Phone must be digits only", Toast.LENGTH_SHORT).show();
                return;
            }
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

        db.addAcc(entrantId, accType, name, dob, gender, email, city, province, phoneNum, notifyPrefs);

// Save session so we don't ask again
        SessionManager.startSession(this, SessionManager.ROLE_ENTRANT, entrantId);

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
