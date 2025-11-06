package com.example.pixel_events.database;

import android.util.Log;

import com.example.pixel_events.events.Event;
import com.example.pixel_events.profile.Profile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DatabaseHandler {
    private static DatabaseHandler instance;
    private final FirebaseFirestore db;
    private final CollectionReference accRef;
    private final CollectionReference eventRef;
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