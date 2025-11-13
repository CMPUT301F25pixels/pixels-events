package com.example.pixel_events.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.R;
import com.example.pixel_events.profile.ProfileFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ProfileActivityUITest {

        @Test
        public void profileScreen_allElementsVisible() {
                FragmentScenario.launchInContainer(ProfileFragment.class, new Bundle());

                onView(withId(R.id.profile_username)).check(matches(isDisplayed()));
                onView(withId(R.id.profile_viewprofile)).check(matches(isDisplayed()));
                onView(withId(R.id.profile_registrationhistory)).check(matches(isDisplayed()));
                onView(withId(R.id.profile_notificationspreferences)).check(matches(isDisplayed()));
                onView(withId(R.id.profile_logout)).check(matches(isDisplayed()));
                onView(withId(R.id.profile_deleteaccount)).check(matches(isDisplayed()));
        }
}
