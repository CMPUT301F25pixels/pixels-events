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
            AuthManager.getInstance().signOut();
            AuthManager.getInstance().setCurrentUserProfile(null);
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finishAffinity();
        });

        deleteAccountButton.setOnClickListener(v -> {
            DatabaseHandler.getInstance().deleteAcc(profile.getUserId());
            AuthManager.getInstance().signOut();
            AuthManager.getInstance().setCurrentUserProfile(null);
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finishAffinity();
        });
    }

    private void replaceFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment_activity_dashboard, fragment)
                .addToBackStack(null)
                .commit();
    }
}
