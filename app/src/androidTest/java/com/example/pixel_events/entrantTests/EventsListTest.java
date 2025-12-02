package com.example.pixel_events.entrantTests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.home.DashboardActivity;
import com.google.android.gms.tasks.Tasks;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

/*
    Tests:
        US 01.01.03: As an entrant, I want to be able to see a list of events that I can join the
                     waiting list for.

    Utilizes:
        Black box and White box Testing.
 */

@RunWith(AndroidJUnit4.class)
public class EventsListTest {
    private final int SEED_EVENT_ID = 9999;

    @Before
    public void seedDatabase() {
        // Ensure at least one event
        DatabaseHandler db = DatabaseHandler.getInstance(true);
        Event evn = new Event(SEED_EVENT_ID, 1, "Seed Event", "url", "Loc", 100, "Desc",
                "100", "2030-01-01", "2030-01-02", "09:00", "10:00", "2027-01-01", "2027-01-02", new ArrayList<>(), Boolean.FALSE);

        try {
            Tasks.await(db.getEventCollection().document(String.valueOf(SEED_EVENT_ID)).set(evn));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEventsVisible() throws InterruptedException {
        // Launch Activity
        ActivityScenario<DashboardActivity> scenario = ActivityScenario.launch(DashboardActivity.class);

        // 1. Handle "Event Deleted" Dialog bleed-over from previous tests
        try {
            // Wait briefly for the dialog to potentially appear
            Thread.sleep(1000);
            // Attempt to click the "OK" button on the alert dialog
            onView(withText("OK")).perform(click());
        } catch (NoMatchingViewException e) {
            // If the dialog is not present (isolated run), ignore and continue
        }

        // 2. Proceed with actual test logic
        // Give the list time to load (Consider replacing sleep with IdlingResource later)
        Thread.sleep(2000);

        onView(withId(R.id.dashboard_eventRecyclerView))
                .check(matches(isDisplayed()));

        onView(withId(R.id.dashboard_eventRecyclerView))
                .check(new RecyclerViewItemCountAssertion(1));

        onView(withId(R.id.dashboard_eventRecyclerView))
                .check(matches(hasDescendant(withId(R.id.eventListTitle))))
                .check(matches(hasDescendant(withId(R.id.eventListTime))))
                .check(matches(hasDescendant(withId(R.id.eventListLocation))));

        onView(withId(R.id.dashboard_addevent))
                .check(matches(not(isDisplayed())));

        scenario.close();
    }

    static class RecyclerViewItemCountAssertion implements ViewAssertion {
        private final int expectedMinimumCount;
        public RecyclerViewItemCountAssertion(int expectedMinimumCount) {
            this.expectedMinimumCount = expectedMinimumCount;
        }
        @Override
        public void check(View view, NoMatchingViewException noViewFoundException) {
            if (noViewFoundException != null) throw noViewFoundException;
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            Assert.assertNotNull("Adapter should not be null", adapter);
            Assert.assertTrue("RecyclerView is empty!", adapter.getItemCount() >= expectedMinimumCount);
        }
    }

    @After
    public void cleanUp() {
        DatabaseHandler db = DatabaseHandler.getInstance(true);
        db.deleteEvent(SEED_EVENT_ID);
        DatabaseHandler.resetInstance();
    }
}