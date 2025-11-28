package com.example.pixel_events.events;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.app.Dialog;
import android.widget.FrameLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputEditText;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.utils.Validator;

import java.util.Calendar;

public class SetRegistrationFragment extends BottomSheetDialogFragment {
    private static final String ARG_EVENT_ID = "eventId";
    private TextInputEditText regStartDateField;
    private TextInputEditText regEndDateField;
    private Button doneButton;

    private int eventId = -1;
    private Event event;

    public static SetRegistrationFragment newInstance(int eventId) {
        SetRegistrationFragment fragment = new SetRegistrationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_set_registration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        regStartDateField = view.findViewById(R.id.inputRegStartDate);
        regEndDateField = view.findViewById(R.id.inputRegEndDate);
        doneButton = view.findViewById(R.id.inputRegButtonDone);

        if (getArguments() != null) {
            eventId = getArguments().getInt(ARG_EVENT_ID, -1);
        }

        if (eventId <= 0) {
            Toast.makeText(requireContext(), "Missing event", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        // Load event and prefill
        DatabaseHandler.getInstance().getEvent(eventId, evt -> {
            event = evt;
            if (isAdded() && event != null) {
                requireActivity().runOnUiThread(() -> {
                    regStartDateField.setText(event.getRegistrationStartDate());
                    regEndDateField.setText(event.getRegistrationEndDate());
                });
            }
        }, err -> {
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to load event", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
            }
        });

        regStartDateField.setOnClickListener(v -> showDatePicker(regStartDateField));
        regEndDateField.setOnClickListener(v -> showDatePicker(regEndDateField));

        doneButton.setOnClickListener(v -> onSave());
    }

    // Apply custom bottom sheet theme overlay (ensures dimmed background & allows styling)
    @Override
    public int getTheme() {
        return R.style.ThemeOverlay_Pixelevents_BottomSheetDialog;
    }

    private void showDatePicker(TextInputEditText target) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, year, month, day) -> target.setText(String.format("%04d-%02d-%02d", year, month + 1, day)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void onSave() {
        if (event == null) {
            Toast.makeText(requireContext(), "Event not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String regStart = regStartDateField.getText() != null ? regStartDateField.getText().toString().trim() : "";
        String regEnd = regEndDateField.getText() != null ? regEndDateField.getText().toString().trim() : "";

        try{
            Validator.validateNotEmpty(regStart, "Registration Start Date");
            Validator.validateNotEmpty(regEnd, "Registration End Date");

            // Validate relations with existing event dates/times
            Validator.validateDateRelations(
                    event.getEventStartDate(),
                    event.getEventEndDate(),
                    regStart,
                    regEnd,
                    event.getEventStartTime(),
                    event.getEventEndTime()
            );

            // Save to DB via setters (they update DB internally)
            event.setRegistrationStartDate(regStart);
            event.setRegistrationEndDate(regEnd);

            // Notify parent (EventFragment) via FragmentResult API so it can refresh UI
            Bundle result = new Bundle();
            result.putInt("eventId", eventId);
            getParentFragmentManager().setFragmentResult("registrationUpdated", result);

            Toast.makeText(requireContext(), "Registration dates updated", Toast.LENGTH_SHORT).show();
            dismiss();
        } catch (IllegalArgumentException ex) {
            Toast.makeText(requireContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(requireContext(), "Failed to save: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}