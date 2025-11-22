package com.example.pixel_events.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pixel_events.R;

import java.util.Objects;

public class ViewProfileFragment extends Fragment {
    private Button editButton;
    private ImageButton backButton;
    private TextView profileText, roleText, usernameText, genderText, emailText, phoneText;
    private TextView postalText, provinceText, cityText;
    private Profile profile;
    boolean edit = true;

    public ViewProfileFragment(Profile profile) {
        this.profile = profile;
    }

    public ViewProfileFragment(Profile profile, boolean edit) {
        this.profile = profile;
        this.edit = edit;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editButton = view.findViewById(R.id.profileEditButton);
        backButton = view.findViewById(R.id.profileBackButton);

        profileText = view.findViewById(R.id.profileProfileText);
        roleText = view.findViewById(R.id.profileRoleText);
        usernameText = view.findViewById(R.id.profileUsernameText);
        genderText = view.findViewById(R.id.profileGenderText);
        emailText = view.findViewById(R.id.profileEmailText);
        phoneText = view.findViewById(R.id.profilePhoneText);
        postalText = view.findViewById(R.id.profilePostalText);
        provinceText = view.findViewById(R.id.profileProvinceText);
        cityText = view.findViewById(R.id.profileCityText);

        this.editButton.setVisibility(this.edit ? View.VISIBLE : View.INVISIBLE);

        profileText.setText("Profile Details");
        roleText.setText(profile.getRole());
        usernameText.setText(profile.getUserName());
        if (!Objects.equals(profile.getGender(), "")) genderText.setText(profile.getGender());
        emailText.setText(profile.getEmail());
        if (!Objects.equals(profile.getPhoneNum(), "")) phoneText.setText(profile.getPhoneNum());
        postalText.setText(profile.getPostalcode());
        if (!Objects.equals(profile.getProvince(), "")) provinceText.setText(profile.getProvince());
        if (!Objects.equals(profile.getCity(), "")) cityText.setText(profile.getCity());

        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        editButton.setOnClickListener(v -> {
            replaceFragment(new EditProfileFragment(profile));
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
