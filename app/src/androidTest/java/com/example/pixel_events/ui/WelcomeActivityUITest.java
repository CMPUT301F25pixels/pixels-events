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
import com.example.pixel_events.login.WelcomeActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class WelcomeActivityUITest {

    @Rule
    public ActivityScenarioRule<WelcomeActivity> activityRule =
            new ActivityScenarioRule<>(WelcomeActivity.class);

    @Test
    public void welcomeScreen_allElementsVisible() {
        // Check title is displayed
        onView(withId(R.id.text_welcome_title))
                .check(matches(isDisplayed()))
                .check(matches(withText("Welcome")));

        // Check description is displayed
        onView(withId(R.id.text_description))
                .check(matches(isDisplayed()));

        // Check all buttons are displayed
        onView(withId(R.id.button_signup))
                .check(matches(isDisplayed()))
                .check(matches(withText("Sign up free")));

        onView(withId(R.id.button_google))
                .check(matches(isDisplayed()));

        onView(withId(R.id.button_apple))
                .check(matches(isDisplayed()));

        onView(withId(R.id.text_login))
                .check(matches(isDisplayed()))
                .check(matches(withText("Log in")));
    }

    @Test
    public void welcomeScreen_buttonsClickable() {
        // Test sign up button is clickable
        onView(withId(R.id.button_signup))
                .check(matches(isDisplayed()))
                .perform(click());
    }

    @Test
    public void welcomeScreen_loginLinkClickable() {
        // Test login link is clickable
        onView(withId(R.id.text_login))
                .check(matches(isDisplayed()))
                .perform(click());
    }
}

