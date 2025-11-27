package com.example.pixel_events.notifications;

import com.example.pixel_events.events.Event;
import com.example.pixel_events.waitinglist.WaitlistUser;

public class EventInvitation {
    private Event event;
    private WaitlistUser waitlistUser;

    public EventInvitation(Event event, WaitlistUser waitlistUser) {
        this.event = event;
        this.waitlistUser = waitlistUser;
    }

    public Event getEvent() {
        return event;
    }

    public WaitlistUser getWaitlistUser() {
        return waitlistUser;
    }
}
