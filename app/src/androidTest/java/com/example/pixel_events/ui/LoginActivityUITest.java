package com.example.pixel_events.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.R;
import com.example.pixel_events.login.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginActivityUITest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void loginScreen_allElementsVisible() {
        // Check title is displayed
        onView(withId(R.id.text_hello))
                .check(matches(isDisplayed()))
                .check(matches(withText("Hello!")));

        onView(withId(R.id.text_welcome_back))
                .check(matches(isDisplayed()))
                .check(matches(withText("Welcome back.")));

        // Check role label
        onView(withId(R.id.text_role_label))
                .check(matches(isDisplayed()));

        // Check all role buttons are displayed
        onView(withId(R.id.button_entrant))
                .check(matches(isDisplayed()));

        onView(withId(R.id.button_organizer))
                .check(matches(isDisplayed()));

        onView(withId(R.id.button_admin))
                .check(matches(isDisplayed()));

        // Check access code field
        onView(withId(R.id.edit_access_code))
                .check(matches(isDisplayed()));
    }

    @Test
    public void loginScreen_buttonsClickable() {
        // Test entrant button is clickable
        onView(withId(R.id.button_entrant))
                .check(matches(isDisplayed()))
                .perform(click());
    }
}

