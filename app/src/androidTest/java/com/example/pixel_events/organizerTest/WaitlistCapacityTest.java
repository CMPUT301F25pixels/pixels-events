package com.example.pixel_events.organizerTest;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.waitinglist.WaitingList;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;


import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class WaitlistCapacityTest {

    private static final int EVENT_ID_CAPACITY_TEST = 1762386211;
    private static final int USER_1 = 1101;
    private static final int USER_2 = 1102;
    private static final int TIMEOUT_SEC = 5;

    private DatabaseHandler db;
    private FirebaseFirestore fs;

    @Before
    public void setUp() throws Exception {
        DatabaseHandler.resetInstance();
        db = DatabaseHandler.getInstance(true);
        fs = db.getFirestore();
        cleanUpTestData();
        setupTestData();
    }

    @After
    public void tearDown() {
        cleanUpTestData();
        DatabaseHandler.resetInstance();
    }



    private void awaitVoid(Task<Void> task) throws InterruptedException, ExecutionException, TimeoutException {
        Tasks.await(task, TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    private Task<WaitingList> getWaitingListTask(int eventId) {
        TaskCompletionSource<WaitingList> tcs = new TaskCompletionSource<>();
        db.getWaitingList(eventId, tcs::setResult, tcs::setException);
        return tcs.getTask();
    }


    private WaitingList awaitTask(Task<WaitingList> task) throws InterruptedException, ExecutionException, TimeoutException {
        Tasks.await(task, TIMEOUT_SEC, TimeUnit.SECONDS);
        return task.getResult();
    }


    private void cleanUpTestData() {
        db.deleteEvent(EVENT_ID_CAPACITY_TEST);
        try {
            Tasks.await(fs.collection("WaitListData").document(String.valueOf(EVENT_ID_CAPACITY_TEST)).delete(), 2, TimeUnit.SECONDS);
        } catch (Exception e) { /* Ignore */ }
    }

    private void setupTestData() throws Exception {
        int maxWaitlistSize = 1;
        WaitingList wl = new WaitingList(EVENT_ID_CAPACITY_TEST, maxWaitlistSize);
        db.addWaitingList(wl);

        Tasks.await(fs.collection("WaitListData").document(String.valueOf(EVENT_ID_CAPACITY_TEST)).get(), TIMEOUT_SEC, TimeUnit.SECONDS);

        awaitVoid(db.joinWaitingList(EVENT_ID_CAPACITY_TEST, USER_1));
        awaitVoid(db.leaveWaitingList(EVENT_ID_CAPACITY_TEST, USER_1));

        Map<String, Object> updates = new HashMap<>();
        updates.put("maxWaitlistSize", maxWaitlistSize);

        TaskCompletionSource<Void> updateTcs = new TaskCompletionSource<>();
        CollectionReference waitListCollection = fs.collection("WaitListData");

        db.modify(waitListCollection, EVENT_ID_CAPACITY_TEST, updates, error -> {
            if (error == null) {
                updateTcs.setResult(null);
            } else {
                updateTcs.setException(new Exception(error));
            }
        });
        awaitVoid(updateTcs.getTask());
    }

    /**
     * US 02.03.01 WB: Test adding a second user when capacity is 1.
     */
    @Test
    public void testAddEntrantInWaitList_fullCapacity() throws Exception {

        // 1. Add first user (fills capacity 1)
        awaitVoid(db.joinWaitingList(EVENT_ID_CAPACITY_TEST, USER_1));

        // Re-fetch to ensure local object is up-to-date for assertion
        WaitingList wlAfter1 = awaitTask(getWaitingListTask(EVENT_ID_CAPACITY_TEST));
        assertTrue("User 1 should be in the waitlist", wlAfter1.isUserInWaitlist(USER_1));

        // 2. Attempt to add second user (should fail due to full capacity)
        try {
            awaitVoid(db.joinWaitingList(EVENT_ID_CAPACITY_TEST, USER_2));

            // If no exception is thrown, the test failed its goal.
            fail("Adding a second user should have failed due to full capacity.");
        } catch (ExecutionException e) {
            // Success path: An ExecutionException was thrown by the Firebase Task.
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            // Assert that the exception message indicates full capacity (or a similar constraint violation)
            assertTrue("Exception message should indicate full capacity or constraint violation", cause.getMessage().contains("Waitlist is full") || cause.getMessage().contains("violated"));
        } catch (InterruptedException | TimeoutException e) {
            // Re-throw other await exceptions
            throw e;
        }

        // 3. Verify user 2 is NOT in the waitlist by re-fetching.
        WaitingList wlAfterFailure = awaitTask(getWaitingListTask(EVENT_ID_CAPACITY_TEST));
        assertFalse("User 2 should NOT be in the waitlist", wlAfterFailure.isUserInWaitlist(USER_2));

        // Cleanup after test
        awaitVoid(db.leaveWaitingList(EVENT_ID_CAPACITY_TEST, USER_1));
    }
}