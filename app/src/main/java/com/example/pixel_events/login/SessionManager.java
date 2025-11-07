package com.example.pixel_events.login;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFS_NAME = "pixels_prefs";
    private static final String KEY_ROLE = "current_role";
    private static final String KEY_PROFILE_ID = "current_profile_id";
    private static final String KEY_ENTRANT_ID = "entrant_profile_id";

    // Roles from spec
    public static final String ROLE_ENTRANT = "user";  // entrant
    public static final String ROLE_ORGANIZER = "org";
    public static final String ROLE_ADMIN = "admin";

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Start a session for any role
    public static void startSession(Context context, String role, int profileId) {
        SharedPreferences.Editor editor = prefs(context).edit();
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_PROFILE_ID, String.valueOf(profileId));

        if (ROLE_ENTRANT.equals(role)) {
            editor.putString(KEY_ENTRANT_ID, String.valueOf(profileId));
        }

        editor.apply();
    }

    public static boolean hasActiveSession(Context context) {
        return getRole(context) != null && getProfileId(context) != null;
    }

    public static String getRole(Context context) {
        return prefs(context).getString(KEY_ROLE, null);
    }

    public static String getProfileId(Context context) {
        return prefs(context).getString(KEY_PROFILE_ID, null);
    }

    public static String getEntrantId(Context context) {
        return prefs(context).getString(KEY_ENTRANT_ID, null);
    }

    public static void clearSession(Context context) {
        prefs(context).edit()
                .remove(KEY_ROLE)
                .remove(KEY_PROFILE_ID)
                .apply();
    }
}
