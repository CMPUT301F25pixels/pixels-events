package com.example.pixel_events.other;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.R;
import com.example.pixel_events.login.SignupFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SignupFragmentUITest {

    @Test
    public void signupScreen_allElementsVisible() {
        FragmentScenario.launchInContainer(SignupFragment.class, new Bundle(), R.style.Theme_Pixelevents);

        onView(withId(R.id.text_signup_title))
                .check(matches(isDisplayed()))
                .check(matches(withText("Sign Up")));

        onView(withId(R.id.signup_user_name)).check(matches(isDisplayed()));
        onView(withId(R.id.signup_user_email)).check(matches(isDisplayed()));
        onView(withId(R.id.signup_user_role)).check(matches(isDisplayed()));

        onView(withId(R.id.signup_user_save))
                .check(matches(isDisplayed()))
                .check(matches(withText("Create account")));

        onView(withId(R.id.signup_user_signin)).check(matches(isDisplayed()));
    }

    @Test
    public void signupScreen_inputFieldsWork() {
        FragmentScenario.launchInContainer(SignupFragment.class, new Bundle(), R.style.Theme_Pixelevents);
        onView(withId(R.id.signup_user_name)).perform(typeText("Test User"));
        onView(withId(R.id.signup_user_email)).perform(typeText("test@example.com"));
    }

    @Test
    public void signupScreen_createButtonClickable() {
        FragmentScenario.launchInContainer(SignupFragment.class, new Bundle(), R.style.Theme_Pixelevents);
        onView(withId(R.id.signup_user_save))
                .check(matches(isDisplayed()))
                .perform(click());
    }
}
