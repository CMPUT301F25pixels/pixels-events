# Notification System Usage

## US 01.04.01 - Win Notification
## US 01.04.02 - Loss Notification

### How to use:

```java
// Create service
LotteryNotificationService service = new LotteryNotificationService(context);

// Send win notification
service.notifyWinner(userId, "Swimming Lessons");

// Send loss notification
service.notifyLoser(userId, "Swimming Lessons");

// Send to multiple users
List<String> winners = Arrays.asList("user1", "user2");
service.notifyWinners(winners, "Swimming Lessons");

List<String> losers = Arrays.asList("user3", "user4");
service.notifyLosers(losers, "Swimming Lessons");
```

### Files created:
- NotificationHelper.java - handles Android notifications
- LotteryNotificationService.java - sends lottery notifications

### Permissions added:
- POST_NOTIFICATIONS - required for Android 13+

### Dependencies added:
- firebase-messaging

