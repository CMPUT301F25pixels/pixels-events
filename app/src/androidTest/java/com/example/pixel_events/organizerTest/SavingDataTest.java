package com.example.pixel_events.organizerTest;

import com.example.pixel_events.utils.SavingData;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList; // ADDED: Import ArrayList
import static org.junit.Assert.*;

public class SavingDataTest {
    private SavingData savingData;
    private Method csvFieldMethod;

    @Before
    public void setUp() throws Exception {

        savingData = new SavingData(new ArrayList<>());
        csvFieldMethod = SavingData.class.getDeclaredMethod("csvField", Object.class);
        csvFieldMethod.setAccessible(true);
    }

    private String callCsvField(Object input) throws Exception {
        return (String) csvFieldMethod.invoke(savingData, input);
    }

    /**
     * US 02.06.05 WB: Test case 1: Input with comma -> expected to be wrapped in double quotes.
     */
    @Test
    public void testCsvField_withComma() throws Exception {
        String input = "Name, with comma";
        String expected = "\"Name, with comma\"";
        String actual = callCsvField(input);
        assertEquals("Should wrap value containing a comma in double quotes", expected, actual);
    }

    /**
     * US 02.06.05 WB: Test case 2: Input is the string "null" -> expected output "null".
     */
    @Test
    public void testCsvField_withStringNull() throws Exception {
        String input = "null";
        String expected = "null";
        String actual = callCsvField(input);
        assertEquals("The string 'null' should not be modified or wrapped", expected, actual);
    }

    /**
     * Additional WB: Test input containing double quotes, which should be escaped by doubling.
     */
    @Test
    public void testCsvField_withDoubleQuotes() throws Exception {
        String input = "Value with \"quotes\"";
        String expected = "\"Value with \"\"quotes\"\"\"";
        String actual = callCsvField(input);
        assertEquals("Should escape inner quotes by doubling and wrap in double quotes", expected, actual);
    }

    /**
     * Additional WB: Test for null object input.
     */
    @Test
    public void testCsvField_withNullObject() throws Exception {
        String expected = "null";
        String actual = callCsvField(null);
        assertEquals("Null input should return the string 'null'", expected, actual);
    }

    /**
     * Additional WB: Test for empty string input.
     */
    @Test
    public void testCsvField_withEmptyString() throws Exception {
        String expected = "null";
        String actual = callCsvField("");
        assertEquals("Empty string input should return the string 'null'", expected, actual);
    }
}