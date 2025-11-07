package com.example.pixel_events.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class EventActivity extends AppCompatActivity {
    private TextInputEditText startDate, endDate, startTime, endTime, regStartDate, regEndDate;
    private Button doneButton, cancelButton, uploadButton;
    private ImageView imageView;
    private ChipGroup tagGroup;
    private Uri filePath;
    private Bitmap bitmap;
    private ArrayList<String> selectedTags = new ArrayList<>();
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_form);

        startDate = findViewById(R.id.eventFormStartDate);
        endDate = findViewById(R.id.eventFormEndDate);
        startTime = findViewById(R.id.eventFormStartTime);
        endTime = findViewById(R.id.eventFormEndTime);
        regStartDate = findViewById(R.id.eventFormRegStartDate);
        regEndDate = findViewById(R.id.eventFormRegEndDate);
        doneButton = findViewById(R.id.eventFormAdd);
        cancelButton = findViewById(R.id.eventFormCancel);
        uploadButton = findViewById(R.id.eventFormUploadImage);
        imageView = findViewById(R.id.eventFormPosterImage);
        tagGroup = findViewById(R.id.eventFormTagGroup);

        startDate.setOnClickListener(v -> showDatePicker(startDate));
        endDate.setOnClickListener(v -> showDatePicker(endDate));
        startTime.setOnClickListener(v -> showTimePicker(startTime));
        endTime.setOnClickListener(v -> showTimePicker(endTime));
        regStartDate.setOnClickListener(v -> showDatePicker(regStartDate));
        regEndDate.setOnClickListener(v -> showDatePicker(regEndDate));

        tagGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            selectedTags.clear(); // Clear old selections
            for (int id : checkedIds) {
                Chip chip = group.findViewById(id);
                if (chip != null) {
                    selectedTags.add(chip.getText().toString());
                }
            }
        });

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

            String imageURL = "";
            if (bitmap != null) {
                imageURL = EventActivity.bitmapToBase64(bitmap);
            }

            try {
                Event newEvent = new Event(eventId, organizerId, title, imageURL, location,
                    capacity, description, fee, sDate, eDate, sTime, eTime, rStart, rEnd, selectedTags);

                android.util.Log.d("EventActivity", "Created event: ID=" + eventId + " Title=" + title);
                
                // Persist to database
                newEvent.saveToDatabase();
                
                android.util.Log.d("EventActivity", "Event saved to database successfully");

                Toast.makeText(EventActivity.this, "Event saved successfully!", Toast.LENGTH_SHORT).show();
                
                // Navigate to event details page
                Intent intent = new Intent(EventActivity.this, EventDetailsActivity.class);
                intent.putExtra("eventId", String.valueOf(eventId));
                startActivity(intent);
                
                // Close this activity
                finish();
            } catch (IllegalArgumentException ex) {
                // Validation from Event constructor failed
                android.util.Log.e("EventActivity", "Validation error: " + ex.getMessage(), ex);
                Toast.makeText(EventActivity.this, "Invalid input: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                android.util.Log.e("EventActivity", "Error saving event: " + ex.getMessage(), ex);
                Toast.makeText(EventActivity.this, "Error saving event: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        cancelButton.setOnClickListener(v -> {
            // Simply finish the activity and return to previous screen
            finish();
        });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    // Handle the result from the image picker
                    if (uri != null) {
                        filePath = uri;
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            bitmap = BitmapFactory.decodeStream(inputStream);
                            imageView.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(EventActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        uploadButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
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

    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); // or JPEG
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public static Bitmap base64ToBitmap(String base64Str) throws IllegalArgumentException {
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

}
