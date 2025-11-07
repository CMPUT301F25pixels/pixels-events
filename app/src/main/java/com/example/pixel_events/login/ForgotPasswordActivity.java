package com.example.pixel_events.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        EditText emailEdit = findViewById(R.id.edit_email);
        EditText codeEdit = findViewById(R.id.edit_verification_code);
        Button continueButton = findViewById(R.id.button_continue);

        continueButton.setOnClickListener(v -> {
            String email = emailEdit.getText().toString();
            String code = codeEdit.getText().toString();

            if (email.isEmpty() || code.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
        });
    }
}

