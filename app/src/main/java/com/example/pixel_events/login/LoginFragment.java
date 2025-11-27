package com.example.pixel_events.login;

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

import com.example.pixel_events.R;
import com.example.pixel_events.admin.AdminActivity;
import com.example.pixel_events.databinding.FragmentLoginBinding;
import com.example.pixel_events.home.DashboardActivity;
import com.example.pixel_events.profile.Profile;

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
}
