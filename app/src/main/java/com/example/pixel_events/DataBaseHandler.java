package com.example.pixel_events;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import org.checkerframework.common.aliasing.qual.Unique;

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


    // ACCOUNT INFO FUNCTIONS ---------------------------------------------------------------------

    /**
     *  Add a new entry for user data
     */
    public void addAcc(int id, String accType, String userName, Date DOB, String gender,
                        String email, String city, String  province, int phoneNum,
                        List<Boolean> notify) {

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

    /**
     * Update Username
     * @param userID Unique userID
     * @param newUSN New Username
     */
    public void updateUSN(int userID, String newUSN) {
        accRef.document(String.valueOf(userID))
                .update("userName", newUSN);
    }

    /**
     * Update Date of Birth
     * @param userID Unique userID
     * @param newDate New Date of Birth
     */
    public void updateDOB(int userID, Date newDate) {
        accRef.document(String.valueOf(userID))
                .update("DOB", newDate);
    }

    /**
     * Update Gender
     * @param userID Unique userID
     * @param newGender New gender
     */
    public void updateGender(int userID, String newGender) {
        accRef.document(String.valueOf(userID))
                .update("gender", newGender);
    }

    /**
     * Update E-mail
     * @param userID Unique userID
     * @param newEmail New E-mail
     */
    public void updateEmail(int userID, String newEmail) {
        accRef.document(String.valueOf(userID))
                .update("email", newEmail);
    }

    /**
     * Update City of residence
     * @param userID Unique userID
     * @param newCity New City name
     */
    public void updateCity(int userID, String newCity) {
        accRef.document(String.valueOf(userID))
                .update("city", newCity);
    }

    /**
     * Update Province of residence
     * @param userID Unique userID
     * @param newProv New Province name
     */
    public void updateProvince(int userID, String newProv) {
        accRef.document(String.valueOf(userID))
                .update("province", newProv);
    }

    /**
     * Update Phone number
     * @param userID Unique userID
     * @param newPhone New Phone number
     */
    public void updatePhone(int userID, int newPhone) {
        accRef.document(String.valueOf(userID))
                .update("phoneNum", newPhone);
    }

    /**
     * Update Notification status
     * @param userID Unique userID
     * @param newNotify New Notification Status
     * @param idx 0: All Notif, 1: Win Notif, 2: Lose Notif
     */
    public void updateNotify(int userID, boolean newNotify, int idx) {
        // return if index out of range
        if (idx > 2 || idx < 0) {
            return;
        }

        // index in range:
        DocumentReference notiRef = accRef.document(String.valueOf(userID));

        // get the notify list
        notiRef.get().addOnSuccessListener(snapshot -> {
            List<Boolean> notifyList = (List<Boolean>) snapshot.get("notify");

            if (notifyList != null && idx < notifyList.size()) {

                // update only the selected index
                notifyList.set(idx, newNotify);
                notiRef.update("notify", notifyList);
            }
        });
    }

    /**
     * Delete the user's account
     * @param userID Unique userID
     */
    public void delAccount(int userID) {
        accRef.document(String.valueOf(userID)).delete();
    }

    // EVENTS INFO FUNCTIONS ----------------------------------------------------------------------
    // TODO:
}

/*
 * Class:
 *      DataBaseHandler
 *
 * Responsibilities:
 *      Initialize DB
 *      Account Data:
 *          Get account information
 *          Add a new account
 *          Add new events for users
 *          Update any of the account fields
 *          Delete an account
 *      Events Data:
 *          TODO
 *
 * Collaborators:
 *      Database
 *      AccountInfo
 *
 */
