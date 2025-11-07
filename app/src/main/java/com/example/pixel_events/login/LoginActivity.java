package com.example.pixel_events.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
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

/**
 * LoginActivity
 *
 * MVP login screen for:
 *  - Entrant (device-based, no password)
 *  - Organizer (access code)
 *  - Admin (access code)
 *
 * All roles are routed to MainActivity for now. Role + profileId are stored
 * in SharedPreferences so the app can remember who is logged in.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "pixels_prefs";
    private static final String KEY_ROLE = "current_role";
    private static final String KEY_PROFILE_ID = "current_profile_id";
    private static final String KEY_ENTRANT_ID = "entrant_profile_id";

    // MVP access codes – change these later or hook to Firestore
    private static final String ORGANIZER_CODE = "ORG123";
    private static final String ADMIN_CODE = "ADMIN123";

    private Button entrantButton;
    private Button organizerButton;
    private Button adminButton;
    private EditText accessCodeEditText;

    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = DatabaseHandler.getInstance();

        // If we already know who you are, skip login
        if (maybeSkipLogin()) {
            return;
        }

        entrantButton = findViewById(R.id.button_entrant);
        organizerButton = findViewById(R.id.button_organizer);
        adminButton = findViewById(R.id.button_admin);
        accessCodeEditText = findViewById(R.id.edit_access_code);

        entrantButton.setOnClickListener(v -> handleEntrantLogin());
        organizerButton.setOnClickListener(v -> handleOrganizerOrAdminLogin(false));
        adminButton.setOnClickListener(v -> handleOrganizerOrAdminLogin(true));
    }

    /**
     * If we already have a stored role + profileId, go straight to MainActivity.
     */
    private boolean maybeSkipLogin() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String role = prefs.getString(KEY_ROLE, null);
        String profileId = prefs.getString(KEY_PROFILE_ID, null);

        if (role == null || profileId == null) {
            return false;
        }

        // In a more complex app, you might route differently per role.
        // For MVP, everyone goes to MainActivity.
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        return true;
    }

    /**
     * Entrant login: device-based identification, no password.
     * - Use ANDROID_ID as a base
     * - Derive an int ID for Profile
     * - Check Firestore for an existing account
     * - If none, create a new Profile with accType "user"
     */
    private void handleEntrantLogin() {
        setButtonsEnabled(false);

        int entrantId = getOrCreateEntrantId();
        String userIdString = String.valueOf(entrantId);

        db.getAcc(userIdString, profile -> {
            if (profile != null) {
                // Existing entrant profile: just log in
                onLoginSuccess("user", entrantId);
            } else {
                // First-time entrant: go to signup screen
                Intent intent = new Intent(LoginActivity.this, EntrantSignupActivity.class);
                intent.putExtra("entrant_id", entrantId);
                startActivity(intent);
                finish();
            }
            setButtonsEnabled(true);
        }, e -> {
            Toast.makeText(LoginActivity.this,
                    "Error checking entrant account: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            setButtonsEnabled(true);
        });
    }

    /**
     * Organizer/Admin login using simple access codes.
     *
     * For MVP:
     *  - Organizer code: ORG123
     *  - Admin code: ADMIN123
     *
     * On success, creates or reuses a Profile with accType "org" or "admin".
     */
    private void handleOrganizerOrAdminLogin(boolean isAdmin) {
        String enteredCode = accessCodeEditText.getText().toString().trim();
        String role = isAdmin ? "admin" : "org";

        // Simple MVP validation
        if (enteredCode.isEmpty()) {
            Toast.makeText(this, "Enter an access code", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isAdmin && !enteredCode.equals(ADMIN_CODE)) {
            Toast.makeText(this, "Invalid admin code", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isAdmin && !enteredCode.equals(ORGANIZER_CODE)) {
            Toast.makeText(this, "Invalid organizer code", Toast.LENGTH_SHORT).show();
            return;
        }

        setButtonsEnabled(false);

        int profileId = isAdmin ? 200001 : 100001;
        String idString = String.valueOf(profileId);

        db.getAcc(idString, profile -> {
            if (profile != null) {
                onLoginSuccess(role, profileId);
            } else {
                createStaffProfile(profileId, role);
            }
            setButtonsEnabled(true);
        }, e -> {
            Toast.makeText(LoginActivity.this, "DB error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            setButtonsEnabled(true);
        });
    }

    /**
     * Create a simple entrant profile in Firestore and then complete login.
     */
    private void createEntrantProfile(int entrantId) {
        // Dummy data for now – can be edited later from Profile screen
        String accType = "user";
        String userName = "Entrant " + entrantId;
        Date dob = new Date(); // placeholder
        String gender = "";
        String email = "";
        String city = "";
        String province = "";
        int phoneNum = 0;

        List<Boolean> notifyPrefs = new ArrayList<>();
        // [All Notif, Win notif, Lose Notif] – default all true for MVP
        notifyPrefs.add(true);
        notifyPrefs.add(true);
        notifyPrefs.add(true);

        // This constructor automatically calls db.addAcc(...) via createProfile()
        db.addAcc(entrantId, accType, userName, dob, gender,
                email, city, province, phoneNum, notifyPrefs);

        onLoginSuccess(accType, entrantId);
    }

    /**
     * Create a simple Organizer/Admin profile in Firestore and then complete login.
     */
    private void createStaffProfile(int id, String role) {
        String name = role.equals("admin") ? "Admin User" : "Organizer User";
        Date dob = new Date();
        String gender = "";
        String email = "";
        String city = "";
        String province = "";
        int phoneNum = 0;

        List<Boolean> notifyPrefs = new ArrayList<>();
        notifyPrefs.add(true);
        notifyPrefs.add(true);
        notifyPrefs.add(true);

        db.addAcc(id, role, name, dob, gender, email, city, province, phoneNum, notifyPrefs);
        onLoginSuccess(role, id);
    }

    /**
     * Save login state and navigate to MainActivity.
     */
    private void onLoginSuccess(String role, int profileId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_ROLE, role)
                .putString(KEY_PROFILE_ID, String.valueOf(profileId))
                .apply();

        Toast.makeText(this, "Logged in as " + role, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Get or create a stable entrant ID tied to this device.
     * Uses ANDROID_ID -> int hash, cached in SharedPreferences.
     */
    private int getOrCreateEntrantId() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int existingId = prefs.getInt(KEY_ENTRANT_ID, -1);
        if (existingId != -1) {
            return existingId;
        }

        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        int newId;
        if (androidId != null) {
            newId = androidId.hashCode();
        } else {
            // Fallback: use current time; unlikely collision in this project
            newId = (int) (System.currentTimeMillis() & 0x7FFFFFFF);
        }

        prefs.edit().putInt(KEY_ENTRANT_ID, newId).apply();
        return newId;
    }

    private void setButtonsEnabled(boolean enabled) {
        entrantButton.setEnabled(enabled);
        organizerButton.setEnabled(enabled);
        adminButton.setEnabled(enabled);
        accessCodeEditText.setEnabled(enabled);
    }
}
