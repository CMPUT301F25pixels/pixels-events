package com.example.pixel_events;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.admin.AdminActivity;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.home.DashboardActivity;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.login.LoginFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DatabaseHandler.getInstance();
        Log.d(TAG, "DatabaseHandler initialized");

        // Show LoginFragment which handles auto-login
        showLoginFragment();
    }

    private void showLoginFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
    }

    private void showAdminActivity() {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
        finish(); // Close MainActivity
    }

    private void showDashboardActivity() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish(); // Close MainActivity so the user can't go back to it
    }
}
