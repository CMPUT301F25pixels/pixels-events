package com.example.pixel_events.entrantTests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.profile.ProfileFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

// INCOMPLETE: USES AUTH

@RunWith(AndroidJUnit4.class)
public class RegHistTest {

    @Before
    public void setup() {
        // Initialize DB in emulator mode
        DatabaseHandler.resetInstance();
        DatabaseHandler.getInstance(true);

        // 2. Setup a Dummy Profile
        // ProfileFragment calls AuthManager.getInstance().getCurrentUserProfile() in onCreateView.
        // We must ensure this returns a valid object to avoid a NullPointerException.
        Profile mockProfile = new Profile();
        mockProfile.setUserId(123);
        mockProfile.setUserName("TestNavigator");
        mockProfile.setRole("entrant");
        mockProfile.setNotify(new ArrayList<>()); // Prevent null pointer on lists if used

        // 3. Inject into AuthManager Singleton
        AuthManager.getInstance().setCurrentUserProfile(mockProfile);
    }

    @Test
    public void testRegistrationHistoryNavigation() {
        // Launch ProfileFragment in isolation
        FragmentScenario<ProfileFragment> scenario = FragmentScenario.launchInContainer(
                ProfileFragment.class,
                null,
                R.style.Theme_Pixelevents
        );
        scenario.moveToState(Lifecycle.State.RESUMED);

        // Verify we are currently on the Profile screen by checking the username text
        onView(withId(R.id.profile_username)).check(matches(withText("TestNavigator")));

        // Perform the click on the Registration History button
        onView(withId(R.id.profile_registrationhistory)).perform(click());

        /* ASSERTION:
           Check if a view specific to RegistrationHistoryFragment is displayed.

           NOTE: Since I do not have the XML for RegistrationHistoryFragment,
           you must change R.id.REPLACE_THIS... to an ID that exists ONLY
           in your RegistrationHistoryFragment (e.g., a page title or the list recycler view).
        */

        // Example: If RegistrationHistoryFragment has a TextView with id "history_title"
        // onView(withId(R.id.history_title)).check(matches(isDisplayed()));

        // If you don't have a specific ID handy yet, you can verify the Profile button is NO LONGER visible:
        // (This confirms navigation happened, though checking for the new view is better)
        // onView(withId(R.id.profile_registrationhistory)).check(matches(not(isDisplayed())));
    }

    @After
    public void tearDown() {
        AuthManager.getInstance().signOut();
        AuthManager.getInstance().setCurrentUserProfile(null);
        DatabaseHandler.resetInstance();
    }
}