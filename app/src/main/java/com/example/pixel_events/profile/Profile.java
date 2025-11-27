package com.example.pixel_events.profile;

import android.util.Log;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.utils.Validator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profile {
    private int userId; // Unique userId (for backwards compatibility)
    private String role; // { user, org, admin }
    private String userName; // User entered name
    private String gender; // { male, female, other }
    private String email; // e-mail address
    private String phoneNum; // user's phone number
    private String postalcode; // user's postal code
    private String province; // user's province
    private String city; // user's city
    private List<Boolean> notify; // [All Notif, Win notif, Lose Notif]
    private boolean autoUpdateDatabase = true;

    public Profile() {
    }

    // New constructor with address fields
    public Profile(int userid, String role, String userName, String gender,
            String email, String phoneNum, String postalcode,
            String province, String city, List<Boolean> notify) {
        // Validate required fields
        Validator.validateNotEmpty(role, "Role");
        Validator.validateNotEmpty(userName, "User Name");
        Validator.validateNotEmpty(gender, "Gender");
        Validator.validateNotEmpty(email, "Email");

        if (userid <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }

        // Assign all variables
        this.userId = userid;
        this.role = role;
        this.userName = userName;
        this.gender = gender;
        this.email = email;
        this.notify = notify;
        this.phoneNum = phoneNum;
        this.postalcode = postalcode;
        this.province = province;
        this.city = city;
    }

    public void setAutoUpdateDatabase(boolean autoUpdate) {
        this.autoUpdateDatabase = autoUpdate;
    }

    public void saveToDatabase() {
        if (!autoUpdateDatabase) {
            return;
        }
        try {
            DatabaseHandler db = DatabaseHandler.getInstance();
            db.addAcc(this);
        } catch (Exception e) {
            Log.e("Event", "Failed to save event to database", e);
        }
    }

    private void updateDatabase(String fieldName, Object value) {
        if (!autoUpdateDatabase || this.userId <= 0) {
            return;
        }

        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put(fieldName, value);

            DatabaseHandler.getInstance().modify(DatabaseHandler.getInstance().getAccountCollection(),
                    this.userId, updates, error -> {
                        if (error != null) {
                            Log.e("Profile", "Failed to update " + fieldName + ": " + error);
                        }
                    });
        } catch (Exception e) {
            Log.e("Profile", "Failed to access database for update", e);
        }

    }

    // Getters
    public int getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public String getUserName() {
        return userName;
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public List<Boolean> getNotify() {
        return notify;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    // Setters / Modify Profile
    public void setUserId(int userId) {
        this.userId = userId;
        updateDatabase("id", userId);
    }

    public void setRole(String role) {
        this.role = role;
        updateDatabase("role", role);
    }

    public void setUserName(String userName) {
        this.userName = userName;
        updateDatabase("userName", userName);
    }

    public void setGender(String gender) {
        this.gender = gender;
        updateDatabase("gender", gender);
    }

    public void setEmail(String email) {
        this.email = email;
        updateDatabase("email", email);
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
        updateDatabase("phoneNum", phoneNum);
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
        updateDatabase("postalcode", postalcode);
    }

    public void setProvince(String province) {
        this.province = province;
        updateDatabase("province", province);
    }

    public void setCity(String city) {
        this.city = city;
        updateDatabase("city", city);
    }

    public void setNotify(List<Boolean> notify) {
        this.notify = notify;
        updateDatabase("notify", notify);
    }
}
