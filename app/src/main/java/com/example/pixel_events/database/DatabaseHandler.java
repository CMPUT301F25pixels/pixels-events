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
import com.google.firebase.firestore.FieldValue;

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

    public void deleteAcc(int userID) {
        accRef.document(String.valueOf(userID))
                .delete()
                .addOnSuccessListener(unused -> Log.d("DB", "Deleted user: " + userID))
                .addOnFailureListener(e -> Log.e("DB", "Error deleting user " + userID, e));
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
                .continueWithTask(t -> waitListRef.document(docId)
                        .update("waitList", com.google.firebase.firestore.FieldValue.arrayUnion(userId)));
    }

    /**
     * Removes the user id from the event waitlist (no-op if absent).
     */
    public Task<Void> leaveWaitingList(int eventId, int userId) {
        String docId = String.valueOf(eventId);
        return ensureWaitListExists(eventId)
                .continueWithTask(t -> waitListRef.document(docId)
                        .update("waitList", com.google.firebase.firestore.FieldValue.arrayRemove(userId)));
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
            // Handle numeric strings for int fields
            if (f.getType() == int.class && value instanceof String) {
                try {
                    value = Integer.parseInt((String) value);
                } catch (NumberFormatException ignored) {
                    /* leave unchanged */ }
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

            // Coerce arrays to ArrayList<Integer>
            Object wl = data.get("waitList");
            if (wl instanceof java.util.List) {
                java.util.ArrayList<Integer> coerced = new java.util.ArrayList<>();
                for (Object o : (java.util.List<?>) wl) {
                    if (o instanceof Number)
                        coerced.add(((Number) o).intValue());
                    else if (o instanceof String) {
                        try {
                            coerced.add(Integer.parseInt((String) o));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                data.put("waitList", coerced);
            }
            Object sel = data.get("selected");
            if (sel instanceof java.util.List) {
                java.util.ArrayList<Integer> coerced = new java.util.ArrayList<>();
                for (Object o : (java.util.List<?>) sel) {
                    if (o instanceof Number)
                        coerced.add(((Number) o).intValue());
                    else if (o instanceof String) {
                        try {
                            coerced.add(Integer.parseInt((String) o));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                data.put("selected", coerced);
            }

            WaitingList wlObj = new WaitingList();
            wlObj.setAutoUpdateDatabase(false);
            setIfPresent(wlObj, data, "eventId");
            setIfPresent(wlObj, data, "status");
            setIfPresent(wlObj, data, "waitList");
            setIfPresent(wlObj, data, "selected");
            setIfPresent(wlObj, data, "maxWaitlistSize");
            return wlObj;
        }
    }

}
