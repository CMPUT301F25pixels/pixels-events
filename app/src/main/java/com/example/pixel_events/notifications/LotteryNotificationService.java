package com.example.pixel_events.notifications;

import android.content.Context;
import java.util.List;

public class LotteryNotificationService {
    
    private NotificationHelper notificationHelper;
    
    public LotteryNotificationService(Context context) {
        this.notificationHelper = new NotificationHelper(context);
    }
    
    public void notifyWinner(String userId, String eventName) {
        notificationHelper.sendWinNotification(eventName);
    }
    
    public void notifyLoser(String userId, String eventName) {
        notificationHelper.sendLossNotification(eventName);
    }
    
    public void notifyWinners(List<String> userIds, String eventName) {
        for (String userId : userIds) {
            notifyWinner(userId, eventName);
        }
    }
    
    public void notifyLosers(List<String> userIds, String eventName) {
        for (String userId : userIds) {
            notifyLoser(userId, eventName);
        }
    }
}

