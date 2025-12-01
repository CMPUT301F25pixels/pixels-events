package com.example.pixel_events.events;

import com.example.pixel_events.waitinglist.WaitlistUser;

/**
 * EventInvitation
 *
 * Model class combining Event and WaitlistUser for invitation display.
 * Used in NotificationFragment to show lottery invitations with event context.
 * Simplifies data binding for invitation accept/decline UI.
 *
 * Implements:
 * - US 01.05.02 (Display and accept invitations)
 * - US 01.05.03 (Display and decline invitations)
 *
 * Collaborators:
 * - Event: Event information for invitation
 * - WaitlistUser: Invitation status
 * - NotificationFragment: Invitation display
 */
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
