package com.example.pixel_events.login;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.R;

public class ResetPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        EditText newPasswordEdit = findViewById(R.id.edit_new_password);
        EditText confirmPasswordEdit = findViewById(R.id.edit_confirm_password);
        Button resetButton = findViewById(R.id.button_reset);

        resetButton.setOnClickListener(v -> {
            String newPassword = newPasswordEdit.getText().toString();
            String confirmPassword = confirmPasswordEdit.getText().toString();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Password reset successful", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

