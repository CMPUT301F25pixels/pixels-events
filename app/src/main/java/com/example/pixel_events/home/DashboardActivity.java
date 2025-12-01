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

import java.util.Set;

/**
 * DashboardActivity
 *
 * Main activity for entrants and organizers after login.
 * Hosts bottom navigation for Home, My Events, Profile, and Scanner.
 * Implements real-time notification listener for instant alerts.
 * Automatically logs out users when their profile is deleted by admin.
 *
 * Implements:
 * - Real-time notification delivery (US 01.04.01, 01.04.02)
 * - Auto-logout on profile deletion
 * - Navigation between main app features
 *
 * Collaborators:
 * - DashboardFragment, MyEventFragment, ProfileFragment, ScannerFragment
 * - NotificationFragment: Notification bell navigation
 * - DatabaseHandler: Real-time listeners
 * - AuthManager: Session management
 */
public class DashboardActivity extends AppCompatActivity {
    private ActivityDashboardBinding binding;
    private com.google.firebase.firestore.ListenerRegistration notificationListener;

    private final Set<Integer> bottomNavDestinations = Set.of(
            R.id.navigation_dashboard,
            R.id.navigation_myevents,
            R.id.navigation_scanner,
            R.id.navigation_profile
    );

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
                       AuthManager.getInstance().signOut(DashboardActivity.this);
                       android.content.Intent intent = new android.content.Intent(DashboardActivity.this, com.example.pixel_events.MainActivity.class);
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

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_dashboard);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(navView, navController);

            // Hide/show dashboard_layout based on destination
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destId = destination.getId();
                boolean isRoot = bottomNavDestinations.contains(destId);

                if (isRoot) {
                    binding.dashboardLayout.setVisibility(View.VISIBLE);
                } else {
                    binding.dashboardLayout.setVisibility(View.GONE);
                }

                getSupportFragmentManager().addOnBackStackChangedListener(() -> {
                    boolean hasOverlay = getSupportFragmentManager().getBackStackEntryCount() > 0;

                    if (hasOverlay) {
                        // We are on a fragment-of-fragment → hide dashboard
                        binding.dashboardLayout.setVisibility(View.GONE);
                        binding.overlayFragmentContainer.setVisibility(View.VISIBLE);
                    } else {
                        // Back to root → show dashboard
                        binding.dashboardLayout.setVisibility(View.VISIBLE);
                        binding.overlayFragmentContainer.setVisibility(View.GONE);
                    }
                });

                // Update title
                if (binding.dashboardTitle != null) {
                    CharSequence title = null;
                    if (navView.getMenu().findItem(destId) != null) {
                        title = navView.getMenu().findItem(destId).getTitle();
                    }
                    if (title == null && destination.getLabel() != null) {
                        title = destination.getLabel();
                    }
                    binding.dashboardTitle.setText(title);
                }

                updateButtonVisibility();
            });

            navView.setOnItemSelectedListener(item -> {
                // Remove overlay fragments if present
                while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStackImmediate();
                }
                if (binding.overlayFragmentContainer != null) {
                    binding.overlayFragmentContainer.setVisibility(View.GONE);
                }
                return NavigationUI.onNavDestinationSelected(item, navController);
            });

            // Add Event Button
            binding.dashboardAddevent.setOnClickListener(v -> {
                binding.dashboardLayout.setVisibility(View.GONE);

                getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .add(R.id.overlay_fragment_container, new CreateEventFragment())
                        .addToBackStack("overlay")
                        .commit();
            });

            // Notification Button
            binding.dashboardShowNotifications.setOnClickListener(v -> {
                binding.dashboardLayout.setVisibility(View.GONE);

                getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .add(R.id.overlay_fragment_container, new NotificationFragment())
                        .addToBackStack("overlay")
                        .commit();
            });

            // Overlay restore on back press
            getSupportFragmentManager().addOnBackStackChangedListener(() -> {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    binding.overlayFragmentContainer.setVisibility(View.GONE);
                    binding.dashboardLayout.setVisibility(View.VISIBLE);
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
        Profile profile = AuthManager.getInstance().getCurrentUserProfile();
        boolean isOrganizer = profile != null && "org".equalsIgnoreCase(profile.getRole());

        binding.dashboardAddevent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        binding.dashboardShowNotifications.setVisibility(isOrganizer ? View.GONE : View.VISIBLE);
    }
}