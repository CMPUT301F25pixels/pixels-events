package com.example.pixel_events.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.utils.ImageConversion;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class CreateEventFragment extends Fragment {
    private TextInputEditText startDate, endDate, startTime, endTime, regStartDate, regEndDate;
    private EditText titleField, locationField, capacityField, descriptionField, feeField;
    private Button doneButton, cancelButton, uploadButton;
    private ImageView imageView;
    private ChipGroup tagGroup;
    private Uri filePath;
    private Bitmap bitmap;
    private ArrayList<String> selectedTags = new ArrayList<>();
    private ActivityResultLauncher<String> imagePickerLauncher;

    private boolean isEditMode = false;
    private int editEventId = -1;
    private Event existingEvent;
    private Profile profile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profile = AuthManager.getInstance().getCurrentUserProfile();

        // Read arguments instead of getIntent
        if (getArguments() != null) {
            isEditMode = getArguments().getBoolean("isEditMode", false);
            editEventId = getArguments().getInt("eventId");
        }

        titleField = view.findViewById(R.id.eventFormTitle);
        locationField = view.findViewById(R.id.eventFormLocation);
        capacityField = view.findViewById(R.id.eventFormCapacity);
        descriptionField = view.findViewById(R.id.eventFormDescription);
        feeField = view.findViewById(R.id.eventFormFee);
        startDate = view.findViewById(R.id.eventFormStartDate);
        endDate = view.findViewById(R.id.eventFormEndDate);
        startTime = view.findViewById(R.id.eventFormStartTime);
        endTime = view.findViewById(R.id.eventFormEndTime);
        regStartDate = view.findViewById(R.id.eventFormRegStartDate);
        regEndDate = view.findViewById(R.id.eventFormRegEndDate);
        doneButton = view.findViewById(R.id.eventFormAdd);
        cancelButton = view.findViewById(R.id.eventFormCancel);
        uploadButton = view.findViewById(R.id.eventFormUploadImage);
        imageView = view.findViewById(R.id.eventFormPosterImage);
        tagGroup = view.findViewById(R.id.eventFormTagGroup);

        // Register image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        filePath = uri;
                        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
                            bitmap = BitmapFactory.decodeStream(inputStream);
                            imageView.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Load existing event if in edit mode
        if (isEditMode && editEventId != -1) {
            doneButton.setText("Update Event");
            loadExistingEvent();
        }

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

        doneButton.setOnClickListener(v -> saveEvent());

        cancelButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        uploadButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    private void showDatePicker(TextInputEditText field) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> field
                        .setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        dpd.getDatePicker().setMinDate(System.currentTimeMillis());
        dpd.show();
    }

    private void showTimePicker(TextInputEditText field) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> field.setText(String.format("%02d:%02d", hourOfDay, minute)),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void loadExistingEvent() {
        DatabaseHandler db = DatabaseHandler.getInstance();
        db.getEvent(editEventId,
                event -> {
                    if (event != null) {
                        existingEvent = event;
                        requireActivity().runOnUiThread(() -> populateFormFields(event));
                    } else {
                        requireActivity().runOnUiThread(
                                () -> Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show());
                        requireActivity().finish();
                    }
                },
                error -> {
                    requireActivity().runOnUiThread(
                            () -> Toast.makeText(requireContext(), "Error loading event", Toast.LENGTH_SHORT).show());
                    requireActivity().finish();
                });
    }

    private void populateFormFields(Event event) {
        titleField.setText(event.getTitle());
        locationField.setText(event.getLocation());
        capacityField.setText(event.getCapacity());
        descriptionField.setText(event.getDescription());
        feeField.setText(event.getFee());
        startDate.setText(event.getEventStartDate());
        endDate.setText(event.getEventEndDate());
        startTime.setText(event.getEventStartTime());
        endTime.setText(event.getEventEndTime());
        regStartDate.setText(event.getRegistrationStartDate());
        regEndDate.setText(event.getRegistrationEndDate());

        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            try {
                bitmap = ImageConversion.base64ToBitmap(event.getImageUrl());
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                android.util.Log.e("CreateEventFrag", "Error loading image", e);
            }
        }

        if (event.getTags() != null) {
            selectedTags.addAll(event.getTags());
            for (String tag : event.getTags()) {
                for (int i = 0; i < tagGroup.getChildCount(); i++) {
                    Chip chip = (Chip) tagGroup.getChildAt(i);
                    if (chip.getText().toString().equals(tag)) {
                        chip.setChecked(true);
                        break;
                    }
                }
            }
        }
    }

    private void saveEvent() {
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

        if (title.isEmpty() || location.isEmpty() || capacity.isEmpty() || description.isEmpty()
                || sDate.isEmpty() || eDate.isEmpty() || sTime.isEmpty() || eTime.isEmpty()
                || rStart.isEmpty() || rEnd.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields (dates & times included)",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String imageURL = "";
        if (bitmap != null) {
            imageURL = ImageConversion.bitmapToBase64(bitmap);
        } else if (isEditMode && existingEvent != null && existingEvent.getImageUrl() != null) {
            imageURL = existingEvent.getImageUrl();
        }

        try {
            int eventId;
            int organizerId;

            if (isEditMode && existingEvent != null) {
                eventId = existingEvent.getEventId();
                organizerId = existingEvent.getOrganizerId();
            } else {
                eventId = (int) ((System.currentTimeMillis() / 1000L) % Integer.MAX_VALUE);
                organizerId = profile != null ? profile.getUserId() : 1;
            }

            Event event = new Event(eventId, organizerId, title, imageURL, location,
                    capacity, description, fee, sDate, eDate, sTime, eTime, rStart, rEnd, selectedTags);

            android.util.Log.d("CreateEventFrag",
                    (isEditMode ? "Updated" : "Created") + " event: ID=" + eventId + " Title=" + title);

            event.saveToDatabase();

            android.util.Log.d("CreateEventFrag", "Event saved to database successfully");

            Toast.makeText(requireContext(), "Event " + (isEditMode ? "updated" : "saved") + " successfully!",
                    Toast.LENGTH_SHORT).show();

            requireActivity().runOnUiThread(() -> {
                androidx.fragment.app.FragmentManager fm = requireActivity().getSupportFragmentManager();
                try {
                    fm.popBackStackImmediate();
                } catch (IllegalStateException ignored) {
                }

                androidx.fragment.app.Fragment detail = new EventDetailedFragment(eventId);
                fm.beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_dashboard, detail)
                        .addToBackStack(null)
                        .commit();
            });
        } catch (IllegalArgumentException ex) {
            android.util.Log.e("CreateEventFrag", "Validation error: " + ex.getMessage(), ex);
            Toast.makeText(requireContext(), "Invalid input: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            android.util.Log.e("CreateEventFrag", "Error saving event: " + ex.getMessage(), ex);
            Toast.makeText(requireContext(), "Error saving event: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
