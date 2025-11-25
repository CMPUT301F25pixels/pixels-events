package com.example.pixel_events.home;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.pixel_events.R;
import android.view.View;
import androidx.navigation.NavController;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.fragment.NavHostFragment;

import com.example.pixel_events.databinding.ActivityDashboardBinding;
import com.example.pixel_events.events.CreateEventFragment;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.notifications.NotificationFragment;
import com.example.pixel_events.profile.Profile;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {
    private ActivityDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        BottomNavigationView navView = binding.dashboardBottomNavView;

        // Safely obtain NavController from NavHostFragment to avoid timing issues
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_dashboard);

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
                if (binding.dashboardTitle != null) {
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
                        binding.dashboardTitle.setText(title);
                    }
                }

                // Update the add button visibility for current user role
                updateButtonVisibility();
            });

            // Hook up add button click once
            if (binding.dashboardAddevent != null) {
                binding.dashboardAddevent.setOnClickListener(v -> {
                    if (binding.dashboardTitle != null) {
                        binding.dashboardTitle.setText("Create Event");
                    }
                    if (binding.overlayFragmentContainer != null) {
                        binding.overlayFragmentContainer.setVisibility(View.VISIBLE);
                    }
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.overlay_fragment_container, new CreateEventFragment())
                            .addToBackStack("overlay")
                            .commit();
                });
            }
            if (binding.dashboardShowNotifications != null){
                binding.dashboardShowNotifications.setOnClickListener(v -> {
                    if (binding.dashboardTitle != null) {
                        binding.dashboardTitle.setText("Notifications");
                    }
                    if (binding.overlayFragmentContainer != null) {
                        binding.overlayFragmentContainer.setVisibility(View.VISIBLE);
                    }
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.overlay_fragment_container, new NotificationFragment())
                            .addToBackStack("overlay")
                            .commit();
                });
            }
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
                        if (binding.dashboardTitle != null) {
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
                                binding.dashboardTitle.setText(title);
                            }
                        }
                    });
                    updateButtonVisibility();
                    if (binding.dashboardAddevent != null) {
                        binding.dashboardAddevent.setOnClickListener(v -> {
                            if (binding.dashboardTitle != null) {
                                binding.dashboardTitle.setText("Create Event");
                            }
                            if (binding.overlayFragmentContainer != null) {
                                binding.overlayFragmentContainer.setVisibility(View.VISIBLE);
                            }
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .setReorderingAllowed(true)
                                    .add(R.id.overlay_fragment_container, new CreateEventFragment())
                                    .addToBackStack("overlay")
                                    .commit();
                        });
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        if (binding == null || binding.dashboardAddevent == null)
            return;
        Profile profile = AuthManager.getInstance().getCurrentUserProfile();
        boolean isOrganizer = profile != null && "org".equalsIgnoreCase(profile.getRole());
        binding.dashboardAddevent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        binding.dashboardShowNotifications.setVisibility(isOrganizer ? View.GONE : View.VISIBLE);
    }
}