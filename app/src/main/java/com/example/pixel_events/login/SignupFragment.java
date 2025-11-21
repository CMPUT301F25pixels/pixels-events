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
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.databinding.FragmentSignupBinding;
import com.example.pixel_events.profile.Profile;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Fragment for user signup
 */
public class SignupFragment extends Fragment {
    private static final String TAG = "SignupFragment";
    private FragmentSignupBinding binding;
    private FirebaseAuth auth;

    public SignupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
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
        final EditText passwordEditText = binding.signupUserPassword;
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
            String password = passwordEditText.getText().toString().trim();

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

            if (TextUtils.isEmpty(password) || password.length() < 6) {
                passwordEditText.setError("Password must be at least 6 characters");
                return;
            }

            if (selectedRoleId == View.NO_ID) {
                Toast.makeText(getContext(), "Please select a role", Toast.LENGTH_SHORT).show();
                return;
            }


            if (postalCodeEditText.getText().toString().trim().isEmpty()) {
                postalCodeEditText.setError("Postal code is required");
                return;
            }

            String role = selectedRoleId == R.id.signup_user_entrant ? "user" : "org";
            String gender = getSelectedGender(selectedGenderId);
            String postalcode = postalCodeEditText.getText().toString().trim();
            String province = provinceEditText.getText().toString().trim();
            String city = cityEditText.getText().toString().trim();

            // Create user account
            createAccount(name, email, password, role, gender, phoneNumber, postalcode, province, city);

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
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

    private void createAccount(String name, String email, String password, String role, String gender, String phone
    , String postalcode, String province, String city) {
        // Show loading state
        binding.signupUserSave.setEnabled(false);

        // Create Firebase Authentication account
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            // Create Profile and save to database
                            createUserProfile(uid, name, email, role, gender, phone, postalcode, province, city);
                        }
                    } else {
                        binding.signupUserSave.setEnabled(true);
                        Log.e(TAG, "Signup failed", task.getException());
                        Toast.makeText(getContext(),
                                "Signup failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createUserProfile(String uid, String name, String email, String role, String gender, String phone,
            String postalcode, String province, String city) {
        // Create notification preferences (all enabled by default)
        List<Boolean> notify = new ArrayList<>();
        notify.add(true); // All notifications
        notify.add(true); // Win notifications
        notify.add(true); // Lose notifications

        try {
            int userId = uid.hashCode();

            Profile profile = new Profile(
                    userId,
                    role,
                    name,
                    gender,
                    email,
                    phone,
                    postalcode != null ? postalcode : "",
                    province != null ? province : "",
                    city != null ? city : "",
                    notify);

            profile.saveToDatabase();

        } catch (Exception e) {
            binding.signupUserSave.setEnabled(true);
            Log.e(TAG, "Error creating profile", e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();

            // Delete the auth user if profile creation fails
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                user.delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Auth user deleted successfully after profile creation failure.");
                    } else {
                        Log.e(TAG, "Failed to delete auth user after profile creation failure.", task.getException());
                    }
                });
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}