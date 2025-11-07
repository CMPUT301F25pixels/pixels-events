package com.example.pixel_events.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.R;
import com.example.pixel_events.events.EventDetailsActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventDetailsActivityUITest {

    @Rule
    public ActivityScenarioRule<EventDetailsActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    private static Intent createIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class);
        intent.putExtra("eventId", "test_event_123");
        return intent;
    }

    @Test
    public void eventDetailsScreen_allElementsVisible() {
        // Check all text views are displayed
        onView(withId(R.id.event_title))
                .check(matches(isDisplayed()));

        onView(withId(R.id.event_location))
                .check(matches(isDisplayed()));

        onView(withId(R.id.event_dates))
                .check(matches(isDisplayed()));

        onView(withId(R.id.event_times))
                .check(matches(isDisplayed()));

        onView(withId(R.id.event_capacity))
                .check(matches(isDisplayed()));

        onView(withId(R.id.event_fee))
                .check(matches(isDisplayed()));

        onView(withId(R.id.event_description))
                .check(matches(isDisplayed()));

        // Check buttons
        onView(withId(R.id.join_button))
                .check(matches(isDisplayed()));

        onView(withId(R.id.back_button))
                .check(matches(isDisplayed()));
    }
}

