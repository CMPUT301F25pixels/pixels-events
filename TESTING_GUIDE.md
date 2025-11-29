# Notification System Testing Guide

## ✅ All User Stories Implemented

### US 01.04.01 - Notify when user wins lottery
### US 01.04.02 - Notify when user loses lottery
### US 02.05.01 - Send notifications to selected entrants
### US 02.07.01 - Notify all entrants on waitlist
### US 02.07.02 - Notify selected entrants
### US 02.07.03 - Notify cancelled entrants
### US 03.08.01 - Review logs of all notifications

---

## Two-Emulator Testing Guide

### Setup
1. Start **2 Android emulators** (Medium Phone API 35)
2. Install the app on both

**Test Accounts:**
- Entrant: `user1@gmail.com` / `user123`
- Organizer: `org1@gmail.com` / `org123`
- Admin: `admin@gmail.com` / `admin123`

---

## Test Scenario 1: Lottery Draw (US 01.04.01, 01.04.02)

**Emulator 1 (Organizer):**
1. Login as `org1@gmail.com`
2. Create event "Swimming Lessons" with capacity 2
3. Go to "My Events" → Select the event
4. Note that "Draw Lottery" button is visible at top

**Emulator 2 (Entrants):**
1. Login as `user1@gmail.com`
2. Join waiting list for "Swimming Lessons"
3. Logout, login as `user2@gmail.com` (create if needed)
4. Join waiting list for "Swimming Lessons"
5. Logout, login as `user3@gmail.com` (create if needed)
6. Join waiting list for "Swimming Lessons"
7. **Keep app open**

**Emulator 1 (Organizer):**
1. View Waiting List → Should see 3 users
2. Click **"Draw Lottery"** button
3. Dialog shows: "2 participant(s) have been randomly selected!"

**Emulator 2 (Check Notifications):**
1. **Expected:** Popup appears immediately:
   - If winner: "Lottery Won! You have been selected for Swimming Lessons. Please sign up!"
   - If loser: "Lottery Result - Unfortunately you were not selected for Swimming Lessons."
2. Click notification bell icon → "Notifications" tab → See stored notification

---

## Test Scenario 2: Organizer Sends Message (US 02.07.01)

**Emulator 1 (Organizer):**
1. Still in "Swimming Lessons" event
2. Click "View Waitlist"
3. Click the **notification bell icon** (new button at top)
4. Select "All Waitlist Entrants"
5. Type message: "Pool will be closed next week"
6. Click "Send"

**Emulator 2 (Entrants):**
1. Login as any entrant (user1, user2, or user3)
2. **Expected:** Popup appears with the organizer's message
3. Notification bell → "Notifications" tab → Message is stored

---

## Test Scenario 3: Notify Selected Only (US 02.07.02)

**Emulator 1 (Organizer):**
1. Click notification bell in waitlist
2. Select "Selected Entrants (Winners)"
3. Type: "Please confirm your attendance"
4. Click "Send"

**Emulator 2:**
1. Login as a **winner**
2. **Expected:** Popup appears with message
3. Login as a **loser**
4. **Expected:** No popup (only winners get it)

---

## Test Scenario 4: Notify Cancelled (US 02.07.03)

**Emulator 1 (Organizer):**
1. In waitlist, click the delete (X) button on a user
2. Confirm removal (status → declined/3)
3. Click notification bell → Select "Cancelled Entrants"
4. Type: "Sorry, spots are limited"
5. Send

**Emulator 2:**
1. Login as the cancelled user
2. **Expected:** Popup with cancellation message

---

## Test Scenario 5: Admin Reviews Logs (US 03.08.01)

**Emulator 1 (Admin):**
1. Login as `admin@gmail.com`
2. Click the **notification bell icon** at top right (in admin dashboard)
3. **Expected:** See list of ALL notifications sent, showing:
   - Title & Message
   - From: Organizer name / System
   - To: Entrant name
   - Type: LOTTERY_WIN, ORGANIZER_MESSAGE, etc.
   - Date/Time

---

## Test Scenario 6: Admin Deletes Event

**Emulator 2 (Entrant):**
1. Login as `user1@gmail.com`
2. Join waitlist for any event
3. **Keep app open**

**Emulator 1 (Admin):**
1. Login as `admin@gmail.com`
2. Go to "Events" → Delete the event user1 joined

**Emulator 2:**
- **Expected:** Popup: "Event Cancelled - The event you interacted with has been cancelled by the Admin."

---

## Test Scenario 7: Admin Deletes Profile (Auto-Logout)

**Emulator 2 (Entrant):**
1. Login as `user1@gmail.com`
2. **Keep app open**

**Emulator 1 (Admin):**
1. Go to "Profiles" → Find user1 → Delete

**Emulator 2:**
- **Expected:** Popup: "Profile Deleted..."
- Click OK
- **Expected:** User is logged out automatically and sent to login screen

---

## Quick Firestore Console Test

1. Login to app as any user (note userId)
2. Open Firebase Console → `AccountData/{userId}/Notifications`
3. Add document:
   ```
   title: "Test"
   message: "Live test"
   read: false
   timestamp: (now)
   type: "GENERAL"
   ```
4. **Expected:** Dialog pops up instantly in app!

---

## Verification Checklist

- [ ] Lottery winners get win notification
- [ ] Lottery losers get loss notification
- [ ] Organizer can message all waitlist
- [ ] Organizer can message selected only
- [ ] Organizer can message cancelled only
- [ ] Admin can view notification logs with sender/recipient names
- [ ] Admin deletion triggers notifications
- [ ] Profile deletion logs user out
- [ ] Real-time alerts work (immediate popups)
- [ ] Notifications persist when user is offline

All features integrated with existing lottery and invitation UI!


