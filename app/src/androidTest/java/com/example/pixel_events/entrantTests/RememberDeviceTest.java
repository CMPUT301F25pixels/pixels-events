package com.example.pixel_events.entrantTests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario; // Import this
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.home.DashboardActivity;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class RememberDeviceTest {
    private static final String PREF_NAME = "PixelEventsPrefs";
    private static final String KEY_USER_ID = "logged_in_user_id";
    private static final String TEST_EMAIL = "device@test.com";

    @Before
    public void setUp() {
        Intents.init();
        DatabaseHandler.getInstance(true); // Emulator
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Clear data
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        AuthManager.getInstance().signOut(context);
    }

    @Test
    public void testRememberDevice() {
        // Signup
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withId(R.id.login_user_signup)).perform(click());
            onView(withId(R.id.signup_user_name)).perform(typeText("Device Test User"), closeSoftKeyboard());
            onView(withId(R.id.signup_user_email)).perform(typeText(TEST_EMAIL), closeSoftKeyboard());
            onView(withId(R.id.signup_user_phone)).perform(typeText("1234567890"), closeSoftKeyboard());
            onView(withId(R.id.signup_user_postalcode)).perform(typeText("T6G2R3"), closeSoftKeyboard());
            onView(withId(R.id.signup_user_entrant)).perform(click());
            onView(withId(R.id.signup_user_save)).perform(click());

            try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }

            // Verify login
            assertNotNull(AuthManager.getInstance().getCurrentUserProfile());

            // Close the app
            scenario.close();
        }

        Intents.release();
        Intents.init();

        // Verify auto login
        try (ActivityScenario<MainActivity> restartScenario = ActivityScenario.launch(MainActivity.class)) {
            try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }

            intended(hasComponent(DashboardActivity.class.getName()));
        }
    }

    @After
    public void tearDown() {
        Intents.release();
        // Cleanup created user
        try {
            CountDownLatch latch = new CountDownLatch(1);
            DatabaseHandler.getInstance().getProfileByEmail(TEST_EMAIL,
                    profile -> {
                        if (profile != null) DatabaseHandler.getInstance().deleteAcc(profile.getUserId());
                        latch.countDown();
                    },
                    e -> latch.countDown()
            );
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DatabaseHandler.resetInstance();
    }
}