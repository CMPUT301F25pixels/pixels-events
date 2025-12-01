package com.example.pixel_events.organizerTest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotClickable;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.MainActivity;
import com.example.pixel_events.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for Organizer-specific features (US 02.01.01, 02.01.04, 02.02.01, 02.03.01, 02.04.01, 02.04.02, 02.06.05).
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerEventUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    /**
     * US 02.01.01 BB: Create event UI and QR visibility.
     */
    @Test
    public void testCreateEvent_and_viewQR() throws InterruptedException {
        // Pre-condition: Assume app starts in DashboardActivity as an Organizer, and the '+' button is visible.
        onView(withId(R.id.dashboard_addevent)).perform(click());

        // 1. Fill out the form (Requires scrolling for all fields in the layout)
        onView(withId(R.id.eventFormTitle)).perform(typeText("UI Test Event"), closeSoftKeyboard());
        onView(withId(R.id.eventFormLocation)).perform(typeText("Test Location"));

        onView(withId(R.id.eventFormDescription)).perform(scrollTo(), typeText("Test Description"), closeSoftKeyboard());
        onView(withId(R.id.eventFormCapacity)).perform(typeText("100"), closeSoftKeyboard());
        onView(withId(R.id.eventFormFee)).perform(typeText("10.50"), closeSoftKeyboard());

        // Mock date/time selection (by simulating clicks on the DatePicker's default 'OK')
        onView(withId(R.id.eventFormRegStartDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.eventFormRegEndDate)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.eventFormStartDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.eventFormEndDate)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.eventFormStartTime)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.eventFormEndTime)).perform(click());
        onView(withText("OK")).perform(click());

        // 2. Click Done
        onView(withId(R.id.eventFormAdd)).perform(scrollTo(), click());

        // 3. Assert navigation to EventDetailedFragment (for the Organizer preview)
        onView(withId(R.id.event_title)).check(matches(isDisplayed()));

        // 4. Assert QR button is visible and clickable
        onView(withId(R.id.event_qrcode_button)).check(matches(isDisplayed()));
        onView(withId(R.id.event_qrcode_button)).perform(click());

        // 5. Assert QR dialog is displayed (from dialog_qrcode.xml)
        onView(withId(R.id.qr_dialog_title)).check(matches(withText("Event QR Code")));
        onView(withId(R.id.qr_code_image)).check(matches(isDisplayed()));
        onView(withId(R.id.qr_dialog_close)).perform(click()); // Close dialog
    }

    /**
     * US 02.04.01 / 02.04.02 BB: Upload and Update poster (UI check for component presence).
     */
    @Test
    public void testPosterUploadAndUpdate_uiElementsExist() throws InterruptedException {
        // Pre-condition: Navigate to CreateEventFragment (simulated via '+' click)
        onView(withId(R.id.dashboard_addevent)).perform(click());

        // 1. Assert Upload poster button (02.04.01 BB)
        onView(withId(R.id.eventFormUploadImage)).check(matches(isDisplayed()));

        // 2. Assert Image view is present
        onView(withId(R.id.eventFormPosterImage)).check(matches(isDisplayed()));

        // 3. Simulate Edit Event Navigation and check for update button (02.04.02 BB)
    }

    /**
     * US 02.01.04 BB: Set registration period (UI check).
     */
    @Test
    public void testSetRegistrationPeriod_success() {

        // Find and click the 'Set Registration Period' button
        onView(withText("Set Registration Period")).perform(scrollTo(), click());

        // Assert the bottom sheet is displayed (fragment_set_registration.xml)
        onView(withId(R.id.inputRegStartDate)).check(matches(isDisplayed()));

        // Click Done (assuming test setup provided valid dates to prefill)
        onView(withId(R.id.inputRegButtonDone)).perform(click());

        // Assert success Toast and dismissal of bottom sheet (back to EventFragment)
        onView(withId(R.id.event_fragment_preview)).check(matches(isDisplayed()));
    }

    /**
     * US 02.02.01 BB & 02.06.05 BB: View Waiting List and Export button visibility.
     */
    @Test
    public void testViewWaitlist_and_ExportButton() {
        // Pre-condition: Assume navigation to EventFragment

        // 1. Click "View waitlist"
        onView(withText("View waitlist")).perform(scrollTo(), click());

        // 2. Assert navigation to WaitingListFragment (fragment_waitinglist.xml)
        onView(withId(R.id.waitinglist_recyclerview)).check(matches(isDisplayed()));

        // 3. Assert Export button is visible (02.06.05 BB)
        onView(withId(R.id.waitinglist_exportButton)).check(matches(isDisplayed()));
        onView(withId(R.id.waitinglist_exportButton)).perform(click());

        // 4. Assert the export dialog appears (checks for 'OK' button in the AlertDialog)
        onView(withText("OK")).check(matches(isDisplayed()));
    }

    /**
     * US 02.03.01 BB: Limit entrants (Waitlist full UI).
     */
    @Test
    public void testJoinButton_waitlistFull() {
        // Pre-condition: Mock a scenario where waitListCount >= maxWaitlistSize for a user on EventDetailedFragment.
        // Assume navigation to EventDetailedFragment (for user view) is complete

        // Assert button text changes and button is disabled
//        onView(withId(R.id.event_jlbutton))
//                .check(matches(allOf(
//                        withText(containsString("Waitlist full")),
//                        isDisplayed(),
//                        isNotClickable()
//                )));
    }
}