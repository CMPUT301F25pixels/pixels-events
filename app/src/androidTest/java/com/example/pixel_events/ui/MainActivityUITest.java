package com.example.pixel_events.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.R;
import com.example.pixel_events.home.DashboardFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityUITest {

        @Test
        public void dashboard_allElementsVisible() {
                FragmentScenario.launchInContainer(DashboardFragment.class, new Bundle());

                // Filter title and controls
                onView(withId(R.id.dashboard_filters_title))
                                .check(matches(isDisplayed()))
                                .check(matches(withText("Filter events")));

                onView(withId(R.id.dashboard_chip_group))
                                .check(matches(isDisplayed()));

                // Recycler view for events list
                onView(withId(R.id.dashboard_eventRecyclerView))
                                .check(matches(isDisplayed()));

                // Apply/Clear buttons
                onView(withId(R.id.dashboard_apply_filters)).check(matches(isDisplayed()));
                onView(withId(R.id.dashboard_clear_filters)).check(matches(isDisplayed()));
        }
}
