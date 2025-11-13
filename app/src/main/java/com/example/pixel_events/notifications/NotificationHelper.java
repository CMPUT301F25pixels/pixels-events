package com.example.pixel_events.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.pixel_events.R;

/**
 * NotificationHelper
 *
 * Manages Android notification system for lottery results.
 * Creates notification channels and sends notifications to users.
 * Handles both win and loss notifications for lottery events.
 */
public class NotificationHelper {
    private static final String CHANNEL_ID = "lottery_notifications";
    private static final String CHANNEL_NAME = "Lottery Results";

    private Context context;
    private NotificationManager notificationManager;

    /**
     * Initialize NotificationHelper with context
     * @param context Application context for notification system access
     */
    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    /**
     * Create notification channel for Android O and above
     * Required for displaying notifications on newer Android versions
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for lottery results");
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Send a win notification to user
     * @param eventName Name of the event user won lottery for
     *
     * Displays: "Congratulations! You won the lottery for [eventName]"
     */
    public void sendWinNotification(String eventName) {
        String title = "Congratulations!";
        String message = "You won the lottery for " + eventName;
        sendNotification(1, title, message);
    }

    /**
     * Send a loss notification to user
     * @param eventName Name of the event user was not selected for
     *
     * Displays: "You were not selected for [eventName]"
     */
    public void sendLossNotification(String eventName) {
        String title = "Lottery Results";
        String message = "You were not selected for " + eventName;
        sendNotification(2, title, message);
    }

    /**
     * Send a notification to user's device
     * @param notificationId Unique ID for this notification
     * @param title Notification title
     * @param message Notification message content
     */
    private void sendNotification(int notificationId, String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(notificationId, builder.build());
    }
}

/*
 * Class:
 *      NotificationHelper
 *
 * Responsibilities:
 *      Create notification channel for Android O+
 *      Send win notifications to lottery winners
 *      Send loss notifications to non-selected entrants
 *      Manage notification system access
 *
 * Collaborators:
 *      LotteryNotificationService
 *      NotificationManager (Android system)
 */
