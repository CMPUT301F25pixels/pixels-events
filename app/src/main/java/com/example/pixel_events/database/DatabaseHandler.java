package com.example.pixel_events.database;

import android.util.Log;

import com.example.pixel_events.events.Event;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.waitingList.WaitingList;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DatabaseHandler {
    private static DatabaseHandler instance;
    private final FirebaseFirestore db;
    private final CollectionReference accRef;
    private final CollectionReference eventRef;
    private final CollectionReference waitListRef;

    private boolean isOfflineMode = false;

    /**
     * Initialize the database
     */
    public DatabaseHandler() {
        this(false);
    }

    /**
     * Initialize the database with specified mode
     * @param offlineMode If true, connects to Firebase emulator; if false, connects to real Firebase
     */
    public DatabaseHandler(boolean offlineMode) {
        db = FirebaseFirestore.getInstance();
        this.isOfflineMode = offlineMode;
        
        if (offlineMode) {
            // Connect to Firebase emulator
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
     * @param offlineMode If true, uses Firebase emulator; if false, uses real Firebase
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

    /**
     * Check if running in offline mode (emulator)
     * @return true if connected to emulator, false if connected to real Firebase
     */
    public boolean isOfflineMode() {
        return isOfflineMode;
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




    // ACCOUNT INFO FUNCTIONS ---------------------------------------------------------------------

    /**
     * Add a new account to the database
     * @param id Unique user ID
     * @param accType Account type (user, org, admin)
     * @param userName User's name
     * @param DOB Date of birth
     * @param gender User's gender
     * @param email Email address
     * @param city City of residence
     * @param province Province of residence
     * @param phoneNum Phone number
     * @param notify Notification preferences
     */
    public void addAcc(int id, String accType, String userName, Date DOB, String gender,
                       String email, String city, String province, int phoneNum,
                       List<Boolean> notify) {

        // Create a new Profile
        Profile newUser = new Profile(id, accType, userName, DOB, gender, email,
                city, province, phoneNum, notify);

        // Add the account to DB
        accRef.document(String.valueOf(newUser.getId()))
                .set(newUser)
                .addOnSuccessListener(unused -> Log.d("DB", "Added User: " + newUser.getId()))
                .addOnFailureListener(e -> Log.w("DB", "Error adding user!", e));
    }

    /**
     * Retrieve account information from the database
     * @param userId Unique user ID
     * @param listener Success listener to handle the retrieved Profile
     * @param errorListener Failure listener to handle errors
     * 
     * The callback will return profile of type Profile. If the userID does not exist, it will return null.
     * 
     * Usage example:
     *     db.getAcc("123", profile -> {
     *         if (profile != null) {
     *            // Handle retrieved profile
     *        } else {
     *           // Handle case where profile does not exist
     *       }
     *    }, error -> {
     *        // Handle error
     *   });
     */
    public void getAcc(String userId, OnSuccessListener<Profile> listener, OnFailureListener errorListener) {
        accRef.document(String.valueOf(userId))
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
     * Modify account information in the database
     * @param userID Unique user ID
     * @param updates Map of field names to new values
     * @param errorCallback Optional callback for handling errors (receives error message)
     * 
     * Usage example:
     *      Map<String, Object> updates = new HashMap<>();
     *      updates.put("userName", "New Name");
     *      updates.put("city", "New City");
     *      db.modifyAcc(123, updates, error -> {
     *          if (error != null) {
     *              // Handle error
     *          }
     *      });
     */
    public void modifyAcc(int userID, Map<String, Object> updates, Consumer<String> errorCallback) {
        // First check if the document exists
        accRef.document(String.valueOf(userID))
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        // Document exists, perform update
                        accRef.document(String.valueOf(userID))
                                .update(updates)
                                .addOnSuccessListener(unused -> {
                                    Log.d("DB", "Updated user " + userID + " with " + updates.size() + " field(s)");
                                    if (errorCallback != null) {
                                        errorCallback.accept(null); // Success, no error
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("DB", "Error updating user " + userID, e);
                                    if (errorCallback != null) {
                                        errorCallback.accept("Failed to update: " + e.getMessage());
                                    }
                                });
                    } else {
                        // Document doesn't exist
                        String errorMsg = "User ID " + userID + " does not exist in database";
                        Log.e("DB", errorMsg);
                        if (errorCallback != null) {
                            errorCallback.accept(errorMsg);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DB", "Error checking if user exists: " + userID, e);
                    if (errorCallback != null) {
                        errorCallback.accept("Error checking user existence: " + e.getMessage());
                    }
                });
    }

    /**
     * Modify account information in the database (simplified version without error callback)
     * @param userID Unique user ID
     * @param updates Map of field names to new values
     */
    public void modifyAcc(int userID, Map<String, Object> updates) {
        modifyAcc(userID, updates, null);
    }

    /**
     * Delete the user's account from the database
     * @param userID Unique user ID
     */
    public void deleteAcc(int userID) {
        accRef.document(String.valueOf(userID))
                .delete()
                .addOnSuccessListener(unused ->
                        Log.d("DB", "Deleted user: " + userID))
                .addOnFailureListener(e ->
                        Log.e("DB", "Error deleting user " + userID, e));
    }

    // EVENT INFO FUNCTIONS -----------------------------------------------------------------------

    /**
     * Add a new event to the database
     * @param eventId Unique event ID
     * @param organizerId ID of the organizer
     * @param title Event title
     * @param imageUrl Event image URL
     * @param location Event location
     * @param capacity Event capacity
     * @param description Event description
     * @param fee Event fee (e.g., "Free" or a price string)
     * @param eventStartDate Event start date
     * @param eventEndDate Event end date
     * @param registrationStartDate Registration start date
     * @param registrationEndDate Registration end date
     */
    public void addEvent(int eventId, int organizerId, String title, String imageUrl,
                         String location, String capacity, String description, String fee,
                         String eventStartDate, String eventEndDate, String eventStartTime, String eventEndTime,
                         String registrationStartDate, String registrationEndDate) {

        // Create a new Event (includes fee)
        Event newEvent = new Event(eventId, organizerId, title, imageUrl, location,
            capacity, description, fee, eventStartDate, eventEndDate,
            eventStartTime, eventEndTime, registrationStartDate, registrationEndDate);

        // Add the event to DB
        eventRef.document(String.valueOf(newEvent.getEventId()))
                .set(newEvent)
                .addOnSuccessListener(unused -> Log.d("DB", "Added Event: " + newEvent.getEventId()))
                .addOnFailureListener(e -> Log.w("DB", "Error adding event!", e));
    }

    /**
     * Retrieve event information from the database
     * @param eventId Unique event ID
     * @param listener Success listener to handle the retrieved Event
     * @param errorListener Failure listener to handle errors
     * 
     * The callback will return event of type Event. If the eventID does not exist, it will return null.
     * 
     * Usage example:
     *     db.getEvent("123", event -> {
     *         if (event != null) {
     *            // Handle retrieved event
     *        } else {
     *           // Handle case where event does not exist
     *       }
     *    }, error -> {
     *        // Handle error
     *   });
     */
    public void getEvent(String eventId, OnSuccessListener<Event> listener, OnFailureListener errorListener) {
        eventRef.document(String.valueOf(eventId))
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        listener.onSuccess(snapshot.toObject(Event.class));
                    } else {
                        listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DB", "Error getting event", e);
                    errorListener.onFailure(e);
                });
    }

    /**
     * Modify event information in the database
     * @param eventID Unique event ID
     * @param updates Map of field names to new values
     * @param errorCallback Optional callback for handling errors (receives error message)
     * 
     * Usage example:
     *      Map<String, Object> updates = new HashMap<>();
     *      updates.put("title", "Updated Event Title");
     *      updates.put("location", "New Location");
     *      db.modifyEvent(123, updates, error -> {
     *          if (error != null) {
     *              // Handle error
     *          }
     *      });
     */
    public void modifyEvent(int eventID, Map<String, Object> updates, Consumer<String> errorCallback) {
        // First check if the document exists
        eventRef.document(String.valueOf(eventID))
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        // Document exists, perform update
                        eventRef.document(String.valueOf(eventID))
                                .update(updates)
                                .addOnSuccessListener(unused -> {
                                    Log.d("DB", "Updated event " + eventID + " with " + updates.size() + " field(s)");
                                    if (errorCallback != null) {
                                        errorCallback.accept(null); // Success, no error
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("DB", "Error updating event " + eventID, e);
                                    if (errorCallback != null) {
                                        errorCallback.accept("Failed to update: " + e.getMessage());
                                    }
                                });
                    } else {
                        // Document doesn't exist
                        String errorMsg = "Event ID " + eventID + " does not exist in database";
                        Log.e("DB", errorMsg);
                        if (errorCallback != null) {
                            errorCallback.accept(errorMsg);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DB", "Error checking if event exists: " + eventID, e);
                    if (errorCallback != null) {
                        errorCallback.accept("Error checking event existence: " + e.getMessage());
                    }
                });
    }

    /**
     * Modify event information in the database (simplified version without error callback)
     * @param eventID Unique event ID
     * @param updates Map of field names to new values
     */
    public void modifyEvent(int eventID, Map<String, Object> updates) {
        modifyEvent(eventID, updates, null);
    }

    /**
     * Delete an event from the database
     * @param eventID Unique event ID
     */
    public void deleteEvent(int eventID) {
        eventRef.document(String.valueOf(eventID))
                .delete()
                .addOnSuccessListener(unused ->
                        Log.d("DB", "Deleted event: " + eventID))
                .addOnFailureListener(e ->
                        Log.e("DB", "Error deleting event " + eventID, e));
    }

    // WAITING LIST FUNCTIONS ------------------------------------------------------------------------
    /**
     * Creates a new waiting list document for an event if it doesn't already exist.
     *
     * @param eventId  The unique identifier of the event (used as the document ID in WaitListData).
     * @param capacity The maximum number of people allowed in the waiting list.
     * @return A Task<Void> that completes when the document is successfully created or merged.
     *
     * <p>This method initializes default fields for the waiting list:
     * <ul>
     *     <li>eventId</li>
     *     <li>status ("waiting")</li>
     *     <li>maxWaitlistSize (capacity)</li>
     *     <li>capacity (duplicate for query convenience)</li>
     *     <li>waitList (empty array)</li>
     *     <li>selected (empty array)</li>
     * </ul>
     * The method uses {@link com.google.firebase.firestore.SetOptions#merge()} to ensure that
     * existing fields are preserved if the document already exists.
     */
    public Task<Void> addWaitingList(String eventId, int capacity) {
        DocumentReference doc = waitListRef.document(eventId);

        Map<String, Object> init = new HashMap<>();
        init.put("eventId", eventId);
        init.put("status", "waiting");
        init.put("maxWaitlistSize", capacity);  // store capacity here
        init.put("capacity", capacity);         // optional duplicate for readability/queries
        init.put("waitList", new ArrayList<String>());
        init.put("selected", new ArrayList<String>());

        return doc.set(init, SetOptions.merge());
    }

    /**
     * Retrieves the waiting list object for a given event.
     *
     * @param eventId        The unique identifier of the event whose waiting list should be fetched.
     * @param listener       A success callback that returns a {@link com.example.pixel_events.waitingList.WaitingList}
     *                       object, or null if the document does not exist.
     * @param errorListener  A failure callback invoked if the Firestore operation fails.
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * db.getWaitingList("12345", waitList -> {
     *     if (waitList != null) {
     *         Log.d("WAITLIST", "Loaded waitlist for event: " + waitList.getEventId());
     *     } else {
     *         Log.d("WAITLIST", "No waitlist found for this event.");
     *     }
     * }, e -> Log.e("WAITLIST", "Failed to fetch waitlist", e));
     * }</pre>
     */
    public void getWaitingList(String eventId, OnSuccessListener<WaitingList> listener, OnFailureListener errorListener) {
        waitListRef.document(String.valueOf(eventId))
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) {
                        listener.onSuccess(null); // no doc yet
                        return;
                    }
                    WaitingList wl = snap.toObject(WaitingList.class);
                    listener.onSuccess(wl);
                })
                .addOnFailureListener(e -> {
                    Log.e("DB", "Error getting waiting list", e);
                    errorListener.onFailure(e);
                });
    }
    /**
     * Attempts to add a user to the waiting list for a given event.
     *
     * @param eventId the identifier of the event whose waiting list to modify; must match
     *                the document ID in the {@code WaitListData} collection.
     * @param userId  the unique identifier of the user to add to the waiting list.
     * @return a {@link com.google.android.gms.tasks.Task} that completes when the update is applied.
     *         The task is successful even if the user was already present (no-op). The task fails if
     *         the waiting list document does not exist or if Firestore update fails.
     */
    public Task<Void> joinWaitingList(String eventId, String userId) {
        DocumentReference doc = waitListRef.document(eventId);

        return doc.get().continueWithTask(task -> {
            DocumentSnapshot snap = task.getResult();
            if (snap == null || !snap.exists()) {
                throw new Exception("Waiting list does not exist for eventId: " + eventId);
            }

            List<String> waitList = (List<String>) snap.get("waitList");
            if (waitList == null) waitList = new ArrayList<>();

            if (waitList.contains(userId)) {
                Log.d("DB", "User already in waitlist: " + userId);
                // Return a completed task to signal success without changes
                return Tasks.forResult(null);
            }

            waitList.add(userId);
            long newCount = waitList.size();

            Map<String, Object> updates = new HashMap<>();
            updates.put("waitList", waitList);
            updates.put("waitlistCount", newCount);

            return doc.update(updates);
        });
    }

    /**
     * Attempts to remove a user from the waiting list for a given event.
     *
     * <p><b>Behavior:</b>
     * <ul>
     *   <li>Fails if the waiting list document for the event does not exist.</li>
     *   <li>No-ops (successful Task with no changes) if the user is not currently in the list.</li>
     *   <li>Otherwise, removes the user from {@code waitList} and updates {@code waitlistCount} to the list size.</li>
     * </ul>
     *
     * <p><b>Important:</b> This method performs a read-then-write (no Firestore transaction).
     * In highly concurrent scenarios, two writers could overwrite each other. If you need
     * strict consistency under contention, use a transaction.
     *
     * @param eventId the identifier of the event whose waiting list to modify; must match
     *                the document ID in the {@code WaitListData} collection.
     * @param userId  the unique identifier of the user to remove from the waiting list.
     * @return a {@link com.google.android.gms.tasks.Task} that completes when the update is applied.
     *         The task is successful even if the user was not present (no-op). The task fails if
     *         the waiting list document does not exist or if Firestore update fails.
     */
    public Task<Void> leaveWaitingList(String eventId, String userId) {
        DocumentReference doc = waitListRef.document(eventId);

        return doc.get().continueWithTask(task -> {
            DocumentSnapshot snap = task.getResult();
            if (snap == null || !snap.exists()) {
                throw new Exception("Waiting list does not exist for eventId: " + eventId);
            }

            List<String> waitList = (List<String>) snap.get("waitList");
            if (waitList == null) waitList = new ArrayList<>();

            if (!waitList.contains(userId)) {
                Log.d("DB", "User not found in waitlist: " + userId);
                // Return a completed task to signal success without changes
                return Tasks.forResult(null);
            }

            waitList.remove(userId);
            long newCount = waitList.size();

            Map<String, Object> updates = new HashMap<>();
            updates.put("waitList", waitList);
            updates.put("waitlistCount", newCount);

            return doc.update(updates);
        });
    }


}

/*
 * Class:
 *      DatabaseHandler
 *
 * Responsibilities:
 *      Initialize DB
 *      Account Data:
 *          Add a new account (addAcc)
 *          Get account information (getAcc)
 *          Modify account fields (modifyAcc)
 *          Delete an account (deleteAcc)
 *      Event Data:
 *          Add a new event (addEvent)
 *          Get event information (getEvent)
 *          Modify event fields (modifyEvent)
 *          Delete an event (deleteEvent)
 *
 * Collaborators:
 *      Database
 *      Profile
 *      Event
 *
 */
