package com.example.pixel_events.organizerTest;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.waitinglist.WaitingList;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


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

    @Before
    public void setUp() throws Exception {
        DatabaseHandler.resetInstance();
        db = DatabaseHandler.getInstance(true);
        cleanUpTestData();
        addWaitingListToDb(1);
    }

    @After
    public void tearDown() {
        cleanUpTestData();
        DatabaseHandler.resetInstance();
    }


    /**
     * Helper to await a Task<Void> result.
     */
    private void awaitVoid(Task<Void> task) throws InterruptedException, ExecutionException, TimeoutException {
        Tasks.await(task, TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    /**
     * Helper to wrap the asynchronous db.getWaitingList call into a synchronous Task.
     */
    private Task<WaitingList> getWaitingListTask(int eventId) {
        TaskCompletionSource<WaitingList> tcs = new TaskCompletionSource<>();
        db.getWaitingList(eventId, tcs::setResult, tcs::setException);
        return tcs.getTask();
    }

    /**
     * Helper to execute and await the Task<WaitingList> result.
     */
    private WaitingList awaitTask(Task<WaitingList> task) throws InterruptedException, ExecutionException, TimeoutException {
        Tasks.await(task, TIMEOUT_SEC, TimeUnit.SECONDS);
        return task.getResult();
    }



    private void cleanUpTestData() {
        db.deleteEvent(EVENT_ID_CAPACITY_TEST);
    }

    private void addWaitingListToDb(int maxWaitlistSize) throws Exception {
        WaitingList wl = new WaitingList(EVENT_ID_CAPACITY_TEST, maxWaitlistSize);
        db.addWaitingList(wl);

        awaitVoid(db.joinWaitingList(EVENT_ID_CAPACITY_TEST, USER_1));
        awaitVoid(db.leaveWaitingList(EVENT_ID_CAPACITY_TEST, USER_1));

        WaitingList initialWl = awaitTask(getWaitingListTask(EVENT_ID_CAPACITY_TEST));
        initialWl.setMaxWaitlistSize(maxWaitlistSize);
        Thread.sleep(1000);
    }

    /**
     * US 02.03.01 WB: Test adding a second user when capacity is 1.
     */
    @Test
    public void testAddEntrantInWaitList_fullCapacity() throws Exception {

        WaitingList wl = awaitTask(getWaitingListTask(EVENT_ID_CAPACITY_TEST));

        // 1. Add first user (fills capacity 1)
        // FIX: Explicit assignment resolves "No candidates found" error
//        Task<Void> add1Task = wl.addEntrantInWaitList(USER_1);
//        awaitVoid(add1Task);
        assertTrue("User 1 should be in the waitlist", wl.isUserInWaitlist(USER_1));

        // 2. Attempt to add second user (should throw IllegalArgumentException)
        try {
            // This method throws IllegalArgumentException synchronously if the list is full.
//            wl.addEntrantInWaitList(USER_2);

            fail("Adding a second user should have failed due to full capacity.");
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            assertTrue("Exception message should indicate full capacity", cause.getMessage().contains("Waitlist is full"));
        }

        assertFalse("User 2 should NOT be in the waitlist", wl.isUserInWaitlist(USER_2));

        // 3. Cleanup
        // FIX: Explicit assignment resolves "No candidates found" error
//        Task<Void> removeTask = wl.removeEntrantInWaitList(USER_1);
//        awaitVoid(removeTask);
    }
}