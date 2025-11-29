package com.example.pixel_events.notifications;

import static org.junit.Assert.*;

import android.util.Log;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.waitinglist.WaitingList;
import com.example.pixel_events.waitinglist.WaitlistUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests for notification system user stories
 * US 01.04.01, 01.04.02, 02.05.01, 02.07.01, 02.07.02, 02.07.03, 03.08.01
 */
public class NotificationSystemTest {
    private DatabaseHandler db;
    private static final int TEST_USER_1 = 999001;
    private static final int TEST_USER_2 = 999002;
    private static final int TEST_ORGANIZER = 999100;
    private static final int TEST_EVENT = 999500;

    @Before
    public void setUp() {
        db = DatabaseHandler.getInstance(true); // Use emulator
    }

    @After
    public void tearDown() {
        DatabaseHandler.resetInstance();
    }

    /**
     * US 01.04.01 - Test win notification is sent
     */
    @Test
    public void testSendWinNotification() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);

        db.sendWinNotification(TEST_EVENT, "Test Event", TEST_USER_1);

        // Wait a moment for async operation
        Thread.sleep(500);

        // Verify notification exists in user's collection
        db.getFirestore()
                .collection("AccountData")
                .document(String.valueOf(TEST_USER_1))
                .collection("Notifications")
                .get()
                .addOnSuccessListener(snapshots -> {
                    boolean found = false;
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                        Notification n = doc.toObject(Notification.class);
                        if (n != null && "LOTTERY_WIN".equals(n.getType())) {
                            found = true;
                            break;
                        }
                    }
                    success.set(found);
                    latch.countDown();
                });

        latch.await(5, TimeUnit.SECONDS);
        assertTrue("Win notification should be sent", success.get());
    }

    /**
     * US 01.04.02 - Test loss notification is sent
     */
    @Test
    public void testSendLossNotification() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);

        db.sendLossNotification(TEST_EVENT, "Test Event", TEST_USER_2);

        Thread.sleep(500);

        db.getFirestore()
                .collection("AccountData")
                .document(String.valueOf(TEST_USER_2))
                .collection("Notifications")
                .get()
                .addOnSuccessListener(snapshots -> {
                    boolean found = false;
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                        Notification n = doc.toObject(Notification.class);
                        if (n != null && "LOTTERY_LOSS".equals(n.getType())) {
                            found = true;
                            break;
                        }
                    }
                    success.set(found);
                    latch.countDown();
                });

        latch.await(5, TimeUnit.SECONDS);
        assertTrue("Loss notification should be sent", success.get());
    }

    /**
     * US 03.08.01 - Test notifications are logged globally
     */
    @Test
    public void testNotificationLogsCreated() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);

        Notification testNotif = new Notification("Test", "Test message", "GENERAL", -1, TEST_USER_1);
        db.addNotification(TEST_USER_1, testNotif);

        Thread.sleep(500);

        // Verify it's in NotificationLogs collection
        db.getFirestore()
                .collection("NotificationLogs")
                .document(testNotif.getNotificationId())
                .get()
                .addOnSuccessListener(doc -> {
                    success.set(doc.exists());
                    latch.countDown();
                });

        latch.await(5, TimeUnit.SECONDS);
        assertTrue("Notification should be logged globally", success.get());
    }
}

