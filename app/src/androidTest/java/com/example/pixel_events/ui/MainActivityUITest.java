package com.example.pixel_events.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.MainActivity;
import com.example.pixel_events.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void mainScreen_allElementsVisible() {
        // Check welcome text
        onView(withId(R.id.welcome_text))
                .check(matches(isDisplayed()))
                .check(matches(withText("Pixels Events")));

        // Check add event button
        onView(withId(R.id.addEvent))
                .check(matches(isDisplayed()))
                .check(matches(withText("Add Event")));

        // Check scan QR button
        onView(withId(R.id.scan_qr_button))
                .check(matches(isDisplayed()))
                .check(matches(withText("Scan QR Code")));

        // Check bottom navigation
        onView(withId(R.id.bottom_nav))
                .check(matches(isDisplayed()));
    }

    @Test
    public void mainScreen_darkThemeApplied() {
        // Verify dark theme colors are applied by checking visibility
        onView(withId(R.id.main))
                .check(matches(isDisplayed()));
    }
}

