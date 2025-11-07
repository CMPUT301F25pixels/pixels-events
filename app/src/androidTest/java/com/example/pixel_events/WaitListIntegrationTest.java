package com.example.pixel_events;

import static org.junit.Assert.*;

import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.database.DatabaseHandler;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class WaitListIntegrationTest {

    private static final String EVENT_ID = "1762386210";
    private static final String USER_1   = "testUser_A";
    private static final String USER_2   = "testUser_B";
    private static final int TIMEOUT_SEC = 70;

    private DatabaseHandler db;
    private FirebaseFirestore fs;

    @Before
    public void setUp() throws Exception {
        db = DatabaseHandler.getInstance(true); // emulator mode
        fs = db.getFirestore();

        // Ensure the document exists and starts empty
        await(db.addWaitingList(EVENT_ID, 10));
        await(db.leaveWaitingList(EVENT_ID, USER_1));
        await(db.leaveWaitingList(EVENT_ID, USER_2));
    }

    // --- 🔹 Test 1: Correct user added and count increments ---
    @Test
    public void join_addsCorrectUser_andIncrementsCount() throws Exception {
        await(db.joinWaitingList(EVENT_ID, USER_1));

        DocumentSnapshot snap = await(fs.collection("WaitListData").document(EVENT_ID).get());
        List<String> waitList = (List<String>) snap.get("waitList");
        Long count = snap.getLong("waitlistCount");

        assertNotNull("waitList should not be null", waitList);
        assertTrue("waitList should contain USER_1", waitList.contains(USER_1));
        assertEquals("waitlistCount should be 1", Long.valueOf(waitList.size()), count);
    }

    // --- 🔹 Test 2: Second user also added, count updates correctly ---
    @Test
    public void join_multipleUsers_incrementsCorrectly() throws Exception {
        await(db.joinWaitingList(EVENT_ID, USER_1));
        await(db.joinWaitingList(EVENT_ID, USER_2));

        DocumentSnapshot snap = await(fs.collection("WaitListData").document(EVENT_ID).get());
        List<String> waitList = (List<String>) snap.get("waitList");
        Long count = snap.getLong("waitlistCount");

        assertNotNull(waitList);
        assertTrue(waitList.contains(USER_1));
        assertTrue(waitList.contains(USER_2));
        assertEquals("Both users should be counted", Long.valueOf(waitList.size()), count);
        assertEquals("Count should equal 2", 2, count.intValue());
    }

    // --- 🔹 Test 3: Leaving removes correct user and decrements count ---
    @Test
    public void leave_removesCorrectUser_andDecrementsCount() throws Exception {
        await(db.joinWaitingList(EVENT_ID, USER_1));
        await(db.joinWaitingList(EVENT_ID, USER_2));
        await(db.leaveWaitingList(EVENT_ID, USER_1));

        DocumentSnapshot snap = await(fs.collection("WaitListData").document(EVENT_ID).get());
        List<String> waitList = (List<String>) snap.get("waitList");
        Long count = snap.getLong("waitlistCount");

        assertNotNull(waitList);
        assertFalse("USER_1 should be removed", waitList.contains(USER_1));
        assertTrue("USER_2 should remain", waitList.contains(USER_2));
        assertEquals("Count should match list size", Long.valueOf(waitList.size()), count);
        assertEquals("Count should be 1", 1, count.intValue());
    }

    // --- 🔹 Test 4: Duplicate joins should not increase count ---
    @Test
    public void join_duplicateUser_doesNotIncrementCount() throws Exception {
        await(db.joinWaitingList(EVENT_ID, USER_1));
        await(db.joinWaitingList(EVENT_ID, USER_1)); // duplicate join

        DocumentSnapshot snap = await(fs.collection("WaitListData").document(EVENT_ID).get());
        List<String> waitList = (List<String>) snap.get("waitList");
        Long count = snap.getLong("waitlistCount");

        assertNotNull(waitList);
        assertEquals("Duplicate join should not increase list size", 1, waitList.size());
        assertEquals("Count should remain 1", 1, count.intValue());
    }

    // --- 🔹 Test 5: Leaving non-existent user does not change anything ---
    @Test
    public void leave_nonexistentUser_doesNothing() throws Exception {
        await(db.joinWaitingList(EVENT_ID, USER_1));
        await(db.leaveWaitingList(EVENT_ID, "ghostUser"));

        DocumentSnapshot snap = await(fs.collection("WaitListData").document(EVENT_ID).get());
        List<String> waitList = (List<String>) snap.get("waitList");
        Long count = snap.getLong("waitlistCount");

        assertNotNull(waitList);
        assertTrue(waitList.contains(USER_1));
        assertEquals("Count should remain 1", 1, count.intValue());
    }

    // --- Helper to await Firebase Tasks ---
    private <T> T await(Task<T> task) throws Exception {
        return Tasks.await(task, TIMEOUT_SEC, TimeUnit.SECONDS);
    }


}
