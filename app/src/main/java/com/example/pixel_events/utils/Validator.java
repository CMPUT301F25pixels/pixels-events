package com.example.pixel_events.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Validator {
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }

    public static void validateDateRelations(
            String eventStartDate,
            String eventEndDate,
            String registrationStartDate,
            String registrationEndDate,
            String eventStartTime,
            String eventEndTime
    ) {
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateFmt.setLenient(false);
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.US);
        timeFmt.setLenient(false);

        try {
            Date startDateObj = null;
            Date endDateObj = null;
            Date regStartObj = null;
            Date regEndObj = null;

            if (eventStartDate != null && !eventStartDate.trim().isEmpty()) {
                startDateObj = dateFmt.parse(eventStartDate);
            }
            if (eventEndDate != null && !eventEndDate.trim().isEmpty()) {
                endDateObj = dateFmt.parse(eventEndDate);
            }
            if (registrationStartDate != null && !registrationStartDate.trim().isEmpty()) {
                regStartObj = dateFmt.parse(registrationStartDate);
            }
            if (registrationEndDate != null && !registrationEndDate.trim().isEmpty()) {
                regEndObj = dateFmt.parse(registrationEndDate);
            }

            // Today's date at midnight
            Date today = dateFmt.parse(dateFmt.format(new Date()));

            if (startDateObj != null && startDateObj.before(today)) {
                throw new IllegalArgumentException("Event start date must be today or after today");
            }
            if (endDateObj != null && endDateObj.before(today)) {
                throw new IllegalArgumentException("Event end date must be today or after today");
            }
            if (regStartObj != null && regStartObj.before(today)) {
                throw new IllegalArgumentException("Registration start date must be today or after today");
            }
            if (regEndObj != null && regEndObj.before(today)) {
                throw new IllegalArgumentException("Registration end date must be today or after today");
            }

            // End must be on or after start when both present
            if (startDateObj != null && endDateObj != null && endDateObj.before(startDateObj)) {
                throw new IllegalArgumentException("Event end date must be on or after event start date");
            }

            // Registration ordering
            if (regStartObj != null && regEndObj != null && regEndObj.before(regStartObj)) {
                throw new IllegalArgumentException("Registration end date must be on or after registration start date");
            }

            // Registration must finish before or on the event start date (if both present)
            if  (regEndObj != null && startDateObj != null && regEndObj.after(startDateObj))  {
                throw new IllegalArgumentException("Registration must end before or on the event start date");
            }

            // Time validation when both times provided
            if (eventStartTime != null && !eventStartTime.trim().isEmpty()
                    && eventEndTime != null && !eventEndTime.trim().isEmpty()) {
                Date startTimeObj = timeFmt.parse(eventStartTime);
                Date endTimeObj = timeFmt.parse(eventEndTime);

                // If event occurs on the same day, ensure end time is after start time
                if (startDateObj != null && endDateObj != null
                        && dateFmt.format(startDateObj).equals(dateFmt.format(endDateObj))) {
                    if (!endTimeObj.after(startTimeObj)) {
                        throw new IllegalArgumentException("Event end time must be after start time when on the same date");
                    }
                }
            }

        } catch (ParseException pe) {
            throw new IllegalArgumentException("Invalid date/time format: " + pe.getMessage());
        }
    }
}
