package com.example.pixel_events;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class DataBaseHandler {
    private final FirebaseFirestore db;

    /**
     * Initialize the database
     */
    public DataBaseHandler() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     *  Add a new entry for user data
     */
    public void addAcc(int id, String accType, String userName, Date DOB, String gender,
                        String email, String city, String  province, int phoneNum,
                        Boolean notify) {

        // Create a new Account
        AccountInfo newUser = new AccountInfo(id, accType, userName, DOB, gender, email,
                city,   province,  phoneNum, notify);

        // Add to database
        db.collection("AccountData")
                .document(String.valueOf(id))       // docID = string(userID)
                .set(newUser)

                // Check for Success
                .addOnSuccessListener(unused ->
                        Log.d("DB", "Added User:" + id))

                // Failed to add
                .addOnFailureListener(e ->
                        Log.w("DB", "Error adding user!", e));

    }

    /**
     * Get account information for a given user
     *
     * @param id Unique user ID
     */
    public void getAccInfo(int id, Consumer<AccountInfo> callback) {
        // Find the account
        db.collection("AccountData")
                .document(String.valueOf(id))
                .get()

                .addOnCompleteListener(task -> {
                    // Account found
                    if (task.isComplete()) {
                        AccountInfo userInfo = task.getResult().toObject(AccountInfo.class);
                        callback.accept(userInfo);

                    // Account no found
                    } else {
                        callback.accept(null);
                    }
                });
    }
    // NOTE:
    // Usage for this function should be as such:
    //      db.getAccInfo(10, accountData -> {
    //          < use the data however needed >
    //      });

    /**
     * Add an event to the list for a given user
     *
     * @param userID Unique user ID
     * @param eventID Unique event ID
     * @param eventStatus 0: upcoming, 1: past and participated, 2: past and not participated
     */
    public void addEvent(int userID, int eventID, int eventStatus) {
        // Store the new even value as a list
        List<Integer> newEvent = new ArrayList<>();
        newEvent.add(eventID);
        newEvent.add(eventStatus);

        // Find the account
        var ref = db.collection("AccountData").document(String.valueOf(userID));

        // Append the new event
        ref.update("events", FieldValue.arrayUnion(newEvent));
    }
}

/*
 * Class:
 *      DataBaseHandler
 *
 * Responsibilities:
 *      Initialize DB
 *      Get account information
 *      Add a new account
 *      Add new events for users
 *
 *
 * Collaborators:
 *      Database
 *      AccountInfo
 *
 */
