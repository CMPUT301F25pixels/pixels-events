package com.example.pixel_events.adminTests;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.waitinglist.WaitingList;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.profile.Profile;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AdminDatabaseIntegrationTest {

    private DatabaseHandler db;
    private FirebaseFirestore fs;
    private static final int EVENT_ID = 2000;
    private static final int ORGANIZER_ID = 2001;
    private static final int USER_ID_1 = 2002;
    private static final int TIMEOUT_SEC = 5;

    @Before
    public void setUp() throws Exception {
        DatabaseHandler.resetInstance();
        db = DatabaseHandler.getInstance(true);
        fs = db.getFirestore();
        cleanUpTestData();
        setupTestData();
    }

    private void cleanUpTestData() {
        db.deleteEvent(EVENT_ID);
        db.deleteAcc(ORGANIZER_ID);
        db.deleteAcc(USER_ID_1);
        try {
            Tasks.await(fs.collection("EventData").document(String.valueOf(EVENT_ID)).get(), 2, TimeUnit.SECONDS);
        } catch (Exception e) { /* Ignore */ }
    }

    private void setupTestData() {
        Event testEvent = new Event(
                EVENT_ID, ORGANIZER_ID, "Event for Admin Test", "base64imageurl", "Location", 100,
                "Description", "Free", "2026-02-01", "2026-02-03", "12:00", "14:00", "2026-01-01", "2026-01-30",
                new ArrayList<>(Arrays.asList("Test")), Boolean.FALSE
        );
        db.addEvent(testEvent);

        Profile orgProfile = new Profile(ORGANIZER_ID, "org", "Organizer Admin Test", "other", "org@test.com", "123", "", "", "", Arrays.asList(true, true, true));
        Profile user1Profile = new Profile(USER_ID_1, "user", "User 1 Admin Test", "other", "user1@test.com", "456", "", "", "", Arrays.asList(true, true, true));
        db.addAcc(orgProfile);
        db.addAcc(user1Profile);

        WaitingList wl = new WaitingList(EVENT_ID);
        db.addWaitingList(wl);
        db.joinWaitingList(EVENT_ID, USER_ID_1);

        try {
            Tasks.await(fs.collection("EventData").document(String.valueOf(EVENT_ID)).get(), 2, TimeUnit.SECONDS);
        } catch (Exception e) { /* Ignore */ }
    }

    private <T> T await(Task<T> task) throws ExecutionException, InterruptedException, TimeoutException {
        return Tasks.await(task, TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    private Task<Event> getEventTask(int eventId) {
        TaskCompletionSource<Event> tcs = new TaskCompletionSource<>();
        db.getEvent(eventId, tcs::setResult, tcs::setException);
        return tcs.getTask();
    }

    private Task<Profile> getProfileTask(int userId) {
        TaskCompletionSource<Profile> tcs = new TaskCompletionSource<>();
        db.getProfile(userId, tcs::setResult, tcs::setException);
        return tcs.getTask();
    }

    private Task<WaitingList> getWaitingListTask(int eventId) {
        TaskCompletionSource<WaitingList> tcs = new TaskCompletionSource<>();
        db.getWaitingList(eventId, tcs::setResult, tcs::setException);
        return tcs.getTask();
    }

    /**
     * US 03.01.01 WB: Verifies deletion of an event also deletes its associated waitlist document.
     */
    @Test
    public void testDeleteEvent_removesEventAndWaitList() throws Exception {
        Event initialEvent = await(getEventTask(EVENT_ID));
        assertNotNull("Event should exist before deletion", initialEvent);

        db.deleteEvent(EVENT_ID);
        Thread.sleep(1000);

        Event deletedEvent = await(getEventTask(EVENT_ID));
        assertNull("Event document should be null after deletion", deletedEvent);

        WaitingList deletedWaitList = await(getWaitingListTask(EVENT_ID));
        assertNull("Waitlist document should be null after event deletion", deletedWaitList);
    }

    /**
     * US 03.02.01 WB: Verifies deleting a profile also removes their ID from all waitlists/selected lists.
     */
    @Test
    public void testDeleteAcc_removesProfileAndCleanUpWaitLists() throws Exception {
        WaitingList wlBefore = await(getWaitingListTask(EVENT_ID));
        assertTrue("User 1 should be on the waitlist before profile deletion", wlBefore.getWaitList().contains(USER_ID_1));

        db.deleteAcc(USER_ID_1);
        Thread.sleep(1000);

        Profile deletedProfile = await(getProfileTask(USER_ID_1));
        assertNull("Profile document should be null after deletion", deletedProfile);

        WaitingList wlAfter = await(getWaitingListTask(EVENT_ID));
        assertFalse("User 1 should be removed from the waitlist after profile deletion", wlAfter.getWaitList().contains(USER_ID_1));
    }

    /**
     * US 03.03.01 WB: Simulates AdminImageFragment action to clear an event poster.
     */
    @Test
    public void testRemoveImage_clearsImageUrl() throws Exception {
        Event initialEvent = await(getEventTask(EVENT_ID));
        assertTrue("Event should have an image before modification", !initialEvent.getImageUrl().isEmpty());

        Map<String, Object> updates = new HashMap<>();
        updates.put("imageUrl", "");

        db.modify(db.getEventCollection(), EVENT_ID, updates, error -> {
            assertNull("DatabaseHandler.modify should succeed", error);
        });

        Thread.sleep(1000);

        Event modifiedEvent = await(getEventTask(EVENT_ID));
        assertNotNull("Modified event should not be null", modifiedEvent);
        assertTrue("Image URL should be cleared (empty string)", modifiedEvent.getImageUrl().isEmpty());
    }

    @After
    public void tearDown() {
        cleanUpTestData();
        DatabaseHandler.resetInstance();
    }
}