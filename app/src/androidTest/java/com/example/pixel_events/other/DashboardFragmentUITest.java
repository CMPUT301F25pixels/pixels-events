package com.example.pixel_events.other;

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

/*
    Tests:
        US 01.01.03: As an entrant, I want to be able to see a list of events that I can join the
                     waiting list for.
        US 01.01.04: As an entrant, I want to filter events based on my interests and availability.

    Utilizes:
        Black Box Testing.
 */
@RunWith(AndroidJUnit4.class)
public class DashboardFragmentUITest {

    @Test
    public void dashboard_allElementsVisible() {
        FragmentScenario.launchInContainer(DashboardFragment.class, new Bundle(), R.style.Theme_Pixelevents);

        onView(withId(R.id.dashboard_filters_title))
                .check(matches(isDisplayed()))
                .check(matches(withText("Filter events")));

        onView(withId(R.id.dashboard_chip_group)).check(matches(isDisplayed()));
        onView(withId(R.id.dashboard_eventRecyclerView)).check(matches(isDisplayed()));
        onView(withId(R.id.dashboard_apply_filters)).check(matches(isDisplayed()));
        onView(withId(R.id.dashboard_clear_filters)).check(matches(isDisplayed()));
    }
}