package com.example.pixel_events.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button signupButton = findViewById(R.id.button_signup);
        Button googleButton = findViewById(R.id.button_google);
        Button appleButton = findViewById(R.id.button_apple);
        TextView loginText = findViewById(R.id.text_login);

        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, EntrantSignupActivity.class);
            startActivity(intent);
        });

        googleButton.setOnClickListener(v -> {
            Toast.makeText(this, "Google sign-in coming soon", Toast.LENGTH_SHORT).show();
        });

        appleButton.setOnClickListener(v -> {
            Toast.makeText(this, "Apple sign-in coming soon", Toast.LENGTH_SHORT).show();
        });

        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}

