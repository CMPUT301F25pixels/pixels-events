package com.example.pixel_events.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.events.Event;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.waitinglist.WaitingList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * LotteryNotificationService
 *
 * Checks for events whose registration deadline has passed
 * and sends lottery notifications to entrants on the waiting list.
 *
 * This is a basic implementation that will be expanded when the
 * full lottery system is implemented.
 */
public class LotteryNotificationService {
    private static final String TAG = "LotteryNotificationService";
    private static final String PREFS_NAME = "pixels_prefs";
    private static final String KEY_PROFILE_ID = "current_profile_id";
    private static final String KEY_NOTIFIED_EVENTS = "notified_events";

    private Context context;
    private NotificationHelper notificationHelper;
    private DatabaseHandler databaseHandler;

    public LotteryNotificationService(Context context) {
        this.context = context;
        this.notificationHelper = new NotificationHelper(context);
        this.databaseHandler = DatabaseHandler.getInstance();
    }

    /**
     * Check all events and send notifications for those whose
     * registration deadline has passed.
     *
     * This method should be called when the app starts or
     * when the user views their events.
     */
    public void checkAndNotifyRegistrationDeadlines() {
        int currentUserId = AuthManager.getInstance().getCurrentUserProfile().getUserId();

        Log.d(TAG, "Checking registration deadlines for user: " + currentUserId);

        // Get all events from database
        databaseHandler.getAllEvents(
                events -> {
                    if (events != null) {
                        for (Event event : events) {
                            checkEventForNotification(event, currentUserId);
                        }
                    }
                },
                error -> {
                    Log.e(TAG, "Error loading events for notification check", error);
                }
        );
    }

    /**
     * Check a specific event to see if its registration deadline has passed
     * and if the user should be notified.
     */
    private void checkEventForNotification(Event event, int currentUserId) {
        if (event == null || event.getRegistrationEndDate() == null) {
            return;
        }

        try {
            // Parse registration end date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date registrationEndDate = sdf.parse(event.getRegistrationEndDate());
            Date now = new Date();

            // Check if registration deadline has passed
            if (registrationEndDate != null && registrationEndDate.before(now)) {
                // Check if user is on waitlist
                int eventId = event.getEventId();
                databaseHandler.getWaitingList(eventId,
                        waitingList -> {
                            if (waitingList != null && waitingList.isUserInWaitlist(currentUserId)) {
                                // Check if we've already notified for this event
                                if (!hasBeenNotified(eventId)) {
                                    sendLotteryNotification(event.getTitle());
                                    markAsNotified(eventId);
                                }
                            }
                        },
                        error -> {
                            Log.e(TAG, "Error checking waitlist for event " + eventId, error);
                        }
                );
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing registration end date for event " + event.getEventId(), e);
        }
    }

    /**
     * Send a lottery notification to the user
     * This is a placeholder implementation until the full lottery system is ready
     */
    private void sendLotteryNotification(String eventName) {
        Log.d(TAG, "Sending lottery notification for event: " + eventName);

        // For now, send a generic notification about lottery results pending
        // In the future, this will be replaced with actual win/loss notifications
        notificationHelper.sendLossNotification(eventName + " - Lottery results pending");
    }

    /**
     * Check if we've already sent a notification for this event
     */
    private boolean hasBeenNotified(int eventId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String notifiedEvents = prefs.getString(KEY_NOTIFIED_EVENTS, "");
        return notifiedEvents.contains(eventId + ",");
    }

    /**
     * Mark an event as having been notified
     */
    private void markAsNotified(int eventId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String notifiedEvents = prefs.getString(KEY_NOTIFIED_EVENTS, "");
        notifiedEvents += eventId + ",";
        prefs.edit().putString(KEY_NOTIFIED_EVENTS, notifiedEvents).apply();
        Log.d(TAG, "Marked event " + eventId + " as notified");
    }

    /**
     * Test method to send a notification immediately
     * Useful for demonstrating the notification system to the TA
     */
    public void sendTestNotification(String eventName) {
        notificationHelper.sendLossNotification(eventName);
    }
}

/*
 * Class:
 *      LotteryNotificationService
 *
 * Responsibilities:
 *      Check for events with passed registration deadlines
 *      Send notifications to users on waiting lists
 *      Track which events have been notified
 *      Provide test notification capability for demos
 *
 * Collaborators:
 *      NotificationHelper
 *      DatabaseHandler
 *      Event
 *      WaitingList
 *
 * Notes:
 *      This is a basic implementation. When the lottery system is fully
 *      implemented, this service will be enhanced to send win/loss
 *      notifications based on actual lottery results.
 */
