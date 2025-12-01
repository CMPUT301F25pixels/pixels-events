package com.example.pixel_events.entrantTests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.not;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.events.EventDetailedFragment;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.waitinglist.WaitingList;
import com.google.android.gms.tasks.Tasks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/*
    Tests:
        US 01.01.01: As an entrant, I want to join the waiting list for a specific event
        US 01.01.02: As an entrant, I want to leave the waiting list for a specific event
    Utilizes:
        Both Black Box and White Box testing.
*/
@RunWith(AndroidJUnit4.class)
public class WaitingListJoinLeaveTest {
    private DatabaseHandler db;
    private WaitingList wl;
    private Event evn;
    private int eventId = 1;
    private int userId = 1;

    @Before
    public void setup() throws Exception {
        DatabaseHandler.resetInstance();
        db = DatabaseHandler.getInstance(true); // emulator mode

        // Create and add a new user
        Profile user = getProfile();
        Tasks.await(db.getAccountCollection().document(String.valueOf(userId)).set(user));

        // Create a waitlist
        wl = new WaitingList(eventId, 100);
        wl.setStatus("waiting");
        Tasks.await(db.getWaitListCollection().document(String.valueOf(eventId)).set(wl));

        // Create Event and add event to DB
        Calendar cal = Calendar.getInstance();
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
        String today = fmt.format(cal.getTime());
        ArrayList<String> tags = new ArrayList<>();

        evn = new Event(eventId,
                1,
                "Test Event",
                "test.URL",
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
                tags);
        Tasks.await(db.getEventCollection().document(String.valueOf(eventId)).set(evn));
    }

    @NonNull
    private Profile getProfile() {
        Profile user = new Profile();
        List<Boolean> noti = new ArrayList<>();
        noti.add(true);
        noti.add(true);
        noti.add(true);

        user.setUserId(userId);
        user.setRole("user");
        user.setUserName("Dummy");
        user.setGender("Male");
        user.setEmail("example@gmail.com");
        user.setPhoneNum("1112223333");
        user.setPostalcode("T1T1T1");
        user.setProvince("Alberta");
        user.setCity("Edmonton");
        user.setNotify(noti);
        return user;
    }

    @Test
    public void testJoinLeaveWaitingList() throws Exception {
        // Pass the userId and eventId explicitly
        Bundle args = new Bundle();
        args.putInt("eventId", eventId);
        args.putInt("userId", userId);

        FragmentScenario<EventDetailedFragment> scenario =
                FragmentScenario.launchInContainer(
                        EventDetailedFragment.class,
                        args,
                        R.style.Theme_Pixelevents
                );
        scenario.moveToState(Lifecycle.State.RESUMED);

        // Wait for loading
        waitForText(R.id.event_joinButton, "Join", 5000);

        // Click Join Button
        onView(withId(R.id.event_joinButton)).perform(click());

        // Wait and Check if join is now disabled
        waitForEnabled(R.id.event_leaveButton, true, 5000);
        onView(withId(R.id.event_joinButton)).check(matches(not(isEnabled())));

        // Click Leave Button
        onView(withId(R.id.event_leaveButton)).perform(click());

        // Wait and check if leave is disabled
        waitForEnabled(R.id.event_joinButton, true, 5000);
        onView(withId(R.id.event_leaveButton)).check(matches(not(isEnabled())));
    }

    // Helper method to wait for text to appear on a View
    public static void waitForText(int viewId, String expectedText, long timeoutMillis) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeoutMillis;

        while (System.currentTimeMillis() < endTime) {
            try {
                onView(withId(viewId)).check(matches(withText(containsString(expectedText))));
                return;
            } catch (Throwable t) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new RuntimeException("Timed out waiting for text '" + expectedText + "' on view " + viewId);
    }

    // Helper method to wait for a View to become enabled/disabled
    public static void waitForEnabled(int viewId, boolean shouldBeEnabled, long timeoutMillis) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeoutMillis;

        while (System.currentTimeMillis() < endTime) {
            try {
                if (shouldBeEnabled) {
                    onView(withId(viewId)).check(matches(isEnabled()));
                } else {
                    onView(withId(viewId)).check(matches(not(isEnabled())));
                }
                return;
            } catch (Throwable t) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new RuntimeException("Timed out waiting for view " + viewId + " to be " + (shouldBeEnabled ? "enabled" : "disabled"));
    }

    @After
    public void cleanup() {
        db.deleteAcc(userId);
        db.deleteEvent(eventId);
        DatabaseHandler.resetInstance();
    }
}