package com.example.pixel_events.admin;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.pixel_events.R;
import com.example.pixel_events.databinding.ActivityAdminBinding;
import com.example.pixel_events.notifications.AdminNotificationLogFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Set;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        BottomNavigationView navView = binding.adminBottomNavView;

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(
                        R.id.nav_host_fragment_activity_admin
                );

        if (navHostFragment == null) return;

        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navView, navController);

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            boolean hasOverlayFragments = getSupportFragmentManager().getBackStackEntryCount() > 0;

            if (hasOverlayFragments) {
                // fragment → fragment of fragment
                binding.adminLayout.setVisibility(View.GONE);
                binding.overlayFragmentContainer.setVisibility(View.VISIBLE);
            } else {
                // returned to bottom nav destination
                binding.adminLayout.setVisibility(View.VISIBLE);
                binding.overlayFragmentContainer.setVisibility(View.GONE);
            }
        });

        navView.setOnItemSelectedListener(item -> {

            // clear all overlay fragments
            while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStackImmediate();
            }

            // restore main layout
            binding.adminLayout.setVisibility(View.VISIBLE);
            binding.overlayFragmentContainer.setVisibility(View.GONE);

            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            CharSequence title = null;
            if (navView.getMenu().findItem(destination.getId()) != null) {
                title = navView.getMenu().findItem(destination.getId()).getTitle();
            }
            if (title == null && destination.getLabel() != null) {
                title = destination.getLabel();
            }
            if (title != null) {
                binding.adminTitle.setText(title);
            }
        });

        binding.adminNotificationLogsBtn.setOnClickListener(v -> {
            binding.overlayFragmentContainer.setVisibility(View.VISIBLE);
            binding.adminLayout.setVisibility(View.GONE);

            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.overlay_fragment_container, new AdminNotificationLogFragment())
                    .addToBackStack("overlay")
                    .commit();
        });
    }
}
