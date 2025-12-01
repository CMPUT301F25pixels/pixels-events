package com.example.pixel_events.organizerTest;

import com.example.pixel_events.utils.Validator;
import org.junit.Test;
import static org.junit.Assert.*;

public class RegistrationPeriodValidationTest {
    private final String FUTURE_EVENT_START = "2026-03-01";
    private final String FUTURE_EVENT_END = "2026-03-05";
    private final String VALID_REG_START = "2026-01-01";
    private final String EVENT_TIME_START = "10:00";
    private final String EVENT_TIME_END = "12:00";

    /**
     * US 02.01.04 WB: Test case 1: regEndDate is one day *after* eventStartDate.
     * Should throw an error.
     */
    @Test
    public void testRegistrationEndDateAfterEventStart() {
        String regEndBad = "2026-03-02"; // One day AFTER event start

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Validator.validateDateRelations(
                    FUTURE_EVENT_START,
                    FUTURE_EVENT_END,
                    VALID_REG_START,
                    regEndBad,
                    EVENT_TIME_START,
                    EVENT_TIME_END
            );
        });

        assertTrue(exception.getMessage().contains("Registration must end before or on the event start date"));
    }

    /**
     * US 02.01.04 WB: Test case 2: regEndDate is *before* regStartDate.
     * Should throw an error.
     */
    @Test
    public void testRegistrationEndBeforeStart() {
        String regStart = "2026-02-10";
        String regEndBad = "2026-02-09";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Validator.validateDateRelations(
                    FUTURE_EVENT_START,
                    FUTURE_EVENT_END,
                    regStart,
                    regEndBad,
                    EVENT_TIME_START,
                    EVENT_TIME_END
            );
        });

        assertTrue(exception.getMessage().contains("Registration end date must be on or after registration start date"));
    }
}