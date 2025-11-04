package com.example.pixel_events;

import android.os.CpuHeadroomParams;
import android.provider.CalendarContract;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AccountInfo {
    public int id;                              // Unique userID
    public String accType;                      // { user, org, admin }
    public String userName;                        // User entered name
    public Date DOB;                            // Date of birth
    public String gender;                       // { male, female, other }
    public String email;                        // e-mail address
    public String city;                         // city of residence
    public String province;                     // province of residence
    public int phoneNum;                        // user's phone number
    public List<Integer> eventsUpcoming;        // list of upcoming eventID
    public List<Integer> eventsPart;            // list of past eventID that user participated in
    public List<Integer> eventsNPart;           // list of past eventID that user did not participate in
    public List<Boolean> notify;                 // [All Notif, Win notif, Lose Notif]

    /**
     * Empty constructor for Firebase
     */
    public AccountInfo() {}

    /**
     * Initialize
     */
    public AccountInfo(int id, String accType, String  userName, Date DOB, String gender,
                              String email, String city, String  province, int phoneNum,
                              List<Boolean> notify) {
        // Assign all variables
        this.id = id;
        this.accType = accType;
        this.userName = userName;
        this.DOB = DOB;
        this.gender = gender;
        this.email = email;
        this.city = city;
        this.province = province;
        this.phoneNum = phoneNum;
        this.notify = notify;

        // assign an empty events list
        this.eventsUpcoming = new ArrayList<>();
        this.eventsPart = new ArrayList<>();
        this.eventsNPart = new ArrayList<>();
    }
}

/*
 * Class:
 *      AccountInfo
 *
 * Responsibilities:
 *      Setup an object to easily use account data
 *
 * Collaborators:
 *      DataBaseHandler
 */
