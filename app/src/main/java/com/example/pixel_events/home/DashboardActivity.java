package com.example.pixel_events.home;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.pixel_events.R;
import android.view.View;
import androidx.navigation.NavController;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.fragment.NavHostFragment;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.databinding.ActivityDashboardBinding;
import com.example.pixel_events.events.CreateEventFragment;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.notifications.NotificationFragment;
import com.example.pixel_events.profile.Profile;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.pixel_events.notifications.Notification;
import com.google.firebase.firestore.DocumentChange;
import androidx.appcompat.app.AlertDialog;

public class DashboardActivity extends AppCompatActivity {
    private ActivityDashboardBinding binding;
    private com.google.firebase.firestore.ListenerRegistration notificationListener;

    @Override
    protected void onResume() {
        super.onResume();
        setupNotificationListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (notificationListener != null) {
            notificationListener.remove();
            notificationListener = null;
        }
    }

    private void setupNotificationListener() {
        Profile user = AuthManager.getInstance().getCurrentUserProfile();
        if (user != null) {
            notificationListener = DatabaseHandler.getInstance().getAccountCollection()
                .document(String.valueOf(user.getUserId()))
                .collection("Notifications")
                .whereEqualTo("read", false) // Only listen to unread
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        android.util.Log.w("Dashboard", "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Notification n = dc.getDocument().toObject(Notification.class);
                                showNotificationAlert(n, user.getUserId());
                            }
                        }
                    }
                });
        }
    }

    private void showNotificationAlert(Notification n, int userId) {
        if (n == null) return;
        
        // Check if it's a deletion notification
        boolean isDeletion = "ADMIN_DELETE".equals(n.getType()) && 
                            (n.getMessage().contains("profile has been deleted") || 
                             n.getMessage().contains("Your profile"));
        
        new AlertDialog.Builder(this)
               .setTitle(n.getTitle())
               .setMessage(n.getMessage())
               .setPositiveButton("OK", (dialog, id) -> {
                   DatabaseHandler.getInstance().markNotificationRead(userId, n.getNotificationId());
                   
                   // If profile was deleted, log out the user
                   if (isDeletion) {
                       AuthManager.getInstance().signOut();
                       android.content.Intent intent = new android.content.Intent(this, com.example.pixel_events.MainActivity.class);
                       intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                       startActivity(intent);
                       finish();
                   }
                   dialog.dismiss();
               })
               .setCancelable(false)
               .show();
    }

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