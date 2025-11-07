package com.example.pixel_events.profile;

import android.util.Log;

import com.example.pixel_events.database.DatabaseHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profile {
    private int id;                              // Unique userID
    private String accType;                      // { user, org, admin }
    private String userName;                     // User entered name
    private Date DOB;                            // Date of birth
    private String gender;                       // { male, female, other }
    private String email;                        // e-mail address
    private String city;                         // city of residence
    private String province;                     // province of residence
    private int phoneNum;                        // user's phone number
    private List<Integer> eventsUpcoming;        // list of upcoming eventID
    private List<Integer> eventsPart;            // list of past eventID that user participated in
    private List<Integer> eventsNPart;           // list of past eventID that user did not participate in
    private List<Boolean> notify;                // [All Notif, Win notif, Lose Notif]

    /**
     * Empty constructor for Firebase
     */
    public Profile() {}

    /**
     * Initialize
     */
    public Profile(int id, String accType, String userName, Date DOB, String gender,
                   String email, String city, String province, int phoneNum,
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

    // Getters
    public int getId() { return id; }
    public String getAccType() { return accType; }
    public String getUserName() { return userName; }
    public Date getDOB() { return DOB; }
    public String getGender() { return gender; }
    public String getEmail() { return email; }
    public String getCity() { return city; }
    public String getProvince() { return province; }
    public int getPhoneNum() { return phoneNum; }
    public List<Integer> getEventsUpcoming() { return eventsUpcoming; }
    public List<Integer> getEventsPart() { return eventsPart; }
    public List<Integer> getEventsNPart() { return eventsNPart; }
    public List<Boolean> getNotify() { return notify; }

    // Setters - These update both the local field and the database
    public void setId(int id) { 
        this.id = id; 
        updateDatabase("id", id);
    }
    
    public void setAccType(String accType) { 
        this.accType = accType; 
        updateDatabase("accType", accType);
    }
    
    public void setUserName(String userName) { 
        this.userName = userName; 
        updateDatabase("userName", userName);
    }
    
    public void setDOB(Date DOB) { 
        this.DOB = DOB; 
        updateDatabase("DOB", DOB);
    }
    
    public void setGender(String gender) { 
        this.gender = gender; 
        updateDatabase("gender", gender);
    }
    
    public void setEmail(String email) { 
        this.email = email; 
        updateDatabase("email", email);
    }
    
    public void setCity(String city) { 
        this.city = city; 
        updateDatabase("city", city);
    }
    
    public void setProvince(String province) { 
        this.province = province; 
        updateDatabase("province", province);
    }
    
    public void setPhoneNum(int phoneNum) { 
        this.phoneNum = phoneNum; 
        updateDatabase("phoneNum", phoneNum);
    }
    
    public void setEventsUpcoming(List<Integer> eventsUpcoming) { 
        this.eventsUpcoming = eventsUpcoming; 
        updateDatabase("eventsUpcoming", eventsUpcoming);
    }
    
    public void setEventsPart(List<Integer> eventsPart) { 
        this.eventsPart = eventsPart; 
        updateDatabase("eventsPart", eventsPart);
    }
    
    public void setEventsNPart(List<Integer> eventsNPart) { 
        this.eventsNPart = eventsNPart; 
        updateDatabase("eventsNPart", eventsNPart);
    }
    
    public void setNotify(List<Boolean> notify) { 
        this.notify = notify; 
        updateDatabase("notify", notify);
    }
    
    /**
     * Helper method to update a single field in the database
     * @param fieldName The name of the field to update
     * @param value The new value for the field
     */
    private void updateDatabase(String fieldName, Object value) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(fieldName, value);
        
        DatabaseHandler.getInstance().modifyAcc(this.id, updates, error -> {
            if (error != null) {
                Log.e("Profile", "Failed to update " + fieldName + ": " + error);
            }
        });
    }

    /**
     * Create and save profile to database
     * @param db DatabaseHandler reference
     */
    private void createProfile(DatabaseHandler db) {
        db.addAcc(this.id, this.accType, this.userName, this.DOB, this.gender,
                this.email, this.city, this.province, this.phoneNum, this.notify);
    }
}