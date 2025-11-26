package com.example.pixel_events.other;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.R;
import com.example.pixel_events.login.LoginFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginFragmentUITest {

    @Test
    public void loginScreen_allElementsVisible() {
        FragmentScenario.launchInContainer(LoginFragment.class, new Bundle(), R.style.Theme_Pixelevents);

        onView(withId(R.id.text_signin_title))
                .check(matches(isDisplayed()))
                .check(matches(withText("Sign In")));
        onView(withId(R.id.login_user_email)).check(matches(isDisplayed()));
        onView(withId(R.id.login_user_password)).check(matches(isDisplayed()));
        onView(withId(R.id.login_user_save))
                .check(matches(isDisplayed()))
                .check(matches(withText("Sign In")));
        onView(withId(R.id.login_user_signup)).check(matches(isDisplayed()));
    }
}
