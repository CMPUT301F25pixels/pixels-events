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
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
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

    public static <T> org.hamcrest.Matcher<T> first(final org.hamcrest.Matcher<T> matcher) {
        return new org.hamcrest.BaseMatcher<T>() {
            boolean isFirst = true;

            @Override
            public boolean matches(final Object item) {
                if (isFirst && matcher.matches(item)) {
                    isFirst = false;
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(final org.hamcrest.Description description) {
                description.appendText("should return first matching item");
            }
        };
    }

    private void performOrganizerLogin() throws InterruptedException {

        onView(withId(R.id.login_role_organizer)).perform(click());
        onView(withId(R.id.login_user_email)).perform(typeText("testorganizer@test.com"), closeSoftKeyboard());

        onView(withId(R.id.login_user_save)).perform(click());
        Thread.sleep(12000);
    }



    /**
     * US 02.01.01 BB: Create event UI and QR visibility.
     */
    @Test
    public void testCreateEvent_and_viewQR() throws InterruptedException {
        performOrganizerLogin();

        onView(withId(R.id.dashboard_addevent)).check(matches(isDisplayed())).perform(click());

        onView(withId(R.id.eventFormTitle)).perform(typeText("UI Test Event"), closeSoftKeyboard());
        onView(withId(R.id.eventFormLocation)).perform(typeText("Test Location"));

        onView(withId(R.id.eventFormDescription)).perform(scrollTo(), typeText("Test Description"), closeSoftKeyboard());
        onView(withId(R.id.eventFormCapacity)).perform(typeText("100"), closeSoftKeyboard());
        onView(withId(R.id.eventFormFee)).perform(typeText("10.50"), closeSoftKeyboard());

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


        onView(withId(R.id.eventFormAdd)).perform(scrollTo(), click());


        onView(withId(R.id.event_title)).check(matches(isDisplayed()));

        onView(withId(R.id.event_qrcode_button)).check(matches(isDisplayed()));
        onView(withId(R.id.event_qrcode_button)).perform(click());

        onView(withId(R.id.qr_dialog_title)).check(matches(withText("Event QR Code")));
        onView(withId(R.id.qr_code_image)).check(matches(isDisplayed()));
        onView(withId(R.id.qr_dialog_close)).perform(click());
    }

    /**
     * US 02.04.01 / 02.04.02 BB: Upload and Update poster (UI check for component presence).
     */
    @Test
    public void testPosterUploadAndUpdate_uiElementsExist() throws InterruptedException {
        performOrganizerLogin();

        onView(withId(R.id.dashboard_addevent)).perform(click());

        // 1. Assert Upload poster button (02.04.01 BB)
        onView(withId(R.id.eventFormUploadImage)).check(matches(isDisplayed()));

        // 2. Assert Image view is present
        onView(withId(R.id.eventFormPosterImage)).check(matches(isDisplayed()));

        // 3. Simulate Edit Event Navigation and check for update button (02.04.02 BB)
    }

    /**
     * US 02.03.01 BB: Limit entrants (Waitlist full UI) - Tested from Entrant perspective.
     */
    @Test
    public void testJoinButton_waitlistFull() throws InterruptedException {
        // 1. Navigate to Dashboard as Entrant
        onView(withId(R.id.login_role_entrant)).perform(click());
        onView(withId(R.id.login_user_email)).perform(typeText("testentrant@test.com"), closeSoftKeyboard());
        onView(withId(R.id.login_user_save)).perform(click());

        Thread.sleep(12000);

        onView(withId(R.id.dashboard_eventRecyclerView)).check(matches(isDisplayed()));

        onView(first(withParent(withId(R.id.dashboard_eventRecyclerView))))
                .perform(click());

        onView(withId(R.id.event_title)).check(matches(isDisplayed()));

        onView(withId(R.id.event_joinButton))
                .check(matches(allOf(
                        withText(containsString("Waitlist full")),
                        isDisplayed(),
                        isNotClickable()
                )));
    }
}