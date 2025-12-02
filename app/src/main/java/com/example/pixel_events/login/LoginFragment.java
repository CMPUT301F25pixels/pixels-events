package com.example.pixel_events.login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.example.pixel_events.R;
import com.example.pixel_events.admin.AdminActivity;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.databinding.FragmentLoginBinding;
import com.example.pixel_events.home.DashboardActivity;
import com.example.pixel_events.profile.Profile;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

/**
 * LoginFragment
 *
 * Fragment handling user authentication via email/password.
 * Provides device-based identification and auto-login functionality.
 * Routes users to appropriate activities based on role
 * (admin/organizer/entrant).
 *
 * Implements:
 * - US 01.07.01 (Device-based identification)
 * - User authentication for all roles
 *
 * Collaborators:
 * - AuthManager: Firebase authentication
 * - Profile: User data retrieval
 * - DatabaseHandler: Profile queries
 * - DashboardActivity, AdminActivity: Post-login navigation
 */
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final EditText usernameEditText = binding.loginUserEmail;
        final EditText codeEditText = binding.loginUserCode;
        final com.google.android.material.button.MaterialButtonToggleGroup roleToggle = binding.loginUserRole;
        final Button loginButton = binding.loginUserSave;
        final ProgressBar loadingProgressBar = view.findViewById(R.id.loading);
        final TextView signupLink = binding.loginUserSignup;

        // Auto-login check
        loadingProgressBar.setVisibility(View.VISIBLE);
        AuthManager.getInstance().autoLogin(requireContext(), () -> {
            loadingProgressBar.setVisibility(View.GONE);
            if (AuthManager.getInstance().getCurrentUserProfile() != null) {
                updateUiWithUser(AuthManager.getInstance().getCurrentUserProfile());
            }
        }, () -> {
            loadingProgressBar.setVisibility(View.GONE);
        });

        // Show/Hide code field based on role
        roleToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.login_role_organizer || checkedId == R.id.login_role_admin) {
                    codeEditText.setVisibility(View.VISIBLE);
                } else {
                    codeEditText.setVisibility(View.GONE);
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                String email = usernameEditText.getText().toString();
                if (!isEmailValid(email)) {
                    usernameEditText.setError(getString(R.string.invalid_username));
                    loginButton.setEnabled(false);
                } else {
                    loginButton.setEnabled(true);
                }
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);

                String role = "user";
                int selectedId = roleToggle.getCheckedButtonId();
                if (selectedId == R.id.login_role_organizer)
                    role = "org";
                else if (selectedId == R.id.login_role_admin)
                    role = "admin";

                // Validate code for Organizer/Admin
                if (role.equals("org") || role.equals("admin")) {
                    String code = codeEditText.getText().toString().trim();
                    if (role.equals("org") && !"ORG123".equals(code)) {
                        loadingProgressBar.setVisibility(View.GONE);
                        codeEditText.setError("Invalid Organizer Code");
                        return;
                    }
                    if (role.equals("admin") && !"ADMIN123".equals(code)) {
                        loadingProgressBar.setVisibility(View.GONE);
                        codeEditText.setError("Invalid Admin Code");
                        return;
                    }
                }

                AuthManager.getInstance().login(requireContext(), usernameEditText.getText().toString(), role,
                        () -> {
                            loadingProgressBar.setVisibility(View.GONE);
                            updateUiWithUser(AuthManager.getInstance().getCurrentUserProfile());
                        },
                        e -> {
                            loadingProgressBar.setVisibility(View.GONE);
                            showLoginFailed(e.getMessage());
                        });
            }
        });

        signupLink.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SignupFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void updateUiWithUser(Profile profile) {
        String welcome = getString(R.string.welcome) + profile.getUserName();

        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(getContext().getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        }

        // Update location in Profile if changed
        updateLocationIfChanged(profile);

        // Navigate to DashboardActivity
        if (getActivity() != null) {
            Intent intent;

            getActivity().getSupportFragmentManager().popBackStack();
            String role = profile.getRole();
            if (role.equalsIgnoreCase("admin"))
                intent = new Intent(getActivity(), AdminActivity.class);
            else
                intent = new Intent(getActivity(), DashboardActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void showLoginFailed(String errorString) {
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(
                    getContext().getApplicationContext(),
                    errorString,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @SuppressLint("MissingPermission")
    private void updateLocationIfChanged(Profile profile) {
        if (profile == null || getContext() == null)
            return;

        boolean fineGranted = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;

        if (!(fineGranted || coarseGranted)) {
            // Permission not granted; skip silently
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(requireContext());
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null)
                        return;

                    double newLat = location.getLatitude();
                    double newLng = location.getLongitude();

                    // Fetch current profile from database to compare against persisted values
                    DatabaseHandler.getInstance().getProfile(
                            profile.getUserId(),
                            dbProfile -> {
                                Double oldLat = (dbProfile != null) ? dbProfile.getLatitude() : profile.getLatitude();
                                Double oldLng = (dbProfile != null) ? dbProfile.getLongitude() : profile.getLongitude();

                                final double THRESHOLD = 0.0001; // ~11m latitude
                                boolean changed = (oldLat == null || oldLng == null)
                                        || Math.abs(newLat - oldLat) > THRESHOLD
                                        || Math.abs(newLng - oldLng) > THRESHOLD;

                                if (changed) {
                                    // Update both in-memory profile and persisted DB only if changed
                                    profile.setLatitude(newLat);
                                    profile.setLongitude(newLng);

                                    java.util.Map<String, Object> updates = new java.util.HashMap<>();
                                    updates.put("latitude", newLat);
                                    updates.put("longitude", newLng);
                                    DatabaseHandler.getInstance().modify(
                                            com.example.pixel_events.database.DatabaseHandler.getInstance()
                                                    .getAccountCollection(),
                                            profile.getUserId(),
                                            updates,
                                            err -> {
                                                if (err != null) {
                                                    android.util.Log.e("LoginFragment",
                                                            "Failed to persist location: " + err);
                                                }
                                            });

                                    Toast.makeText(requireContext(), "Location updated", Toast.LENGTH_SHORT).show();
                                }
                            },
                            e -> {
                                // On error, fallback to updating using local comparison
                                Double oldLat = profile.getLatitude();
                                Double oldLng = profile.getLongitude();
                                final double THRESHOLD = 0.0001;
                                boolean changed = (oldLat == null || oldLng == null)
                                        || Math.abs(newLat - oldLat) > THRESHOLD
                                        || Math.abs(newLng - oldLng) > THRESHOLD;
                                if (changed) {
                                    profile.setLatitude(newLat);
                                    profile.setLongitude(newLng);
                                    Toast.makeText(requireContext(), "Location updated", Toast.LENGTH_SHORT).show();
                                }
                            });
                });
    }
}
