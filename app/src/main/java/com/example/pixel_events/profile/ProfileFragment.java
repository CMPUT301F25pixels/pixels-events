package com.example.pixel_events.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.MainActivity;

/**
 * ProfileFragment
 *
 * Main profile screen for users showing profile summary and navigation options.
 * Provides access to profile editing, registration history, notification preferences, and account deletion.
 * Displays username and role information.
 *
 * Implements:
 * - US 01.02.02 (Access profile update)
 * - US 01.02.03 (Access registration history)
 * - US 01.02.04 (Delete profile)
 * - US 01.04.03 (Access notification preferences)
 *
 * Collaborators:
 * - Profile: Current user data
 * - EditProfileFragment: Profile editing
 * - RegistrationHistoryFragment: Event history
 * - NotificationPreferencesFragment: Opt-out settings
 */
public class ProfileFragment extends Fragment {
    private Profile profile;
    private Button viewProfileButton, registrationHistoryButton, notificationPreferencesButton, logoutButton,
            deleteAccountButton;
    private TextView usernameTextView;
    private ImageView profileImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profile = AuthManager.getInstance().getCurrentUserProfile();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewProfileButton = view.findViewById(R.id.profile_viewprofile);
        registrationHistoryButton = view.findViewById(R.id.profile_registrationhistory);
        notificationPreferencesButton = view.findViewById(R.id.profile_notificationspreferences);
        logoutButton = view.findViewById(R.id.profile_logout);
        deleteAccountButton = view.findViewById(R.id.profile_deleteaccount);
        usernameTextView = view.findViewById(R.id.profile_username);
        profileImageView = view.findViewById(R.id.imageView);

        usernameTextView.setText(profile.getUserName());
        profileImageView.setImageResource(R.drawable.ic_launcher_foreground);

        viewProfileButton.setOnClickListener(v -> {
            replaceFragment(new ViewProfileFragment(profile));
        });

        registrationHistoryButton.setOnClickListener(v -> {
            replaceFragment(new RegistrationHistoryFragment());
        });

        notificationPreferencesButton.setOnClickListener(v -> {
            replaceFragment(new NotificationPreferencesFragment());
        });

        logoutButton.setOnClickListener(v -> {
            AuthManager.getInstance().signOut(requireContext());
            AuthManager.getInstance().setCurrentUserProfile(null);
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finishAffinity();
        });

        deleteAccountButton.setOnClickListener(v -> {
            DatabaseHandler.getInstance().deleteAcc(profile.getUserId());
            AuthManager.getInstance().signOut(requireContext());
            AuthManager.getInstance().setCurrentUserProfile(null);
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finishAffinity();
        });

    }

    private void replaceFragment(Fragment fragment) {
        int containerId;
        if (requireActivity().findViewById(R.id.nav_host_fragment_activity_dashboard) != null) {
            containerId = R.id.nav_host_fragment_activity_dashboard;
        } else if (requireActivity().findViewById(R.id.nav_host_fragment_activity_admin) != null) {
            containerId = R.id.nav_host_fragment_activity_admin;
        } else {
            // Fallback or error handling
            containerId = android.R.id.content; // Last resort
        }

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(containerId, fragment)
                .addToBackStack(null)
                .commit();
    }
}
