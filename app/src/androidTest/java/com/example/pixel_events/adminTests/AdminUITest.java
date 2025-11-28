package com.example.pixel_events.adminTests;


import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.MainActivity;
import com.example.pixel_events.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;

/**
 * Instrumented UI tests for Admin-specific functionality (US 03.01.01, 03.02.01, 03.03.01, 03.04.01, 03.05.01, 03.06.01).
 * NOTE: These tests rely on Firebase Emulator setup and an Admin login mock/pre-condition.
 */
@RunWith(AndroidJUnit4.class)
public class AdminUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);


    private void navigateToAdminTab(String title) {
        onView(allOf(withText(title), isDisplayed())).perform(click());
    }

    /**
     * US 03.04.01 BB: Browse events.
     */
    @Test
    public void testBrowseEvents_dashboardDisplay() {
        navigateToAdminTab("Dashboard");
        onView(withId(R.id.admin_events_recycler)).check(matches(isDisplayed()));

    }

    /**
     * US 03.05.01 BB: Browse profiles and test toggle functionality.
     */
    @Test
    public void testBrowseProfiles_toggleDisplay() {
        navigateToAdminTab("Profiles"); // Navigates to AdminProfileFragment

        // 1. Assert the profile list and toggle group are visible
        onView(withId(R.id.admin_profile_selection)).check(matches(isDisplayed()));
        onView(withId(R.id.myevents_RecyclerView)).check(matches(isDisplayed()));

        // 2. Assert 'Users' button is displayed and selected by default
        onView(withId(R.id.admin_profiles_entrants))
                .check(matches(allOf(withText("Users"), isDisplayed())));

        // 3. Click 'Organizers' and assert the button is checked
        onView(withId(R.id.admin_profiles_organizers)).perform(click());
        onView(withId(R.id.admin_profiles_organizers)).check(matches(isChecked()));

        // 4. Click 'Users' back and assert the button is checked
        onView(withId(R.id.admin_profiles_entrants)).perform(click());
        onView(withId(R.id.admin_profiles_entrants)).check(matches(isChecked()));
    }

    /**
     * US 03.06.01 BB: Browse images.
     */
    @Test
    public void testBrowseImages_display() {
        navigateToAdminTab("Images");
        onView(withId(R.id.admin_images_recycler)).check(matches(isDisplayed()));


    }

    /**
     * US 03.01.01 BB: Remove events (UI action - relies on a known test event).
     */
    @Test
    public void testRemoveEvents_uiAction() {
        navigateToAdminTab("Dashboard");


        // 1. Find the delete button next to the test item and click it.
        ViewInteraction deleteButton = onView(first(withId(R.id.admin_event_delete)));
        deleteButton.perform(click());

        // 2. Assert the deletion confirmation dialog appears
        onView(withText("Delete event")).check(matches(isDisplayed()));
        onView(withText("Delete")).perform(click());

        // The event should now be removed from the list.
    }

    /**
     * US 03.03.01 BB: Remove images (UI action).
     */
    @Test
    public void testRemoveImages_uiAction() {
        navigateToAdminTab("Images");

        // Pre-condition: At least one image is visible in the list.
        // 1. Find the delete button on the first item and click it.
        onView(first(withId(R.id.admin_image_delete)))
                .perform(click());

        // The image should be cleared and the list reloaded (removing the item).
    }

    public static <T> org.hamcrest.Matcher<T> first(final org.hamcrest.Matcher<T> matcher) {
        return new org.hamcrest.BaseMatcher<T>() {
            boolean isFirst = true;

            @Override
            public boolean matches(final Object item) {
                if (isFirst && matcher.matches(item)) {
                    isFirst = false;
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(final org.hamcrest.Description description) {
                description.appendText("should return first matching item");
            }
        };
    }
}