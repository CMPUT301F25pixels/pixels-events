package com.example.pixel_events.utils;

import android.content.Context;
import android.util.Log;

import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.profile.Profile;

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
    private final List<Integer> profileIds;
    private final List<Profile> loadedProfiles = new ArrayList<>();
    private final DatabaseHandler db = DatabaseHandler.getInstance();

    public SavingData(List<Integer> profileIds) {
        this.profileIds = profileIds != null ? profileIds : Collections.emptyList();
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
        if (profileIds.isEmpty()) {
            if (callback != null) callback.accept("Nothing to export");
            return;
        }
        AtomicInteger remaining = new AtomicInteger(profileIds.size());
        for (Integer id : profileIds) {
            if (id == null) {
                if (remaining.decrementAndGet() == 0) finishExport(context, eventId, callback);
                continue;
            }
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
            writer.write("UserID,Name,Email,Phone Number,Gender,City,Province,Postal Code\n");
            for (Profile p : loadedProfiles) {
                writer.write(csvField(p.getUserId()) + "," +
                        csvField(p.getUserName()) + "," +
                        csvField(p.getEmail()) + "," +
                        csvField(p.getPhoneNum()) + "," +
                        csvField(p.getGender()) + "," +
                        csvField(p.getCity()) + "," +
                        csvField(p.getProvince()) + "," +
                        csvField(p.getPostalcode()) + "\n");
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
        if (s.contains("\"")) s = s.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\n")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}
