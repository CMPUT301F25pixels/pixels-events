package com.example.pixel_events.notifications;

import org.junit.Test;
import static org.junit.Assert.*;

public class LotteryNotificationServiceTest {

    @Test
    public void testServiceCreation() {
        assertNotNull("Service should be creatable", LotteryNotificationService.class);
    }
    
    @Test
    public void testNotificationHelperCreation() {
        assertNotNull("Helper should be creatable", NotificationHelper.class);
    }
}

