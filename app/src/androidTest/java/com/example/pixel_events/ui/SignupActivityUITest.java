package com.example.pixel_events.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.R;
import com.example.pixel_events.login.EntrantSignupActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SignupActivityUITest {

    @Rule
    public ActivityScenarioRule<EntrantSignupActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    private static Intent createIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EntrantSignupActivity.class);
        intent.putExtra("entrant_id", 12345);
        return intent;
    }

    @Test
    public void signupScreen_allElementsVisible() {
        // Check title
        onView(withId(R.id.text_signup_title))
                .check(matches(isDisplayed()))
                .check(matches(withText("Sign Up")));

        // Check all input fields are displayed
        onView(withId(R.id.edit_entrant_name))
                .check(matches(isDisplayed()));

        onView(withId(R.id.edit_entrant_email))
                .check(matches(isDisplayed()));

        onView(withId(R.id.edit_entrant_password))
                .check(matches(isDisplayed()));

        // Check button
        onView(withId(R.id.button_entrant_save))
                .check(matches(isDisplayed()))
                .check(matches(withText("Create account")));

        // Check sign in link
        onView(withId(R.id.text_signin))
                .check(matches(isDisplayed()));
    }

    @Test
    public void signupScreen_inputFieldsWork() {
        // Test typing into name field
        onView(withId(R.id.edit_entrant_name))
                .perform(typeText("Test User"));

        // Test typing into email field
        onView(withId(R.id.edit_entrant_email))
                .perform(typeText("test@example.com"));
    }

    @Test
    public void signupScreen_createButtonClickable() {
        onView(withId(R.id.button_entrant_save))
                .check(matches(isDisplayed()))
                .perform(click());
    }
}

