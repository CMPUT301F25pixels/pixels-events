package com.example.pixel_events.entrantTests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.containsString;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

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

import java.nio.file.Watchable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/*
    Tests:
        US 01.01.01 As an entrant, I want to join the waiting list for a specific event
        US 01.01.02 As an entrant, I want to leave the waiting list for a specific event
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
                "100",
                "Test Desc",
                "100",
                "2026-01-01",
                "2030-01-01",
                "09:00",
                "10:00",
                today,
                "2025-12-30",
                tags);
        Tasks.await(db.getEventCollection().document(String.valueOf(eventId)).set(evn));
        }

    @NonNull
    private Profile getProfile() {
        Profile user = new Profile();
        List<Integer> eventsU = new ArrayList<>();
        List<Integer> eventsP = new ArrayList<>();
        List<Integer> eventsN = new ArrayList<>();
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
        user.setEventsUpcoming(eventsU);
        user.setEventsNPart(eventsP);
        user.setEventsNPart(eventsN);
        user.setNotify(noti);
        return user;
    }

    /*
    Tests:
        US 01.01.01 As an entrant, I want to join the waiting list for a specific event
        US 01.01.02 As an entrant, I want to leave the waiting list for a specific event
     */
    @Test
    public void joinLeaveWaitingList() throws Exception {
        // SETUP: Pass the userId explicitly
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

        // 1. Wait for "Loading..." to turn into "Join" (Max wait 5 seconds)
        waitForText(R.id.event_jlbutton, "Join", 5000);

        // 2. Click Join
        onView(withId(R.id.event_jlbutton)).perform(click());

        // 3. Wait for database update: "Join" should turn into "Leave"
        waitForText(R.id.event_jlbutton, "Leave", 5000);

        // 4. Click Leave
        onView(withId(R.id.event_jlbutton)).perform(click());

        // 5. Wait for database update: "Leave" should turn into "Join"
        waitForText(R.id.event_jlbutton, "Join", 5000);
    }

    // Helper method to wait for text to appear on a View
    public static void waitForText(int viewId, String expectedText, long timeoutMillis) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeoutMillis;

        while (System.currentTimeMillis() < endTime) {
            try {
                // Check if the view has the expected text
                onView(withId(viewId)).check(matches(withText(containsString(expectedText))));
                return; // Success!
            } catch (Throwable t) {
                // Failure means the text isn't there yet.
                // We sleep a tiny bit and try again.
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // If we reach here, we timed out. Throw the error to fail the test.
        throw new RuntimeException("Timed out waiting for text '" + expectedText + "' on view " + viewId);
    }


    @After
    public void cleanup() {
        db.deleteAcc(userId);
        db.deleteEvent(eventId);
        DatabaseHandler.resetInstance();
    }

}

