package com.example.pixel_events.notifications;

import android.content.Context;
import java.util.List;

/**
 * LotteryNotificationService
 *
 * Business logic layer for sending lottery-related notifications.
 * Handles both individual and bulk notification sending for
 * lottery winners and non-selected entrants.
 *
 * Implements US 01.04.01 (win notification) and US 01.04.02 (loss notification).
 */
public class LotteryNotificationService {
    
    private NotificationHelper notificationHelper;
    
    /**
     * Initialize lottery notification service
     * @param context Application context for notification system
     */
    public LotteryNotificationService(Context context) {
        this.notificationHelper = new NotificationHelper(context);
    }
    
    /**
     * Send win notification to a single user
     * @param userId User ID of the lottery winner
     * @param eventName Name of the event they won lottery for
     * 
     * Usage example:
     *     service.notifyWinner("user123", "Swimming Lessons");
     */
    public void notifyWinner(String userId, String eventName) {
        notificationHelper.sendWinNotification(eventName);
    }
    
    /**
     * Send loss notification to a single user
     * @param userId User ID of the non-selected entrant
     * @param eventName Name of the event they were not selected for
     * 
     * Usage example:
     *     service.notifyLoser("user456", "Swimming Lessons");
     */
    public void notifyLoser(String userId, String eventName) {
        notificationHelper.sendLossNotification(eventName);
    }
    
    /**
     * Send win notifications to multiple users
     * @param userIds List of user IDs who won the lottery
     * @param eventName Name of the event
     * 
     * Usage example:
     *     List<String> winners = Arrays.asList("user1", "user2", "user3");
     *     service.notifyWinners(winners, "Swimming Lessons");
     */
    public void notifyWinners(List<String> userIds, String eventName) {
        for (String userId : userIds) {
            notifyWinner(userId, eventName);
        }
    }
    
    /**
     * Send loss notifications to multiple users
     * @param userIds List of user IDs who were not selected
     * @param eventName Name of the event
     * 
     * Usage example:
     *     List<String> losers = Arrays.asList("user4", "user5");
     *     service.notifyLosers(losers, "Swimming Lessons");
     */
    public void notifyLosers(List<String> userIds, String eventName) {
        for (String userId : userIds) {
            notifyLoser(userId, eventName);
        }
    }
}

/*
 * Class:
 *      LotteryNotificationService
 *
 * Responsibilities:
 *      Send win notifications to lottery winners (US 01.04.01)
 *      Send loss notifications to non-selected entrants (US 01.04.02)
 *      Handle bulk notification sending
 *      Coordinate with NotificationHelper for delivery
 *
 * Collaborators:
 *      NotificationHelper
 *      LotterySystem (for receiving winner/loser lists)
 */
