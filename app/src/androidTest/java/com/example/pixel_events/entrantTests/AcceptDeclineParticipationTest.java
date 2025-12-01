package com.example.pixel_events.entrantTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.waitinglist.WaitingList;
import com.example.pixel_events.waitinglist.WaitlistUser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/*
    Tests:
        US 01.05.02: As an entrant I want to be able to accept the invitation to register/sign up
                     when chosen to participate in an event.
        US 01.05.03: As an entrant I want to be able to decline an invitation when chosen to
                     participate in an event.

    Utilizes:
        White Box Testing
 */
@RunWith(AndroidJUnit4.class)
public class AcceptDeclineParticipationTest {
    private DatabaseHandler db;
    private ArrayList<String> tags;
    private String today;

    private static final int STATUS_CHOSEN = 1;
    private static final int STATUS_ACCEPTED = 2;
    private static final int STATUS_DECLINED = 3;

    @Before
    public void setUp() {
        // Init
        db = DatabaseHandler.getInstance(true);
        tags = new ArrayList<>();
        tags.add("TestTag");
        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    @Test
    public void testAcceptInvite() throws InterruptedException {
        int eventId = 6001;
        int userId = 101;

        // Create event
        Event evn = new Event(eventId,
                1,
                "Test Event",
                "test.URL",
                "Edmonton",
                100,
                "Test Desc",
                "100",
                "2026-01-01",
                "2030-01-01",
                "09:00",
                "10:00",
                today, // Registration start
                "2025-12-10", // Registration end
                tags);

        db.addEvent(evn);

        // Create Waitlist
        CountDownLatch setupLatch = new CountDownLatch(1);
        ArrayList<WaitlistUser> seededUsers = new ArrayList<>();
        seededUsers.add(new WaitlistUser(userId, STATUS_CHOSEN));
        db.addWaitingList(new WaitingList(eventId));

        // Wait and overwrite
        Thread.sleep(500);
        db.getWaitListCollection().document(String.valueOf(eventId))
                .update("waitList", seededUsers)
                .addOnSuccessListener(v -> setupLatch.countDown());

        if (!setupLatch.await(5, TimeUnit.SECONDS)) fail("Database setup timed out");

        // Get waitlist
        CountDownLatch actionLatch = new CountDownLatch(1);
        db.getWaitingList(eventId, wl -> {
            // Verify
            boolean isChosen = wl.getWaitList().stream()
                    .anyMatch(u -> u.getUserId() == userId && u.getStatus() == STATUS_CHOSEN);
            if (!isChosen) fail("Setup failed: User not chosen.");

            // Accept
            wl.updateUserStatus(userId, STATUS_ACCEPTED);

            // Wait
            actionLatch.countDown();
        }, e -> fail("Failed to fetch waitlist"));

        actionLatch.await(5, TimeUnit.SECONDS);
        Thread.sleep(1000);

        // Verify Accept
        CountDownLatch verifyLatch = new CountDownLatch(1);
        db.getWaitingList(eventId, wl -> {
            boolean isAccepted = wl.getWaitList().stream()
                    .anyMatch(u -> u.getUserId() == userId && u.getStatus() == STATUS_ACCEPTED);

            if (!isAccepted) fail("User status did not update to ACCEPTED");
            verifyLatch.countDown();
        }, e -> fail("Failed to fetch verification waitlist"));

        verifyLatch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testDeclineInvite() throws InterruptedException {
        int eventId = 6002;
        int userId = 102;

        // Create Event
        Event evn = new Event(eventId,
                1,
                "Test Event",
                "test.URL",
                "Edmonton",
                100,
                "Test Desc",
                "100",
                "2026-01-01",
                "2030-01-01",
                "09:00",
                "10:00",
                today, // Registration start
                "2025-12-10", // Registration end
                tags);

        db.addEvent(evn);

        // Create waitlist
        CountDownLatch setupLatch = new CountDownLatch(1);
        ArrayList<WaitlistUser> seededUsers = new ArrayList<>();
        seededUsers.add(new WaitlistUser(userId, STATUS_CHOSEN));

        db.addWaitingList(new WaitingList(eventId));
        Thread.sleep(500);
        db.getWaitListCollection().document(String.valueOf(eventId))
                .update("waitList", seededUsers)
                .addOnSuccessListener(v -> setupLatch.countDown());

        if (!setupLatch.await(5, TimeUnit.SECONDS)) fail("Database setup timed out");

        // Decline
        CountDownLatch actionLatch = new CountDownLatch(1);
        db.getWaitingList(eventId, wl -> {
            wl.updateUserStatus(userId, STATUS_DECLINED);
            actionLatch.countDown();

        }, e -> fail("Failed to fetch waitlist"));

        actionLatch.await(5, TimeUnit.SECONDS);
        Thread.sleep(1000);

        // Verify decline
        CountDownLatch verifyLatch = new CountDownLatch(1);
        db.getWaitingList(eventId, wl -> {
            boolean isDeclined = wl.getWaitList().stream()
                    .anyMatch(u -> u.getUserId() == userId && u.getStatus() == STATUS_DECLINED);

            if (!isDeclined) fail("User status did not update to DECLINED");
            verifyLatch.countDown();
        }, e -> fail("Failed to fetch verification waitlist"));

        verifyLatch.await(5, TimeUnit.SECONDS);
    }
}