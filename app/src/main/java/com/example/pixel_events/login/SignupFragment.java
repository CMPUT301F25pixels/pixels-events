package com.example.pixel_events.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pixel_events.R;
import com.example.pixel_events.databinding.FragmentSignupBinding;
import com.example.pixel_events.profile.Profile;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for user signup
 */
public class SignupFragment extends Fragment {
    private static final String TAG = "SignupFragment";
    private FragmentSignupBinding binding;

    public SignupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final EditText nameEditText = binding.signupUserName;
        final EditText emailEditText = binding.signupUserEmail;
        final MaterialButtonToggleGroup roleToggle = binding.signupUserRole;
        final MaterialButtonToggleGroup genderToggle = binding.signupUserGender;
        final EditText phoneEditText = binding.signupUserPhone;
        final EditText postalCodeEditText = binding.signupUserPostalcode;
        final EditText provinceEditText = binding.signupUserProvince;
        final EditText cityEditText = binding.signupUserCity;
        final Button signupButton = binding.signupUserSave;
        final TextView signinLink = view.findViewById(R.id.signup_user_signin);

        // Set default selections
        roleToggle.check(R.id.signup_user_entrant);

        signupButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();

            int selectedRoleId = roleToggle.getCheckedButtonId();
            int selectedGenderId = genderToggle.getCheckedButtonId();
            String phoneNumber = phoneEditText.getText().toString().trim();

            // Validate inputs
            if (TextUtils.isEmpty(name)) {
                nameEditText.setError("Name is required");
                return;
            }

            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Valid email is required");
                return;
            }

            if (selectedRoleId == View.NO_ID) {
                Toast.makeText(getContext(), "Please select a role", Toast.LENGTH_SHORT).show();
                return;
            }

            String role = "user";
            if (selectedRoleId == R.id.signup_user_organizer) {
                role = "org";
            } else if (selectedRoleId == R.id.signup_user_admin) {
                role = "admin";
            }

            if (postalCodeEditText.getText().toString().trim().isEmpty()) {
                postalCodeEditText.setError("Postal code is required");
                return;
            }

            String gender = getSelectedGender(selectedGenderId);
            String postalcode = postalCodeEditText.getText().toString().trim();
            String province = provinceEditText.getText().toString().trim();
            String city = cityEditText.getText().toString().trim();

            // Create user account
            createAccount(name, email, role, gender, phoneNumber, postalcode, province, city);
        });

        // Navigate to login
        if (signinLink != null) {
            signinLink.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }
    }

    private String getSelectedGender(int selectedGenderId) {
        if (selectedGenderId == View.NO_ID) {
            return "other";
        }

        if (selectedGenderId == R.id.signup_gender_male) {
            return "male";
        } else if (selectedGenderId == R.id.signup_gender_female) {
            return "female";
        } else if (selectedGenderId == R.id.signup_gender_other) {
            return "other";
        }

        return "other";
    }

    private void createAccount(String name, String email, String role, String gender, String phone, String postalcode,
            String province, String city) {
        // Show loading state
        if (binding != null)
            binding.signupUserSave.setEnabled(false);

        // Generate a random ID for the user since we don't have Firebase UID
        int userId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        if (userId <= 0)
            userId = 1; // Ensure positive

        List<Boolean> notify = new ArrayList<>();
        notify.add(true);
        notify.add(true);
        notify.add(true);

        Profile newProfile = new Profile(userId, role, name, gender, email, phone, postalcode, province, city, notify);

        AuthManager.getInstance().signup(requireContext(), newProfile, () -> {
            Log.d(TAG, "Signup successful");
            Toast.makeText(getContext(), "Account created successfully", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }, e -> {
            Log.e(TAG, "Signup failed", e);
            Toast.makeText(getContext(), "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            if (binding != null)
                binding.signupUserSave.setEnabled(true);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
