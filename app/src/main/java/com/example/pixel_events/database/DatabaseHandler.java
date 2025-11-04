package com.example.pixel_events.database;

import android.util.Log;

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

    /**
     * Initialize the database
     */
    public DatabaseHandler() {
        db = FirebaseFirestore.getInstance();
        accRef = db.collection("AccountData");
        eventRef = db.collection("EventData");
    }

    public static synchronized DatabaseHandler getInstance() {
        if (instance == null) {
            instance = new DatabaseHandler();
        }
        return instance;
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
}