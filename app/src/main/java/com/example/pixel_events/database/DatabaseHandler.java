package com.example.pixel_events.database;

import android.util.Log;

import com.example.pixel_events.events.Event;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.waitinglist.WaitingList;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;
import java.util.function.Consumer;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

public class DatabaseHandler {
    private static DatabaseHandler instance;
    private final FirebaseFirestore db;
    private final CollectionReference accRef;
    private final CollectionReference eventRef;
    private final CollectionReference waitListRef;

    /**
     * Initialize the database
     */
    private DatabaseHandler() {
        this(false);
    }

    private DatabaseHandler(boolean offlineMode) {
        db = FirebaseFirestore.getInstance();

        if (offlineMode) {
            // Use 10.0.2.2 for Android emulator (localhost equivalent)
            // Use 127.0.0.1 for instrumentation tests on desktop
            try {
                db.useEmulator("10.0.2.2", 8080);
                Log.d("DatabaseHandler", "Connected to Firebase emulator at 10.0.2.2:8080");
            } catch (IllegalStateException e) {
                // Emulator already set, ignore
                Log.d("DatabaseHandler", "Emulator already configured");
            }
        }

        accRef = db.collection("AccountData");
        eventRef = db.collection("EventData");
        waitListRef = db.collection("WaitListData");
    }

    public static synchronized DatabaseHandler getInstance() {
        if (instance == null) {
            instance = new DatabaseHandler();
        }
        return instance;
    }

    /**
     * Get or create instance with specified mode
     * 
     * @param offlineMode If true, uses Firebase emulator; if false, uses real
     *                    Firebase
     * @return DatabaseHandler instance
     */
    public static synchronized DatabaseHandler getInstance(boolean offlineMode) {
        if (instance == null) {
            instance = new DatabaseHandler(offlineMode);
        }
        return instance;
    }

    /**
     * Reset the singleton instance (useful for testing)
     */
    public static synchronized void resetInstance() {
        instance = null;
    }

    // Optional accessors for other classes to read internal references
    public FirebaseFirestore getFirestore() {
        return db;
    }

    /**
     * Convert a Firebase UID string to a positive integer id used by legacy DB.
     * Ensures the returned id is > 0.
     */
    public static int uidToId(String uid) {
        if (uid == null)
            return 1;
        int raw = uid.hashCode();
        if (raw == Integer.MIN_VALUE)
            return Integer.MAX_VALUE;
        int v = Math.abs(raw);
        if (v <= 0)
            v = 1;
        return v;
    }

    public CollectionReference getAccountCollection() {
        return accRef;
    }

    public CollectionReference getEventCollection() {
        return eventRef;
    }

    public CollectionReference getWaitListCollection() {
        return waitListRef;
    }

    // COMMON FUNCTIONS
    // --------------------------------------------------------------------------

    public void modify(CollectionReference reference,
            Object id,
            Map<String, Object> updates,
            Consumer<String> errorCallback) {
        // First check if the document exists
        reference.document(String.valueOf(id))
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        // Document exists, perform update
                        reference.document(String.valueOf(id))
                                .update(updates)
                                .addOnSuccessListener(unused -> {
                                    Log.d("DB", "Updated user " + id + " with " +
                                            updates.size() + " field(s)");
                                    if (errorCallback != null) {
                                        errorCallback.accept(null); // Success, no error
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("DB", "Error updating user " + id, e);
                                    if (errorCallback != null) {
                                        errorCallback.accept("Failed to update: " +
                                                e.getMessage());
                                    }
                                });
                    } else {
                        // Document doesn't exist
                        String errorMsg = "User ID " + id + " does not exist in database";
                        Log.e("DB", errorMsg);
                        if (errorCallback != null) {
                            errorCallback.accept(errorMsg);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DB", "Error checking if user exists: " + id, e);
                    if (errorCallback != null) {
                        errorCallback.accept("Error checking user existence: " + e.getMessage());
                    }
                });
    }

    // ACCOUNT INFO FUNCTIONS
    // ---------------------------------------------------------------------

    public void addAcc(Profile newUser) {
        // Add the account to DB
        accRef.document(String.valueOf(newUser.getUserId()))
                .set(newUser)
                .addOnSuccessListener(unused -> Log.d("DB", "Added User: " + newUser.getUserName()))
                .addOnFailureListener(e -> Log.w("DB", "Error adding user!", e));
    }

