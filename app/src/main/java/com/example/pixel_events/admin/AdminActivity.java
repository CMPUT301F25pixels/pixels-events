package com.example.pixel_events.admin;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.pixel_events.R;
import com.example.pixel_events.databinding.ActivityAdminBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {
    private ActivityAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        BottomNavigationView navView = binding.adminBottomNavView;

        // Safely obtain NavController from NavHostFragment to avoid timing issues
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_admin);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(navView, navController);

            // Hide overlay container when its back stack empties
            getSupportFragmentManager().addOnBackStackChangedListener(() -> {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0 && binding.overlayFragmentContainer != null) {
                    binding.overlayFragmentContainer.setVisibility(View.GONE);
                }
            });

            // Bottom navigation: clear ONLY overlay fragments, then delegate to NavigationUI
            navView.setOnItemSelectedListener(item -> {
                // Remove overlay fragments if present (they were added via add() on overlay container)
                while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStackImmediate();
                }
                if (binding.overlayFragmentContainer != null) {
                    binding.overlayFragmentContainer.setVisibility(View.GONE);
                }
                return NavigationUI.onNavDestinationSelected(item, navController);
            });

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                CharSequence title = null;
                navView.getMenu();
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
        } else {
            // If fragment not yet created, post a runnable to retry after layout pass
            binding.getRoot().post(() -> {
                NavHostFragment nhf = (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_activity_dashboard);
                if (nhf != null) {
                    NavController nc = nhf.getNavController();
                    NavigationUI.setupWithNavController(navView, nc);

                    // Hide overlay when back stack empties
                    getSupportFragmentManager().addOnBackStackChangedListener(() -> {
                        if (getSupportFragmentManager().getBackStackEntryCount() == 0 && binding.overlayFragmentContainer != null) {
                            binding.overlayFragmentContainer.setVisibility(View.GONE);
                        }
                    });

                    navView.setOnItemSelectedListener(item -> {
                        while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                            getSupportFragmentManager().popBackStackImmediate();
                        }
                        if (binding.overlayFragmentContainer != null) {
                            binding.overlayFragmentContainer.setVisibility(View.GONE);
                        }
                        return NavigationUI.onNavDestinationSelected(item, nc);
                    });

                    nc.addOnDestinationChangedListener((controller, destination, arguments) -> {
                        if (binding.adminTitle != null) {
                            CharSequence title = null;
                            if (navView != null && navView.getMenu() != null) {
                                if (navView.getMenu().findItem(destination.getId()) != null) {
                                    title = navView.getMenu().findItem(destination.getId()).getTitle();
                                }
                            }
                            if (title == null && destination.getLabel() != null) {
                                title = destination.getLabel();
                            }
                            if (title != null) {
                                binding.adminTitle.setText(title);
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
