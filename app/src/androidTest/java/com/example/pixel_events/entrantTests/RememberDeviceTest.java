package com.example.pixel_events.entrantTests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.pixel_events.R;
import com.example.pixel_events.home.DashboardActivity;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/*
    Tests:
        US 01.07.01: As an entrant, I want to be identified by my device, so that I don't have to
                     use a username and password.
    Utilizes:
        White Box Testing.
 */

@RunWith(AndroidJUnit4.class)
public class RememberDeviceTest {
    private static final String PREF_NAME = "PixelEventsPrefs";
    private static final String KEY_USER_ID = "logged_in_user_id";

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        Intents.init();
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        AuthManager.getInstance().signOut(context);
    }

    @Test
    public void testDeviceIdentificationAndAutoLogin() {
        // Signup
        onView(withId(R.id.login_user_signup)).perform(click());

        // Fill details
        onView(withId(R.id.signup_user_name)).perform(typeText("Device Test User"), closeSoftKeyboard());
        onView(withId(R.id.signup_user_email)).perform(typeText("device@test.com"), closeSoftKeyboard());
        onView(withId(R.id.signup_user_phone)).perform(typeText("1234567890"), closeSoftKeyboard());
        onView(withId(R.id.signup_user_postalcode)).perform(typeText("T6G2R3"), closeSoftKeyboard());
        onView(withId(R.id.signup_user_entrant)).perform(click());

        // Complete Signup
        onView(withId(R.id.signup_user_save)).perform(click());

        // Wait
        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }

        // clear the history
        Intents.release();
        Intents.init();

        // Verify internal state
        assertNotNull("AuthManager should have a current profile",
                AuthManager.getInstance().getCurrentUserProfile());

        // Verify Persistence
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int savedUserId = prefs.getInt(KEY_USER_ID, -1);

        assertNotEquals("User ID should be saved in SharedPreferences", -1, savedUserId);
        assertEquals("Saved ID should match AuthManager ID",
                AuthManager.getInstance().getCurrentUserProfile().getUserId(), savedUserId);

        // Close the Activity
        activityRule.getScenario().close();

        // 5. Relaunch the Activity
        ActivityScenario.launch(MainActivity.class);

        // Wait
        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }

        // 6. Verify we navigated to Dashboard directly
        intended(hasComponent(DashboardActivity.class.getName()));
    }

    @After
    public void tearDown() {
        Intents.release();
    }
}