    public void getProfile(int id,
            OnSuccessListener<Profile> listener,
            OnFailureListener errorListener) {
        accRef.document(String.valueOf(id))
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        listener.onSuccess(snapshot.toObject(Profile.class));
                    } else {
                        listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DB", "Error getting account", e);
                    errorListener.onFailure(e);
                });
    }

    /**
     * Fetch a profile by email address.
     *
     * @param email         The email to search for
     * @param listener      Success callback with the Profile object (or null if not
     *                      found)
     * @param errorListener Failure callback
     */
    public void getProfileByEmail(String email,
            OnSuccessListener<Profile> listener,
            OnFailureListener errorListener) {
        accRef.whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        try {
                            // Manually map fields to avoid strict type issues if any
                            int id = 0;
                            Object idObj = document.get("userId"); // Try "userId" first
                            if (idObj == null)
                                idObj = document.get("id"); // Fallback to "id"

                            if (idObj instanceof Number) {
                                id = ((Number) idObj).intValue();
                            } else if (idObj instanceof String) {
                                try {
                                    id = Integer.parseInt((String) idObj);
                                } catch (NumberFormatException e) {
                                    id = 0;
                                }
                            }

                            if (id == 0) {
                                // Try to parse from document ID if it's an int
                                try {
                                    id = Integer.parseInt(document.getId());
                                } catch (NumberFormatException e) {
                                    // ignore
                                }
                            }

                            Profile profile = document.toObject(Profile.class);
                            // Ensure ID is set if toObject didn't catch it (e.g. if field name mismatch)
                            if (profile != null && profile.getUserId() == 0) {
                                profile.setUserId(id);
                            }
                            listener.onSuccess(profile);
                        } catch (Exception e) {
                            Log.e("DB", "Error parsing profile", e);
                            errorListener.onFailure(e);
                        }
                    } else {
                        listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DB", "Error finding profile by email", e);
                    errorListener.onFailure(e);
                });
    }

    public void deleteAcc(int userID) {
        // 1) Delete events organized by this user
        eventRef.whereEqualTo("organizerId", userID).get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        try {
                            int eventId = Integer.parseInt(doc.getId());
                            deleteEvent(eventId);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                });

        // 2) Remove user from waitlists and redraw if they had accepted
        waitListRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    java.util.List<com.google.android.gms.tasks.Task<Void>> removals = new java.util.ArrayList<>();
                    java.util.List<Integer> redrawEvents = new java.util.ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        int eventId;
                        try {
                            eventId = Integer.parseInt(doc.getId());
                        } catch (NumberFormatException ex) {
                            continue;
                        }

                        // Coerce to WaitingList for simple inspection
                        WaitingList wl = safeMapWaitingList(doc);
                        if (wl == null || wl.getWaitList() == null)
                            continue;

                        boolean containsUser = false;
                        boolean accepted = false;
                        for (com.example.pixel_events.waitinglist.WaitlistUser u : wl.getWaitList()) {
                            if (u.getUserId() == userID) {
                                containsUser = true;
                                if (u.getStatus() == 2)
                                    accepted = true;
                                break;
                            }
                        }

                        if (containsUser) {
                            removals.add(leaveWaitingList(eventId, userID));
                            if (accepted)
                                redrawEvents.add(eventId);
                        }
                    }

                    Tasks.whenAll(removals).addOnSuccessListener(unused -> {
                        // Trigger redraws for freed accepted slots
                        for (Integer eid : redrawEvents) {
                            getWaitingList(eid, wl -> {
                                if (wl != null) {
                                    wl.drawLottery(new WaitingList.OnLotteryDrawnListener() {
                                        @Override
                                        public void onSuccess(int n) {
                                            Log.d("DB", "Redrew lottery for " + eid + "; selected=" + n);
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            Log.e("DB", "Redraw failed for " + eid, e);
                                        }
                                    });
                                }
                            }, e -> Log.e("DB", "Failed to load waitlist for redraw: " + eid, e));
                        }

                        // 3) Delete account document
                        accRef.document(String.valueOf(userID)).delete()
                                .addOnSuccessListener(unused2 -> Log.d("DB", "Deleted user: " + userID))
                                .addOnFailureListener(e -> Log.e("DB", "Error deleting user " + userID, e));

                    }).addOnFailureListener(e -> {
                        Log.e("DB", "Failed to remove user from waitlists", e);
                        accRef.document(String.valueOf(userID)).delete()
                                .addOnSuccessListener(
                                        unused2 -> Log.d("DB", "Deleted user (partial cleanup): " + userID))
                                .addOnFailureListener(err -> Log.e("DB", "Error deleting user " + userID, err));
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("DB", "Failed to scan waitlists for deletion: " + userID, e);
                    accRef.document(String.valueOf(userID)).delete()
                            .addOnSuccessListener(unused -> Log.d("DB", "Deleted user (fallback): " + userID))
                            .addOnFailureListener(err -> Log.e("DB", "Error deleting user " + userID, err));
                });
    }

    /**
     * Fetch all profiles in AccountData collection.
     * 
     * @param listener      success callback with list of Profile objects (empty
     *                      list if none)
     * @param errorListener failure callback invoked on Firestore error
     */
    public void getAllProfile(OnSuccessListener<java.util.List<Profile>> listener,
            OnFailureListener errorListener) {
        accRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    java.util.List<Profile> profiles = new java.util.ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        try {
                            Profile p = document.toObject(Profile.class);
                            if (p != null) {
                                profiles.add(p);
                            }
                        } catch (RuntimeException ex) {
                            Log.e("DB", "Failed to deserialize Profile doc: " + document.getId(), ex);
                        }
                    }
                    listener.onSuccess(profiles);
                })
                .addOnFailureListener(e -> {
                    Log.e("DB", "Error getting all profiles", e);
                    errorListener.onFailure(e);
                });
    }

    // EVENT INFO FUNCTIONS
    // -----------------------------------------------------------------------

    public void addEvent(Event newEvent) {
        // Add the event to DB
        eventRef.document(String.valueOf(newEvent.getEventId()))
                .set(newEvent)
                .addOnSuccessListener(unused -> {
                    Log.d("DB", "Successfully added Event: " + newEvent.getEventId() +
                            " - " + newEvent.getTitle());
                })
                .addOnFailureListener(e -> {
                    Log.e("DB", "Error adding event " + newEvent.getEventId(), e);
                });
    }

    public void getEvent(int id,
            OnSuccessListener<Event> listener,
            OnFailureListener errorListener) {
        eventRef.document(String.valueOf(id))
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        try {
                            Event ev = safeMapEvent(snapshot);
                            listener.onSuccess(ev);
                        } catch (RuntimeException ex) {
                            Log.e("DB", "Failed to deserialize Event", ex);
                            errorListener.onFailure(ex);
                        }
                    } else {
                        listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DB", "Error getting account", e);
                    errorListener.onFailure(e);
                });
    }

    public void getAllEvents(OnSuccessListener<java.util.List<Event>> listener,
            OnFailureListener errorListener) {
        eventRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    java.util.List<Event> events = new java.util.ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        try {
                            Event ev = safeMapEvent(document);
                            if (ev != null) {
                                events.add(ev);
                            }
                        } catch (RuntimeException ex) {
                            Log.e("DB", "Failed to deserialize Event doc: " + document.getId(), ex);
                        }
                    }
                    listener.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    Log.e("DB", "Error getting all events", e);
                    errorListener.onFailure(e);
                });
    }

    public void deleteEvent(int eventID) {
        eventRef.document(String.valueOf(eventID))
                .delete()
                .addOnSuccessListener(unused -> Log.d("DB", "Deleted event: " + eventID))
                .addOnFailureListener(e -> Log.e("DB", "Error deleting event " + eventID, e));
        waitListRef.document(String.valueOf(eventID))
                .delete()
                .addOnSuccessListener(unused -> Log.d("DB", "Deleted Waitlist for event: " + eventID))
                .addOnFailureListener(e -> Log.e("DB", "Error deleting event " + eventID, e));
    }

    // WAITING LIST PUBLIC OPERATIONS (Task-based)
    // -----------------------------------------------------------------------

    /**
     * Ensures the waitlist document exists for the given event.
     * If it does not exist, creates a minimal document with empty arrays.
     */
    private Task<Void> ensureWaitListExists(int eventId) {
        String docId = String.valueOf(eventId);
        return waitListRef.document(docId)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        return Tasks.forException(e != null ? e : new RuntimeException("Failed to read waitlist"));
                    }
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot != null && snapshot.exists()) {
                        return Tasks.forResult(null);
                    }
                    java.util.Map<String, Object> init = new java.util.HashMap<>();
                    init.put("eventId", eventId);
                    init.put("waitList", java.util.Collections.emptyList());
                    return waitListRef.document(docId).set(init).continueWith(t -> null);
                });
    }

    /**
     * Adds the user id to the event waitlist (idempotent).
     */
    public Task<Void> joinWaitingList(int eventId, int userId) {
        String docId = String.valueOf(eventId);
        return ensureWaitListExists(eventId)
                .continueWithTask(t -> waitListRef.document(docId).get())
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return Tasks.forException(task.getException() != null ? task.getException()
                                : new RuntimeException("Failed to read waitlist"));
                    }

                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot == null || !snapshot.exists()) {
                        return Tasks.forException(new RuntimeException("Waitlist not found"));
                    }

                    // Get current waitList
                    java.util.List<Object> waitList = (java.util.List<Object>) snapshot.get("waitList");
                    if (waitList == null) {
                        waitList = new java.util.ArrayList<>();
                    }

                    // Check if user already exists
                    boolean userExists = false;
                    for (Object obj : waitList) {
                        if (obj instanceof java.util.Map) {
                            java.util.Map<String, Object> userMap = (java.util.Map<String, Object>) obj;
                            Object uid = userMap.get("userId");
                            int existingUserId = 0;
                            if (uid instanceof Number) {
                                existingUserId = ((Number) uid).intValue();
                            } else if (uid instanceof String) {
                                try {
                                    existingUserId = Integer.parseInt((String) uid);
                                } catch (NumberFormatException e) {
                                    // ignore
                                }
                            }
                            if (existingUserId == userId) {
                                userExists = true;
                                break;
                            }
                        }
                    }

                    // If user doesn't exist, add them
                    if (!userExists) {
                        java.util.Map<String, Object> newUser = new java.util.HashMap<>();
                        newUser.put("userId", userId);
                        newUser.put("status", 0); // 0 = waiting/undecided
                        return waitListRef.document(docId)
                                .update("waitList", com.google.firebase.firestore.FieldValue.arrayUnion(newUser));
                    } else {
                        // User already exists, do nothing
                        return Tasks.forResult(null);
                    }
                });
    }

    /**
     * Removes the user from the event waitlist by finding and removing their
     * WaitlistUser object.
     */
    public Task<Void> leaveWaitingList(int eventId, int userId) {
        String docId = String.valueOf(eventId);
        return ensureWaitListExists(eventId)
                .continueWithTask(t -> waitListRef.document(docId).get())
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return Tasks.forException(task.getException() != null ? task.getException()
                                : new RuntimeException("Failed to read waitlist"));
                    }

                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot == null || !snapshot.exists()) {
                        return Tasks.forException(new RuntimeException("Waitlist not found"));
                    }

                    // Get current waitList
                    java.util.List<Object> waitList = (java.util.List<Object>) snapshot.get("waitList");
                    if (waitList == null || waitList.isEmpty()) {
                        return Tasks.forResult(null); // Nothing to remove
                    }

                    // Find and remove the user's entry
                    java.util.List<Object> updatedWaitList = new java.util.ArrayList<>();
                    for (Object obj : waitList) {
                        if (obj instanceof java.util.Map) {
                            java.util.Map<String, Object> userMap = (java.util.Map<String, Object>) obj;
                            Object uid = userMap.get("userId");
                            int existingUserId = 0;
                            if (uid instanceof Number) {
                                existingUserId = ((Number) uid).intValue();
                            } else if (uid instanceof String) {
                                try {
                                    existingUserId = Integer.parseInt((String) uid);
                                } catch (NumberFormatException e) {
                                    // ignore
                                }
                            }
                            // Keep all users except the one leaving
                            if (existingUserId != userId) {
                                updatedWaitList.add(obj);
                            }
                        }
                    }

                    // Update the waitlist with the filtered list
                    return waitListRef.document(docId).update("waitList", updatedWaitList);
                });
    }

    /**
     * Get all events that a user is part of (in any waitlist).
     * 
     * @param userId        The user ID to search for
     * @param listener      Success callback with list of Events
     * @param errorListener Failure callback
     */
    public void getEventsForUser(int userId,
            OnSuccessListener<java.util.List<Event>> listener,
            OnFailureListener errorListener) {
        // First get all waitlists
        waitListRef.get()
                .addOnSuccessListener(waitlistSnapshot -> {
                    // Collect event IDs where user is in the waitlist
                    java.util.List<Integer> eventIds = new java.util.ArrayList<>();

                    for (QueryDocumentSnapshot doc : waitlistSnapshot) {
                        try {
                            java.util.List<Object> waitList = (java.util.List<Object>) doc.get("waitList");
                            if (waitList != null) {
                                for (Object obj : waitList) {
                                    if (obj instanceof java.util.Map) {
                                        java.util.Map<String, Object> userMap = (java.util.Map<String, Object>) obj;
                                        Object uid = userMap.get("userId");
                                        int existingUserId = 0;
                                        if (uid instanceof Number) {
                                            existingUserId = ((Number) uid).intValue();
                                        } else if (uid instanceof String) {
                                            try {
                                                existingUserId = Integer.parseInt((String) uid);
                                            } catch (NumberFormatException e) {
                                                // ignore
                                            }
                                        }
                                        if (existingUserId == userId) {
                                            // Get event ID from document ID
                                            try {
                                                int eventId = Integer.parseInt(doc.getId());
                                                eventIds.add(eventId);
                                            } catch (NumberFormatException e) {
                                                // ignore
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("DB", "Error parsing waitlist document: " + doc.getId(), e);
                        }
                    }

                    // Now fetch all the events
                    if (eventIds.isEmpty()) {
                        listener.onSuccess(new java.util.ArrayList<>());
                        return;
                    }

                    getAllEvents(allEvents -> {
                        java.util.List<Event> userEvents = new java.util.ArrayList<>();
                        for (Event event : allEvents) {
                            if (eventIds.contains(event.getEventId())) {
                                userEvents.add(event);
                            }
                        }
                        listener.onSuccess(userEvents);
                    }, errorListener);
                })
                .addOnFailureListener(e -> {
                    Log.e("DB", "Error getting waitlists for user", e);
                    errorListener.onFailure(e);
                });
    }

    // Map a Firestore snapshot to Event while coercing types (e.g., String -> int)
    private Event safeMapEvent(DocumentSnapshot snapshot) {
        try {
            // Fast path
            return snapshot.toObject(Event.class);
        } catch (RuntimeException ex) {
            // Coerce where necessary and try again via reflection
            Map<String, Object> data = snapshot.getData();
            if (data == null) {
                throw ex;
            }

            // Coerce ID fields to Integer when persisted as String
            Object eid = data.get("eventId");
            if (eid instanceof String) {
                try {
                    data.put("eventId", Integer.parseInt((String) eid));
                } catch (NumberFormatException ignored) {
                    /* keep as-is */ }
            }
            Object oid = data.get("organizerId");
            if (oid instanceof String) {
                try {
                    data.put("organizerId", Integer.parseInt((String) oid));
                } catch (NumberFormatException ignored) {
                    /* keep as-is */ }
            }

            // Build Event instance using reflection to avoid setters (which update DB)
            Event ev = new Event();
            ev.setAutoUpdateDatabase(false);
            setIfPresent(ev, data, "eventId");
            setIfPresent(ev, data, "title");
            setIfPresent(ev, data, "imageUrl");
            setIfPresent(ev, data, "location");
            setIfPresent(ev, data, "capacity");
            setIfPresent(ev, data, "description");
            setIfPresent(ev, data, "organizerId");
            setIfPresent(ev, data, "qrCode");
            setIfPresent(ev, data, "eventStartDate");
            setIfPresent(ev, data, "eventEndDate");
            setIfPresent(ev, data, "registrationStartDate");
            setIfPresent(ev, data, "registrationEndDate");
            setIfPresent(ev, data, "eventStartTime");
            setIfPresent(ev, data, "eventEndTime");
            setIfPresent(ev, data, "fee");
            setIfPresent(ev, data, "tags");
            // waitingList is optional and has its own collection; skip here

            return ev;
        }
    }

    private void setIfPresent(Object target, Map<String, Object> data, String fieldName) {
        if (!data.containsKey(fieldName))
            return;
        try {
            java.lang.reflect.Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object value = data.get(fieldName);
            // Handle numeric conversions for int fields
            if (f.getType() == int.class) {
                if (value instanceof String) {
                    try {
                        value = Integer.parseInt((String) value);
                    } catch (NumberFormatException ignored) {
                        /* leave unchanged */ }
                } else if (value instanceof Long) {
                    // Firestore returns numbers as Long, convert to int
                    value = ((Long) value).intValue();
                } else if (value instanceof Integer) {
                    // Already an int, no conversion needed
                    value = (Integer) value;
                }
            }
            f.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.w("DB", "Field set failed for " + fieldName + ": " + e.getMessage());
        }
    }

    // WAITING LIST FUNCTIONS
    // -----------------------------------------------------------------------

    public void addWaitingList(WaitingList newWaitingList) {
        // Add the waitlist to DB (store under WaitListData)
        waitListRef.document(String.valueOf(newWaitingList.getEventId()))
                .set(newWaitingList)
                .addOnSuccessListener(unused -> {
                    Log.d("DB", "Successfully added waitlist: " + newWaitingList.getEventId());
                })
                .addOnFailureListener(e -> {
                    Log.e("DB", "Error adding waitlist " + newWaitingList.getEventId(), e);
                });
    }

    public void getWaitingList(int id,
            OnSuccessListener<WaitingList> listener,
            OnFailureListener errorListener) {
        waitListRef.document(String.valueOf(id))
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        try {
                            WaitingList wl = safeMapWaitingList(snapshot);
                            listener.onSuccess(wl);
                        } catch (RuntimeException ex) {
                            Log.e("DB", "Failed to deserialize WaitingList", ex);
                            errorListener.onFailure(ex);
                        }
                    } else {
                        listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DB", "Error getting waitlist", e);
                    errorListener.onFailure(e);
                });
    }

    private WaitingList safeMapWaitingList(DocumentSnapshot snapshot) {
        try {
            return snapshot.toObject(WaitingList.class);
        } catch (RuntimeException ex) {
            Map<String, Object> data = snapshot.getData();
            if (data == null)
                throw ex;

            Object eid = data.get("eventId");
            if (eid instanceof String) {
                try {
                    data.put("eventId", Integer.parseInt((String) eid));
                } catch (NumberFormatException ignored) {
                }
            }
            Object max = data.get("maxWaitlistSize");
            if (max instanceof String) {
                try {
                    data.put("maxWaitlistSize", Integer.parseInt((String) max));
                } catch (NumberFormatException ignored) {
                }
            }

            // Coerce arrays to ArrayList<WaitlistUser>
            Object wl = data.get("waitList");
            if (wl instanceof java.util.List) {
                java.util.ArrayList<com.example.pixel_events.waitinglist.WaitlistUser> coerced = new java.util.ArrayList<>();
                for (Object o : (java.util.List<?>) wl) {
                    if (o instanceof java.util.Map) {
                        try {
                            java.util.Map<String, Object> map = (java.util.Map<String, Object>) o;
                            int userId = 0;
                            int status = 0;
                            if (map.containsKey("userId")) {
                                Object uid = map.get("userId");
                                if (uid instanceof Number)
                                    userId = ((Number) uid).intValue();
                                else if (uid instanceof String)
                                    userId = Integer.parseInt((String) uid);
                            }
                            if (map.containsKey("status")) {
                                Object st = map.get("status");
                                if (st instanceof Number)
                                    status = ((Number) st).intValue();
                                else if (st instanceof String)
                                    status = Integer.parseInt((String) st);
                            }
                            coerced.add(new com.example.pixel_events.waitinglist.WaitlistUser(userId, status));
                        } catch (Exception e) {
                            Log.e("DB", "Failed to parse WaitlistUser map", e);
                        }
                    } else if (o instanceof Number) {
                        // Handle legacy data where waitList was just a list of user IDs
                        coerced.add(new com.example.pixel_events.waitinglist.WaitlistUser(((Number) o).intValue(), 0));
                    } else if (o instanceof String) {
                        try {
                            coerced.add(new com.example.pixel_events.waitinglist.WaitlistUser(
                                    Integer.parseInt((String) o), 0));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                data.put("waitList", coerced);
            }

            // Remove selected field handling as it's deprecated
            data.remove("selected");

            WaitingList wlObj = new WaitingList();
            wlObj.setAutoUpdateDatabase(false);
            setIfPresent(wlObj, data, "eventId");
            setIfPresent(wlObj, data, "status");
            setIfPresent(wlObj, data, "waitList");
            setIfPresent(wlObj, data, "maxWaitlistSize");
            return wlObj;
        }
    }

}
