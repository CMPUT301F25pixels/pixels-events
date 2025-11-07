package com.example.pixel_events.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.pixel_events.R;

public class NotificationHelper {
    private static final String CHANNEL_ID = "lottery_notifications";
    private static final String CHANNEL_NAME = "Lottery Results";
    
    private Context context;
    private NotificationManager notificationManager;
    
    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }
    
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
    
    public void sendWinNotification(String eventName) {
        String title = "Congratulations!";
        String message = "You won the lottery for " + eventName;
        sendNotification(1, title, message);
    }
    
    public void sendLossNotification(String eventName) {
        String title = "Lottery Results";
        String message = "You were not selected for " + eventName;
        sendNotification(2, title, message);
    }
    
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

