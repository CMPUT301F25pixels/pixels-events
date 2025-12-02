package com.example.pixel_events.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.waitinglist.WaitingList;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

/**
 * SetWaitlistFragment
 *
 * Bottom sheet dialog for organizers to set maximum waiting list capacity.
 * Allows optional capacity limiting for event waiting lists.
 * Updates waitlist size in database.
 *
 * Implements:
 * - US 02.03.01 (Optionally limit waitlist size)
 *
 * Collaborators:
 * - WaitingList: Capacity setting
 * - DatabaseHandler: Persistence
 */
public class SetWaitlistFragment extends BottomSheetDialogFragment {
    private static final String ARG_EVENT_ID = "eventId";
    private TextInputEditText setWaitlistSizeField;
    private Button doneButton;
    private int eventId = -1;
    private WaitingList waitingList;

    public static SetWaitlistFragment newInstance(int eventId) {
        SetWaitlistFragment fragment = new SetWaitlistFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_set_waitlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setWaitlistSizeField = view.findViewById(R.id.inputWaitlistSize);
        doneButton = view.findViewById(R.id.inputWaitlistButtonDone);

        if (getArguments() != null) {
            eventId = getArguments().getInt(ARG_EVENT_ID, -1);
        }

        if (eventId <= 0) {
            Toast.makeText(requireContext(), "Missing event", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        // Load event and prefill
        DatabaseHandler.getInstance().getWaitingList(eventId, wl -> {
            waitingList = wl;
            if (isAdded() && waitingList != null) {
                requireActivity().runOnUiThread(() -> {
                    setWaitlistSizeField.setText(String.valueOf(waitingList.getMaxWaitlistSize()));
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

        doneButton.setOnClickListener(v -> onSave());
    }

    // Apply custom bottom sheet theme overlay (ensures dimmed background & allows styling)
    @Override
    public int getTheme() {
        return R.style.ThemeOverlay_Pixelevents_BottomSheetDialog;
    }

    private void onSave() {
        if (waitingList == null) {
            Toast.makeText(requireContext(), "Waiting List not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String waitlistSize = setWaitlistSizeField.getText() != null ? setWaitlistSizeField.getText().toString().trim() : "";

        if (waitlistSize.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a waitlist size.", Toast.LENGTH_LONG).show();
            return;
        }
        int size;
        try {
            size = Integer.parseInt(waitlistSize);
        } catch (NumberFormatException ex) {
            Toast.makeText(requireContext(), "Waitlist size must be a valid number.", Toast.LENGTH_LONG).show();
            return;
        }
        if (size <= 0) {
            Toast.makeText(requireContext(), "Waitlist size must be a positive number.", Toast.LENGTH_LONG).show();
            return;
        }
        try{
            // Save to DB via setters (they update DB internally)
            waitingList.setMaxWaitlistSize(size);

            // Notify parent (EventFragment) via FragmentResult API so it can refresh UI
            Bundle result = new Bundle();
            result.putInt("eventId", eventId);
            getParentFragmentManager().setFragmentResult("registrationUpdated", result);

            Toast.makeText(requireContext(), "Waitlist size updated", Toast.LENGTH_SHORT).show();
            dismiss();
        } catch (IllegalArgumentException ex) {
            Toast.makeText(requireContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(requireContext(), "Failed to save: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
