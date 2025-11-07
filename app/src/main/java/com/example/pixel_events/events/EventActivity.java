package com.example.pixel_events.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.widget.Toast;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.MainActivity;
import com.example.pixel_events.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class EventActivity extends AppCompatActivity {
    private TextInputEditText startDate, endDate, startTime, endTime, regStartDate, regEndDate;
    private Button doneButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventform);

        startDate = findViewById(R.id.eventFormStartDate);
        endDate = findViewById(R.id.eventFormEndDate);
        startTime = findViewById(R.id.eventFormStartTime);
        endTime = findViewById(R.id.eventFormEndTime);
        regStartDate = findViewById(R.id.eventFormRegStartDate);
        regEndDate = findViewById(R.id.eventFormRegEndDate);
        doneButton = findViewById(R.id.eventFormAdd);
        cancelButton = findViewById(R.id.eventFormCancel);


        startDate.setOnClickListener(v -> showDatePicker(startDate));
        endDate.setOnClickListener(v -> showDatePicker(endDate));
        startTime.setOnClickListener(v -> showTimePicker(startTime));
        endTime.setOnClickListener(v -> showTimePicker(endTime));
        regStartDate.setOnClickListener(v -> showDatePicker(regStartDate));
        regEndDate.setOnClickListener(v -> showDatePicker(regEndDate));

        doneButton.setOnClickListener(v -> {
            // Extract details from form fields
            TextInputEditText titleField = findViewById(R.id.eventFormTitle);
            TextInputEditText locationField = findViewById(R.id.eventFormLocation);
            TextInputEditText capacityField = findViewById(R.id.eventFormCapacity);
            TextInputEditText descriptionField = findViewById(R.id.eventFormDescription);
            TextInputEditText feeField = findViewById(R.id.eventFormFee);

            String title = titleField.getText() != null ? titleField.getText().toString().trim() : "";
            String location = locationField.getText() != null ? locationField.getText().toString().trim() : "";
            String capacity = capacityField.getText() != null ? capacityField.getText().toString().trim() : "";
            String description = descriptionField.getText() != null ? descriptionField.getText().toString().trim() : "";
            String fee = feeField.getText() != null ? feeField.getText().toString().trim() : "";

            String sDate = startDate.getText() != null ? startDate.getText().toString().trim() : "";
            String eDate = endDate.getText() != null ? endDate.getText().toString().trim() : "";
            String sTime = startTime.getText() != null ? startTime.getText().toString().trim() : "";
            String eTime = endTime.getText() != null ? endTime.getText().toString().trim() : "";
            String rStart = regStartDate.getText() != null ? regStartDate.getText().toString().trim() : "";
            String rEnd = regEndDate.getText() != null ? regEndDate.getText().toString().trim() : "";

            // Basic validation
            if (title.isEmpty() || location.isEmpty() || capacity.isEmpty() || description.isEmpty()
                    || sDate.isEmpty() || eDate.isEmpty() || sTime.isEmpty() || eTime.isEmpty()
                    || rStart.isEmpty() || rEnd.isEmpty()) {
                Toast.makeText(EventActivity.this, "Please fill all required fields (dates & times included)", Toast.LENGTH_LONG).show();
                return;
            }

            // Generate a simple positive event ID (seconds since epoch truncated)
            int eventId = (int) ((System.currentTimeMillis() / 1000L) % Integer.MAX_VALUE);
            // Placeholder organizer id — replace with real user id when available
            int organizerId = 1;

            // imageUrl not collected here; use empty string
            String imageUrl = "";

            try {
                Event newEvent = new Event(eventId, organizerId, title, imageUrl, location,
                    capacity, description, fee, sDate, eDate, sTime, eTime, rStart, rEnd);

                // Persist to database
                newEvent.saveToDatabase();

                Toast.makeText(EventActivity.this, "Event saved", Toast.LENGTH_SHORT).show();
                // Close activity and return
                finish();
            } catch (IllegalArgumentException ex) {
                // Validation from Event constructor failed
                Toast.makeText(EventActivity.this, "Invalid input: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                Toast.makeText(EventActivity.this, "Error saving event: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        cancelButton.setOnClickListener(v -> {
            // Simply finish the activity and return to previous screen
            finish();
        });
    }

    private void showDatePicker(TextInputEditText field) {
    Calendar c = Calendar.getInstance();
    DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) ->
        // Use ISO format yyyy-MM-dd to match Event tests and parsing
        field.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)),
        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

    // Disallow selecting today or earlier: set min date to tomorrow
    long oneDay = 24L * 60L * 60L * 1000L;
    dpd.getDatePicker().setMinDate(System.currentTimeMillis() + oneDay);
    dpd.show();
    }

    private void showTimePicker(TextInputEditText field) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) ->
                field.setText(String.format("%02d:%02d", hourOfDay, minute)),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }
}
