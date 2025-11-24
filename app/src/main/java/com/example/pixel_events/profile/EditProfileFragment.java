package com.example.pixel_events.profile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.login.AuthManager;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {
    private ImageButton backButton;
    private EditText nameInput, phoneInput, emailInput;
    private EditText postalInput, provinceInput, cityInput;
    private Button saveButton;
    private MaterialButtonToggleGroup roleToggle;
    private MaterialButtonToggleGroup genderToggle;
    private Profile profile;

    public EditProfileFragment(Profile profile) {
        this.profile = profile;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        backButton = view.findViewById(R.id.editprofile_backbutton);
        nameInput = view.findViewById(R.id.editprofile_user_name);
        phoneInput = view.findViewById(R.id.editprofile_user_phone);
        emailInput = view.findViewById(R.id.editprofile_user_email);
        postalInput = view.findViewById(R.id.editprofile_user_postalcode);
        provinceInput = view.findViewById(R.id.editprofile_user_province);
        cityInput = view.findViewById(R.id.editprofile_user_city);
        roleToggle = view.findViewById(R.id.editprofile_user_role);
        genderToggle = view.findViewById(R.id.editprofile_user_gender);
        saveButton = view.findViewById(R.id.editprofile_user_save);

        // Prefill with current profile values
        if (profile != null) {
            if (profile.getUserName() != null) {
                nameInput.setText(profile.getUserName());
            }
            if (profile.getPhoneNum() != null) {
                phoneInput.setText(profile.getPhoneNum());
            }
            if (profile.getEmail() != null) {
                emailInput.setText(profile.getEmail());
            }
            if (profile.getPostalcode() != null) {
                postalInput.setText(profile.getPostalcode());
            }
            if (profile.getProvince() != null) {
                provinceInput.setText(profile.getProvince());
            }
            if (profile.getCity() != null) {
                cityInput.setText(profile.getCity());
            }

            // Initialize role toggle from profile
            if (profile.getRole() != null) {
                String role = profile.getRole();
                if ("org".equalsIgnoreCase(role) || "organizer".equalsIgnoreCase(role)) {
                    roleToggle.check(R.id.editprofile_user_organizer);
                } else {
                    roleToggle.check(R.id.editprofile_user_entrant);
                }
            }

            // Initialize gender toggle from profile
            if (profile.getGender() != null) {
                String gender = profile.getGender();
                if ("male".equalsIgnoreCase(gender)) {
                    genderToggle.check(R.id.editprofile_gender_male);
                } else if ("female".equalsIgnoreCase(gender)) {
                    genderToggle.check(R.id.editprofile_gender_female);
                } else {
                    genderToggle.check(R.id.editprofile_gender_other);
                }
            }
        }

        backButton.setOnClickListener(v -> showDiscardDialog());

        // handle system back to confirm discard
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        showDiscardDialog();
                    }
                });

        saveButton.setOnClickListener(v -> saveChanges());

        return view;
    }

    private void showDiscardDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Discard changes?")
                .setMessage("Are you sure you want to discard your changes?")
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes",
                        (dialog, which) -> requireActivity().getSupportFragmentManager().popBackStack())
                .show();
    }

    private void saveChanges() {
        if (profile == null) {
            Toast.makeText(getContext(), "Profile not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String newName = nameInput.getText().toString().trim();
        String newPhone = phoneInput.getText().toString().trim();
        String newPostal = postalInput != null ? postalInput.getText().toString().trim() : "";
        String newProvince = provinceInput != null ? provinceInput.getText().toString().trim() : "";
        String newCity = cityInput != null ? cityInput.getText().toString().trim() : "";
        String email = emailInput.getText().toString().trim();
        int selectedRoleId = roleToggle != null ? roleToggle.getCheckedButtonId() : View.NO_ID;
        int selectedGenderId = genderToggle != null ? genderToggle.getCheckedButtonId() : View.NO_ID;
        String role = resolveRole(selectedRoleId, profile.getRole());
        String gender = resolveGender(selectedGenderId, profile.getGender());

        Map<String, Object> updates = new HashMap<>();
        if (!newName.isEmpty() && !newName.equals(profile.getUserName())) {
            updates.put("userName", newName);
        }
        if (!email.isEmpty() && !email.equals(profile.getEmail())) {
            updates.put("email", email);
            AuthManager.getInstance().setCurrentUserEmail(email);
        }

        if (!newPhone.isEmpty() && !newPhone.equals(profile.getPhoneNum())) {
            updates.put("phoneNum", newPhone);
        }

        if (!gender.isEmpty() && !gender.equals(profile.getGender())) {
            updates.put("gender", gender);
        }

        if (!role.isEmpty() && !role.equals(profile.getRole())) {
            updates.put("role", role);
        }

        if (newPostal != null && !newPostal.equals(profile.getPostalcode())) {
            updates.put("postalcode", newPostal);
        }

        if (newProvince != null && !newProvince.equals(profile.getProvince())) {
            updates.put("province", newProvince);
        }

        if (newCity != null && !newCity.equals(profile.getCity())) {
            updates.put("city", newCity);
        }

        if (updates.isEmpty()) {
            // Nothing to save
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        DatabaseHandler.getInstance().modify(DatabaseHandler.getInstance().getAccountCollection(),
                profile.getUserId(), updates, error -> {
                    if (error == null) {
                        // apply changes locally
                        if (updates.containsKey("userName"))
                            profile.setUserName(newName);
                        if (updates.containsKey("phoneNum"))
                            profile.setPhoneNum(newPhone);
                        if (updates.containsKey("email"))
                            profile.setEmail(email);
                        if (updates.containsKey("gender"))
                            profile.setGender(gender);
                        if (updates.containsKey("role"))
                            profile.setRole(role);
                        if (updates.containsKey("postalcode"))
                            profile.setPostalcode(newPostal);
                        if (updates.containsKey("province"))
                            profile.setProvince(newProvince);
                        if (updates.containsKey("city"))
                            profile.setCity(newCity);
                        AuthManager.getInstance().setCurrentUserProfile(profile);
                        requireActivity().runOnUiThread(
                                () -> Toast.makeText(getContext(), "Profile saved", Toast.LENGTH_SHORT).show());
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        requireActivity().runOnUiThread(() -> Toast
                                .makeText(getContext(), "Failed to save: " + error, Toast.LENGTH_LONG).show());
                    }
                });
    }

    private String resolveRole(int selectedRoleId, @Nullable String fallback) {
        if (selectedRoleId == R.id.editprofile_user_entrant)
            return "user";
        if (selectedRoleId == R.id.editprofile_user_organizer)
            return "org";
        return fallback != null ? fallback : "user";
    }

    private String resolveGender(int selectedGenderId, @Nullable String fallback) {
        if (selectedGenderId == R.id.editprofile_gender_male)
            return "male";
        if (selectedGenderId == R.id.editprofile_gender_female)
            return "female";
        if (selectedGenderId == R.id.editprofile_gender_other)
            return "other";
        return fallback != null ? fallback : "other";
    }
}
