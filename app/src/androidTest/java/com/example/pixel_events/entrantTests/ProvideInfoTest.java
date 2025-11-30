package com.example.pixel_events.entrantTests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.MainActivity;
import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.profile.Profile;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/*
    Tests:
        US 01.02.01: As an entrant, I want to provide my personal information such as name,
                     email and optional phone number in the app
    Utilized:
        Both Black Box and White Box Testing.
 */
@RunWith(AndroidJUnit4.class)
public class ProvideInfoTest {

    private static final String TEST_NAME = "Dummy";
    private static final String TEST_EMAIL = "user" + System.currentTimeMillis() + "@test.com";
    private static final String TEST_PHONE = "1112223333";
    private static final String TEST_POSTAL = "T1T1T1";
    private static final String TEST_PROVINCE = "Alberta";
    private static final String TEST_CITY = "Edmonton";

    @Before
    public void setup() {
        DatabaseHandler.resetInstance();
        DatabaseHandler.getInstance(true);
        FirebaseAuth.getInstance().signOut();
    }

    @Test
    public void testSignupFlowAndReturn() throws InterruptedException {
        // Launch Activity
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        // Go to Signup
        onView(withId(R.id.login_user_signup)).perform(click());

        // Fill Form
        onView(withId(R.id.signup_user_name)).perform(scrollTo(), typeText(TEST_NAME), closeSoftKeyboard());
        onView(withId(R.id.signup_user_email)).perform(scrollTo(), typeText(TEST_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.signup_user_phone)).perform(scrollTo(), typeText(TEST_PHONE), closeSoftKeyboard());
        onView(withId(R.id.signup_user_entrant)).perform(scrollTo(), click());
        onView(withId(R.id.signup_gender_male)).perform(scrollTo(), click());
        onView(withId(R.id.signup_user_postalcode)).perform(scrollTo(), typeText(TEST_POSTAL), closeSoftKeyboard());
        onView(withId(R.id.signup_user_province)).perform(scrollTo(), typeText(TEST_PROVINCE), closeSoftKeyboard());
        onView(withId(R.id.signup_user_city)).perform(scrollTo(), typeText(TEST_CITY), closeSoftKeyboard());

        // Save
        onView(withId(R.id.signup_user_save)).perform(scrollTo(), click());

        // Wait
        Thread.sleep(3000);

        // 4. Verify
        onView(withId(R.id.dashboard_bottom_nav_view))
                .check(matches(isDisplayed()));
        verifyUserInDatabase();

        scenario.close();
    }

    private void verifyUserInDatabase() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final Profile[] result = new Profile[1];
        final Throwable[] error = new Throwable[1];

        DatabaseHandler.getInstance().getProfileByEmail(TEST_EMAIL,
                profile -> {
                    result[0] = profile;
                    latch.countDown();
                },
                e -> {
                    error[0] = e;
                    latch.countDown();
                }
        );

        boolean success = latch.await(5, TimeUnit.SECONDS);
        if (!success) fail("Timeout waiting for database verification");
        if (error[0] != null) fail("Database error: " + error[0].getMessage());

        assertNotNull("Profile should exist in DB", result[0]);
        assertEquals("Name matches", TEST_NAME, result[0].getUserName());
    }

    @After
    public void cleanup() {
        DatabaseHandler.resetInstance();
    }
}