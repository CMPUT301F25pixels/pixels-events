package com.example.pixel_events.other;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.waitinglist.WaitingList;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class WaitListIntegrationTest {

    private static final int EVENT_ID = 1762386210;
    private static final int USER_1 = 1001;
    private static final int USER_2 = 1002;
    private static final int TIMEOUT_SEC = 70;

    private DatabaseHandler db;
    private FirebaseFirestore fs;

    @Before
    public void setUp() throws Exception {
        db = DatabaseHandler.getInstance(false); // emulator mode
        WaitingList testWaitingList = new WaitingList(EVENT_ID, 10);
        fs = db.getFirestore();

        // Ensure the document exists and starts empty
        db.addWaitingList(testWaitingList);
        await(db.leaveWaitingList(EVENT_ID, USER_1));
        await(db.leaveWaitingList(EVENT_ID, USER_2));
    }

    // --- 🔹 Test 1: Correct user added and count increments ---
    @Test
    public void join_addsCorrectUser_andIncrementsCount() throws Exception {
        await(db.joinWaitingList(EVENT_ID, USER_1));

        List<Integer> waitList = getWaitListInts();

        assertNotNull("waitList should not be null", waitList);
        assertTrue("waitList should contain USER_1", waitList.contains(USER_1));
        assertEquals("List size should be 1", 1, waitList.size());
    }

    // --- 🔹 Test 2: Second user also added, count updates correctly ---
    @Test
    public void join_multipleUsers_incrementsCorrectly() throws Exception {
        await(db.joinWaitingList(EVENT_ID, USER_1));
        await(db.joinWaitingList(EVENT_ID, USER_2));

        List<Integer> waitList = getWaitListInts();

        assertNotNull(waitList);
        assertTrue(waitList.contains(USER_1));
        assertTrue(waitList.contains(USER_2));
        assertEquals("Count should equal 2", 2, waitList.size());
    }

    // --- 🔹 Test 3: Leaving removes correct user and decrements count ---
    @Test
    public void leave_removesCorrectUser_andDecrementsCount() throws Exception {
        await(db.joinWaitingList(EVENT_ID, USER_1));
        await(db.joinWaitingList(EVENT_ID, USER_2));
        await(db.leaveWaitingList(EVENT_ID, USER_1));

        List<Integer> waitList = getWaitListInts();

        assertNotNull(waitList);
        assertFalse("USER_1 should be removed", waitList.contains(USER_1));
        assertTrue("USER_2 should remain", waitList.contains(USER_2));
        assertEquals("Count should be 1", 1, waitList.size());
    }

    // --- 🔹 Test 4: Duplicate joins should not increase count ---
    @Test
    public void join_duplicateUser_doesNotIncrementCount() throws Exception {
        await(db.joinWaitingList(EVENT_ID, USER_1));
        await(db.joinWaitingList(EVENT_ID, USER_1)); // duplicate join

        List<Integer> waitList = getWaitListInts();

        assertNotNull(waitList);
        assertEquals("Duplicate join should not increase list size", 1, waitList.size());
    }

    // --- 🔹 Test 5: Leaving non-existent user does not change anything ---
    @Test
    public void leave_nonexistentUser_doesNothing() throws Exception {
        await(db.joinWaitingList(EVENT_ID, USER_1));
        await(db.leaveWaitingList(EVENT_ID, 999));

        List<Integer> waitList = getWaitListInts();

        assertNotNull(waitList);
        assertTrue(waitList.contains(USER_1));
        assertEquals("Count should remain 1", 1, waitList.size());
    }

    // --- Helper to await Firebase Tasks ---
    private <T> T await(Task<T> task) throws Exception {
        return Tasks.await(task, TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    // Helper to coerce Firestore numeric array values to Integer
    private List<Integer> getWaitListInts() throws Exception {
        var snap = await(fs.collection("WaitListData").document(String.valueOf(EVENT_ID)).get());
        java.util.List<?> raw = (java.util.List<?>) snap.get("waitList");
        java.util.ArrayList<Integer> ints = new java.util.ArrayList<>();
        if (raw != null) {
            for (Object o : raw) {
                if (o instanceof Number) {
                    ints.add(((Number) o).intValue());
                } else if (o instanceof String) {
                    try { ints.add(Integer.parseInt((String) o)); } catch (NumberFormatException ignored) {}
                }
            }
        }
        return ints;
    }

}
