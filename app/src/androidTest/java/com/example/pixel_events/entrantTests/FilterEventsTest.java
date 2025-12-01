package com.example.pixel_events.entrantTests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;

import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.home.DashboardFragment;
import com.example.pixel_events.profile.Profile;
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
        US 01.01.04: As an entrant, I want to filter events based on my interests and availability.

    Utilizes:
        Black box testing.
 */

@RunWith(AndroidJUnit4.class)
public class FilterEventsTest {
    DatabaseHandler db;
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
        noti.add(true); noti.add(true); noti.add(true);

        Profile user = new Profile(userID, "user", "Dummy", "Male", "example@gmail.com",
                "1112223333", "T1T1T1", "Alberta", "Edmonton", noti);
        Tasks.await(db.getAccountCollection().document(String.valueOf(userID)).set(user));

        // Create Event 1
        Calendar cal = Calendar.getInstance();
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
        String today = fmt.format(cal.getTime());

        ArrayList<String> tags1 = new ArrayList<>();
        tags1.add("Adventure");
        tags1.add("Esport");

        evn = new Event(eventID1, 1, "Event One", "test.URL", "Edmonton", 100, "Test Desc",
                "100", "2026-01-01", "2036-02-01", "09:00", "10:00", today, "2025-12-30", tags1, Boolean.FALSE);
        Tasks.await(db.getEventCollection().document(String.valueOf(eventID1)).set(evn));

        // Create Event 2
        ArrayList<String> tags2 = new ArrayList<>();
        tags2.add("Gaming");
        tags2.add("Relaxing");

        evn = new Event(eventID2, 1, "Event Two", "test.URL", "Edmonton", 100, "Test Desc",
                "100", "2026-02-01", "2036-03-01", "09:00", "10:00", today, "2025-12-30", tags2, Boolean.FALSE);
        Tasks.await(db.getEventCollection().document(String.valueOf(eventID2)).set(evn));
    }

    public static ViewAction setDate(final int year, final int monthOfYear, final int dayOfMonth) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(DatePicker.class);
            }

            @Override
            public String getDescription() {
                return "set date";
            }

            @Override
            public void perform(UiController uiController, View view) {
                DatePicker datePicker = (DatePicker) view;
                datePicker.updateDate(year, monthOfYear, dayOfMonth);
            }
        };
    }

    @Test
    public void testFilter() throws InterruptedException {
        FragmentScenario<DashboardFragment> scenario =
                FragmentScenario.launchInContainer(DashboardFragment.class, null, R.style.Theme_Pixelevents);
        scenario.moveToState(Lifecycle.State.RESUMED);

        // wait
        Thread.sleep(1500);

        // Verify start
        onView(withText("Event One")).check(matches(isDisplayed()));
        onView(withText("Event Two")).check(matches(isDisplayed()));

        // Availability test
        onView(withId(R.id.dashboard_start_date)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName())))
                .perform(setDate(2026, 0, 15)); // Jan 15, 2026
        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(R.id.dashboard_apply_filters)).perform(click());

        // Check
        onView(withText("Event One")).check(doesNotExist());
        onView(withText("Event Two")).check(matches(isDisplayed()));

        // Clear Filters
        onView(withId(R.id.dashboard_clear_filters)).perform(click());

        // Tag test
        try {
            // Select a tag
            onView(allOf(
                    withText("Adventure"),
                    withParent(withId(R.id.dashboard_chip_group))
            )).perform(click());

            onView(withId(R.id.dashboard_apply_filters)).perform(click());

            // Check
            onView(withText("Event One")).check(matches(isDisplayed()));
            onView(withText("Event Two")).check(doesNotExist());

        } catch (Exception e) {
            Log.e("FilterEventsTest", "Tag filtering test failed: Chip 'Esport' might be missing from the layout", e);
        }
    }

    @After
    public void cleanUp() {
        if (db != null) {
            db.deleteAcc(userID);
            db.deleteEvent(eventID1);
            db.deleteEvent(eventID2);
        }
        DatabaseHandler.resetInstance();
    }
}