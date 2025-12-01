package com.example.pixel_events.entrantTests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.events.EventDetailedFragment;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.waitinglist.WaitingList;
import com.google.android.gms.tasks.Tasks;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/*
    Tests:
        US 01.05.04: As an entrant, I want to know how many total entrants are on the waiting list
                     for an event.
        US 01.05.05: As an entrant, I want to be informed about the criteria or guidelines for the
                     lottery selection process.
    Utilizes:
        Black Box and White Box testing.
 */

@RunWith(AndroidJUnit4.class)
public class EntrantCountNGuideTest {
    private DatabaseHandler db;
    private Event evn;
    int eventID = 1;
    int userID = 1;
    WaitingList wl;

    @Before
    public void setup() throws Exception {
        DatabaseHandler.resetInstance();
        db = DatabaseHandler.getInstance(true); // emulator mode

        // Add a user
        List<Boolean> noti = new ArrayList<>();
        noti.add(true);
        noti.add(true);
        noti.add(true);
        Profile user = new Profile(userID, "user", "Dummy", "Male", "example@gmail.com",
                "1112223333", "T1T1T1", "Alberta", "Edmonton", noti);
        Tasks.await(db.getAccountCollection().document(String.valueOf(userID)).set(user));
        AuthManager.getInstance().setCurrentUserProfile(user);

        // Create Event and add event to DB
        Calendar cal = Calendar.getInstance();
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
        String today = fmt.format(cal.getTime());
        ArrayList<String> tags = new ArrayList<>();

        evn = new Event(eventID,
                1,
                "Test Event",
                null,
                "Edmonton",
                100,
                "Test Desc",
                "100",
                "2026-01-01",
                "2030-01-01",
                "09:00",
                "10:00",
                today, // Registration start
                "2025-12-10", // Registration end
                tag,
                Boolean.FALSE);
        Tasks.await(db.getEventCollection().document(String.valueOf(eventID)).set(evn));

        // Create a waitlist
        wl = new WaitingList(eventID, 100);
        wl.setStatus("waiting");
        Tasks.await(db.getWaitListCollection().document(String.valueOf(eventID)).set(wl));

        // Pass the userId and eventId explicitly
        Bundle args = new Bundle();
        args.putInt("eventId", eventID);
        args.putInt("userId", userID);

        FragmentScenario<EventDetailedFragment> scenario =
                FragmentScenario.launchInContainer(
                        EventDetailedFragment.class,
                        args,
                        R.style.Theme_Pixelevents
                );
        scenario.moveToState(Lifecycle.State.RESUMED);
    }

    @Test
    public void entrantCountTest() {
        // Wait for UI
        waitForView(R.id.event_joinButton, withText("Join"), 5000);

        // check initial statue (0)
        waitForView(R.id.event_waitinglistcount, withText(containsString("0 in waiting list")), 5000);

        // User joins
        onView(withId(R.id.event_joinButton)).perform(click());

        // check new state (1)
        waitForView(R.id.event_waitinglistcount, withText(containsString("1 in waiting list")), 5000);

        // user leaves
        waitForView(R.id.event_leaveButton, withText("Leave"), 5000);
        onView(withId(R.id.event_leaveButton)).perform(click());

        // Check new state (0)
        waitForView(R.id.event_waitinglistcount, withText(containsString("0 in waiting list")), 5000);
    }

    @Test
    public void lotteryGuidelineNDescTest() {
        // Wait
        waitForView(R.id.event_joinButton, withText("Join"), 5000);

        // Make sure description is displayed
        onView(withId(R.id.event_description))
                .check(matches(isDisplayed()));

        // Check if lottery guidelines are present
        onView(withId(R.id.lottery_step1))
                .check(matches(isDisplayed()));
    }

    private void waitForView(int viewId, Matcher<View> matcher, long timeout) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeout;

        while (System.currentTimeMillis() < endTime) {
            try {
                onView(withId(viewId)).check(matches(matcher));
                return; // Match found
            } catch (NoMatchingViewException | AssertionError e) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
        }
        // timeout
        onView(withId(viewId)).check(matches(matcher));
    }

    @After
    public void cleanup() {
        // 1. Clear the persistent User Profile so the next test doesn't think it's logged in
        AuthManager.getInstance().setCurrentUserProfile(null);

        // 2. Existing cleanup
        db.deleteAcc(userID);
        db.deleteEvent(eventID);
        DatabaseHandler.resetInstance();
    }
}