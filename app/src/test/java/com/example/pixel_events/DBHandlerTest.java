package com.example.pixel_events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBHandlerTest {



    @Test
    public void testGetAccInfo() {
        DataBaseHandler db = new DataBaseHandler();
        db.getAccInfo(1, accountData -> {
            assertEquals(1, accountData.id);
            assertEquals("org", accountData.accType);
            assertEquals("Naur", accountData.userName);
            assertEquals("other", accountData.gender);
            assertEquals("test123@456.com", accountData.email);
            assertEquals("Edmonton", accountData.city);
            assertEquals("AB", accountData.province);
            assertEquals(2223144, accountData.phoneNum);
            assertEquals(true, accountData.notify);
        });
    }

    @Test
    public void testAddEvent() {
        DataBaseHandler db = new DataBaseHandler();
        db.addEvent(1, 23, 2);

        List<Integer> newEvent = new ArrayList<>();
        newEvent.add(23);
        newEvent.add(2);

        db.getAccInfo(1, accountData -> {
            assertEquals(newEvent, accountData.events);
        });
    }

}
