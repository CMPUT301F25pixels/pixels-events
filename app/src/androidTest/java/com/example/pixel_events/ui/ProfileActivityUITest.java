package com.example.pixel_events.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.R;
import com.example.pixel_events.settings.ProfileActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ProfileActivityUITest {

    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule =
            new ActivityScenarioRule<>(ProfileActivity.class);

    @Test
    public void profileScreen_allElementsVisible() {
        // Check title
        onView(withId(R.id.profileProfileText))
                .check(matches(isDisplayed()));

        // Check role and username
        onView(withId(R.id.profileRoleText))
                .check(matches(isDisplayed()));

        onView(withId(R.id.profileUsernameText))
                .check(matches(isDisplayed()));

        // Check edit button
        onView(withId(R.id.profileEditButton))
                .check(matches(isDisplayed()));

        // Check profile fields
        onView(withId(R.id.profileDOBText))
                .check(matches(isDisplayed()));

        onView(withId(R.id.profileGenderText))
                .check(matches(isDisplayed()));

        onView(withId(R.id.profileEmailText))
                .check(matches(isDisplayed()));

        onView(withId(R.id.profilePhoneText))
                .check(matches(isDisplayed()));

        onView(withId(R.id.profileCityText))
                .check(matches(isDisplayed()));

        onView(withId(R.id.profileProvinceText))
                .check(matches(isDisplayed()));
    }

    @Test
    public void profileScreen_bottomNavVisible() {
        // Check bottom navigation bar
        onView(withId(R.id.profileBottomBar))
                .check(matches(isDisplayed()));
    }
}

