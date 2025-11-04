package com.example.pixel_events;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class DataBaseHandler {
    private final FirebaseFirestore db;
    private final CollectionReference accRef;

    /**
     * Initialize the database
     */
    public DataBaseHandler() {
        db = FirebaseFirestore.getInstance();
        accRef = db.collection("AccountData");
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

        // Add the account to DB
        accRef.document(String.valueOf(newUser.id))
                .set(newUser)
                .addOnSuccessListener(unused -> Log.d("DB", "Added User: " + newUser.id))
                .addOnFailureListener(e -> Log.w("DB", "Error adding user!", e));
    }

    /**
     * Sets up a listener to get real-time info
     *
     * @param id Unique user ID
     */
    public void getAcc(int id, Consumer<AccountInfo> acc) {
        // Find the account
        accRef.document(String.valueOf(id))
                .get()

                // Account found
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        acc.accept(snapshot.toObject(AccountInfo.class));
                    } else {
                        acc.accept(null);           // return null if some error
                    }
                })
                // Account not found
                .addOnFailureListener(e -> {
                    Log.e("DB", "Error getting account", e);
                    acc.accept(null);
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
        String field;
        // Store the new even value as a list
        if (eventStatus == 0) field = "eventsUpcoming";
        else if (eventStatus == 1) field = "eventsPart";
        else field = "eventsNPart";

        accRef.document(String.valueOf(userID))
                .update(field, FieldValue.arrayUnion(eventID))
                .addOnSuccessListener(unused ->
                        Log.d("DB", "Added event " + eventID + " for user " + userID))
                .addOnFailureListener(e ->
                        Log.e("DB", "Error adding event for user " + userID, e));
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
