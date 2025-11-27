package com.example.pixel_events.entrantTests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import android.util.Log;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.events.EventDetailedFragment;
import com.example.pixel_events.login.SignupFragment;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.waitinglist.WaitingList;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/*
    Tests:
        US 01.02.01: As an entrant, I want to provide my personal information such as name, email
                     and optional phone number in the app
 */
@RunWith(AndroidJUnit4.class)
public class ProvideInfoTest {
    private DatabaseHandler db;
    private FirebaseAuth auth;
    int userID;
    private static final String TEST_NAME = "Dummy";
    private static final String TEST_EMAIL = "user" + System.currentTimeMillis() + "@example.com";;
    private static final String TEST_PASSWORD = "pass123";
    private static final String TEST_PHONE = "1112223333";
    private static final String TEST_POSTAL = "T1T1T1";
    private static final String TEST_PROVINCE = "Alberta";
    private static final String TEST_CITY = "Edmonton";
    private static final String TEST_GENDER = "male"; // Based on the ID clicked
    private static final String TEST_ROLE = "user"; // Based on the ID clicked

    @Before
    public void setup() {
        // Setup DB Emulator
        DatabaseHandler.resetInstance();
        db = DatabaseHandler.getInstance(true); // emulator mode

        // Setup Auth Emulator
        auth = FirebaseAuth.getInstance();
        try {
            auth.useEmulator("10.0.2.2", 9099);
            Log.d("DatabaseHandler", "Connected to Authenticator emulator at 10.0.2.2:8081");
        } catch (IllegalStateException e) {
            // Emulator already set, ignore
            Log.d("DatabaseHandler", "Emulator already configured");
        }
    }


    @Test
    public void enterInfoTest() throws InterruptedException {
        // Instantiate fragment
        FragmentScenario<SignupFragment> scenario =
                FragmentScenario.launchInContainer(
                        SignupFragment.class,
                        null,
                        R.style.Theme_Pixelevents
                );
        scenario.moveToState(Lifecycle.State.RESUMED);

        // Enter values in the text fields
        onView(withId(R.id.signup_user_name))
                .perform(scrollTo(), typeText(TEST_NAME), closeSoftKeyboard());
        onView(withId(R.id.signup_user_email))
                .perform(scrollTo(), typeText(TEST_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.signup_user_password))
                .perform(scrollTo(), typeText(TEST_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.signup_user_phone))
                .perform(scrollTo(), typeText(TEST_PHONE), closeSoftKeyboard());
        onView(withId(R.id.signup_user_entrant))
                .perform(scrollTo(), click());
        onView(withId(R.id.signup_gender_male))
                .perform(scrollTo(), click());
        onView(withId(R.id.signup_user_postalcode))
                .perform(scrollTo(), typeText(TEST_POSTAL), closeSoftKeyboard());
        onView(withId(R.id.signup_user_province))
                .perform(scrollTo(), typeText(TEST_PROVINCE), closeSoftKeyboard());
        onView(withId(R.id.signup_user_city))
                .perform(scrollTo(), typeText(TEST_CITY), closeSoftKeyboard());

        // Save and wait
        onView(withId(R.id.signup_user_save))
                .perform(scrollTo(), click());
        Thread.sleep(2000);

        // Check if account is made
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            fail("Auth user is null. Signup failed.");
        }
        String uid = auth.getCurrentUser().getUid();

        // Convert to int
        userID = DatabaseHandler.uidToId(uid);

        // Use latch to wait for async call
        CountDownLatch latch = new CountDownLatch(1);
        final Profile[] receivedProfile = new Profile[1]; // Container to hold result
        final Throwable[] receivedError = new Throwable[1]; // Container for errors

        // Get the account info
        DatabaseHandler.getInstance().getProfile(userID,
                p -> {
                    receivedProfile[0] = p;
                    latch.countDown(); // Release the lock
                },
                e -> {
                    receivedError[0] = e;
                    latch.countDown(); // Release the lock even on error
                }
        );

        // wait a ma of 5 seconds
        boolean success = latch.await(5, TimeUnit.SECONDS);
        if (!success) {
            fail("Timeout waiting for DatabaseHandler.getProfile callback");
        }

        // Look for DB error
        if (receivedError[0] != null) {
            receivedError[0].printStackTrace();
            fail("Database read failed: " + receivedError[0].getMessage());
        }

        // Validate values we entered
        Profile p = receivedProfile[0];
        assertNotNull("Profile object returned from DB is null! Check if userID " + userID + " matches the document ID in Firestore.", p);

        assertEquals("Name matches", TEST_NAME, p.getUserName());
        assertEquals("Email matches", TEST_EMAIL, p.getEmail());
        assertEquals("Phone matches", TEST_PHONE, p.getPhoneNum());
        assertEquals("Gender matches", TEST_GENDER, p.getGender());
        assertEquals("City matches", TEST_CITY, p.getCity());
        assertEquals("Province matches", TEST_PROVINCE, p.getProvince());
        assertEquals("Role matches", TEST_ROLE, p.getRole());
    }

    @After
    public void cleanup() {
        db.deleteAcc(userID);
        DatabaseHandler.resetInstance();
    }
}
