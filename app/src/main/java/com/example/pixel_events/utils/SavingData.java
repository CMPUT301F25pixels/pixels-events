package com.example.pixel_events.utils;

import android.content.Context;
import android.util.Log;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.profile.Profile;
import com.example.pixel_events.waitinglist.WaitlistUser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Utility class to export profile data to a CSV file.
 * Construct with a list of profile IDs; call exportProfiles to perform asynchronous export.
 */
public class SavingData {
    private final List<WaitlistUser> waitlistUsers;
    private final List<Profile> loadedProfiles = Collections.synchronizedList(new ArrayList<>());
    private final DatabaseHandler db = DatabaseHandler.getInstance();

    public SavingData(List<WaitlistUser> profiles) {
        this.waitlistUsers = new ArrayList<>(profiles);
    }

    /**
     * Asynchronously loads profiles then writes a CSV.
     * @param context Android context for storage path
     * @param eventId event identifier used in filename
     * @param callback receives final status message (success path or error / nothing to export)
     */
    public void exportProfiles(Context context, int eventId, Consumer<String> callback) {
        if (context == null) {
            if (callback != null) callback.accept("Context unavailable");
            return;
        }
        if (waitlistUsers.isEmpty()) {
            if (callback != null) callback.accept("Nothing to export");
            return;
        }
        AtomicInteger remaining = new AtomicInteger(waitlistUsers.size());
        for (WaitlistUser user : waitlistUsers) {
            if (user == null) {
                if (remaining.decrementAndGet() == 0) finishExport(context, eventId, callback);
                continue;
            }
            int id = user.getUserId();
            db.getProfile(id, profile -> {
                if (profile != null) {
                    loadedProfiles.add(profile);
                }
                if (remaining.decrementAndGet() == 0) finishExport(context, eventId, callback);
            }, e -> {
                Log.e("SavingData", "Failed to fetch profile " + id, e);
                if (remaining.decrementAndGet() == 0) finishExport(context, eventId, callback);
            });
        }
    }

    private void finishExport(Context context, int eventId, Consumer<String> callback) {
        if (loadedProfiles.isEmpty()) {
            if (callback != null) callback.accept("Nothing to export");
            return;
        }
        File appFilesDir = context.getExternalFilesDir(null); // App-specific external storage
        if (appFilesDir == null) {
            if (callback != null) callback.accept("Storage unavailable");
            return;
        }
        // Create subdirectory as originally specified: pixel-event
        File exportDir = new File(appFilesDir, "pixel-event");
        if (!exportDir.exists() && !exportDir.mkdirs()) {
            if (callback != null) callback.accept("Failed to create export directory");
            return;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "event_" + eventId + "_" + timeStamp + ".csv";
        File outFile = new File(exportDir, fileName);

        try (FileWriter writer = new FileWriter(outFile)) {
            writer.write("UserID,Name,Email,Phone Number,Gender,City,Province,Postal Code,Status\n");
            for (Profile p : loadedProfiles) {
                // Find status for this profile
                String statusLabel = "Didn't Choose";
                for (WaitlistUser user : waitlistUsers) {
                    if (user.getUserId() == p.getUserId()) {
                        int status = user.getStatus();
                        if (status == 2) statusLabel = "Accepted";
                        else if (status == 3) statusLabel = "Declined";
                        else if (status == 1) statusLabel = "Selected";
                        else if (status == 0) statusLabel = "Waiting";
                        else statusLabel = "Didn't Choose";
                        break;
                    }
                }
                writer.write(csvField(p.getUserId()) + "," +
                        csvField(p.getUserName()) + "," +
                        csvField(p.getEmail()) + "," +
                        csvField(p.getPhoneNum()) + "," +
                        csvField(p.getGender()) + "," +
                        csvField(p.getCity()) + "," +
                        csvField(p.getProvince()) + "," +
                        csvField(p.getPostalcode()) + "," +
                        csvField(statusLabel) + "\n");
            }
        } catch (IOException e) {
            if (callback != null) callback.accept("Failed to export: " + e.getMessage());
            return;
        }
        if (callback != null) callback.accept("Exported to: " + outFile.getAbsolutePath());
    }

    private String csvField(Object val) {
        if (val == null) return "null";
        String s = String.valueOf(val).trim();
        if (s.isEmpty()) return "null";
        if (s.contains("\"") || s.contains(",") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
        }
        if (s.contains(",") || s.contains("\n")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}
