package com.example.pixel_events.adminTests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;

import android.os.Bundle;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.R;
import com.example.pixel_events.admin.AdminDashboardFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminUITest {

    @Test
    public void adminDashboard_listElementsVisible() {
        FragmentScenario.launchInContainer(AdminDashboardFragment.class, new Bundle(), R.style.Theme_Pixelevents);

        onView(withId(R.id.admin_events_recycler))
                .check(matches(isDisplayed()));

        onView(withId(R.id.admin_events_progress))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));


        onView(withId(R.id.admin_events_empty))
                .check(matches(withText("No events found")))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
    }
}