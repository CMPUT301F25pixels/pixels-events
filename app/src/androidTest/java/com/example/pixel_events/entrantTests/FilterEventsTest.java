package com.example.pixel_events.entrantTests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.equalTo;
import static java.util.regex.Pattern.matches;

import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.events.EventDetailedFragment;
import com.example.pixel_events.home.DashboardFragment;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.waitinglist.WaitingList;
import com.google.android.gms.tasks.Tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/*
    Tests
        US 01.01.04: As an entrant, I want to filter events based on my interests and availability.
*/

// INCOMPLETE: NEED TO UNDERSTAND
@RunWith(AndroidJUnit4.class)
public class FilterEventsTest {
    DatabaseHandler db;
    WaitingList wl;
    int eventID1 = 1;
    int eventID2 = 2;
    int userID = 1;
    Event evn;

    @Before
    public void setup() throws Exception {
        DatabaseHandler.resetInstance();
        db = DatabaseHandler.getInstance(true); // emulator mode

        // Create and add a new user
        List<Boolean> noti = new ArrayList<>();
        noti.add(true);
        noti.add(true);
        noti.add(true);

        Profile user = new Profile(
                userID,
                "user",
                "Dummy",
                "Male",
                "example@gmail.com",
                "1112223333",
                "T1T1T1",
                "Alberta",
                "Edmonton",
                noti);
        Tasks.await(db.getAccountCollection().document(String.valueOf(userID)).set(user));

        // Create Event 1 and add event to DB
        Calendar cal = Calendar.getInstance();
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
        String today = fmt.format(cal.getTime());
        ArrayList<String> tags1 = new ArrayList<>();
        tags1.add("Gaming");
        tags1.add("Esport");

        evn = new Event(eventID1,
                1,
                "Event One",
                "test.URL",
                "Edmonton",
                100,
                "Test Desc",
                "100",
                "2026-01-01",
                "2036-02-01",
                "09:00",
                "10:00",
                today,
                "2025-12-30",
                tags1);
        Tasks.await(db.getEventCollection().document(String.valueOf(eventID1)).set(evn));

        // Create Event 2 and add event to DB
        ArrayList<String> tags2 = new ArrayList<>();
        tags2.add("Gaming");
        tags2.add("Relaxing");

        evn = new Event(eventID2,
                1,
                "Event Twe",
                "test.URL",
                "Edmonton",
                100,
                "Test Desc",
                "100",
                "2026-02-01",
                "2036-03-01",
                "09:00",
                "10:00",
                today,
                "2025-12-30",
                tags2);
        Tasks.await(db.getEventCollection().document(String.valueOf(eventID2)).set(evn));
    }

    @Test
    public void filterTest() {
        FragmentScenario<DashboardFragment> scenario =
                FragmentScenario.launchInContainer(
                        DashboardFragment.class,
                        null,
                        R.style.Theme_Pixelevents
                );
        scenario.moveToState(Lifecycle.State.RESUMED);

        // Check if all events are visible


        // Set a date and look for changes


        // Click a filter and look for changes


    }
}
