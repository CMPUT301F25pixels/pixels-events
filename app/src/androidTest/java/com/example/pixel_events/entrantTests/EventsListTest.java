package com.example.pixel_events.entrantTests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.R;
import com.example.pixel_events.home.DashboardActivity;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


// INCOMPLETE

@RunWith(AndroidJUnit4.class)
public class EventsListTest {

    @Rule
    public ActivityScenarioRule<DashboardActivity> activityRule =
            new ActivityScenarioRule<>(DashboardActivity.class);

    @Test
    public void testEntrantCanSeeEventList() throws InterruptedException {
        // 1. INCREASED WAIT: Give Firebase 5 seconds to fetch data.
        // Emulator network connections can be slow on first launch.
        Thread.sleep(5000);

        // 2. Check List Visibility
        onView(withId(R.id.dashboard_eventRecyclerView))
                .check(matches(isDisplayed()));

        // 3. DEBUG ASSERTION: Ensure list is not empty before checking contents
        onView(withId(R.id.dashboard_eventRecyclerView))
                .check(new RecyclerViewItemCountAssertion(1));

        // 4. Check Item Content
        // Since we confirmed above that items exist, this check should now pass
        // OR give a specific error about missing sub-views.
        onView(withId(R.id.dashboard_eventRecyclerView))
                .check(matches(hasDescendant(withId(R.id.eventListTitle))))
                .check(matches(hasDescendant(withId(R.id.eventListTime))))
                .check(matches(hasDescendant(withId(R.id.eventListLocation))));

        // 5. Check Role Restrictions
        onView(withId(R.id.dashboard_addevent))
                .check(matches(not(isDisplayed())));
    }

    // Helper class to check if RecyclerView has items
    // This gives you a much clearer error message if the DB is empty
    static class RecyclerViewItemCountAssertion implements ViewAssertion {
        private final int expectedMinimumCount;

        public RecyclerViewItemCountAssertion(int expectedMinimumCount) {
            this.expectedMinimumCount = expectedMinimumCount;
        }

        @Override
        public void check(View view, NoMatchingViewException noViewFoundException) {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }

            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();

            Assert.assertNotNull("Adapter should not be null", adapter);

            // This will fail with a clear message if your DB is empty
            Assert.assertTrue(
                    "RecyclerView is empty! Add data to Firebase or check your internet connection.",
                    adapter.getItemCount() >= expectedMinimumCount
            );
        }
    }
}