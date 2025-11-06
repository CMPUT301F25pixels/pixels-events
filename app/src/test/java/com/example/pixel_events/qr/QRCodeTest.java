package com.example.pixel_events.qr;

import android.graphics.Bitmap;

import org.junit.Test;

import static org.junit.Assert.*;

public class QRCodeTest {

    @Test
    public void testGenerateQRCode() {
        String eventId = "123";
        int size = 200;
        
        Bitmap qr = QRCode.generateQRCode(eventId, size);
        
        assertNotNull("qr code should not be null", qr);
        assertEquals("qr width should match", size, qr.getWidth());
        assertEquals("qr height should match", size, qr.getHeight());
    }

    @Test
    public void testGenerateQRCodeWithDifferentSizes() {
        String eventId = "456";
        
        Bitmap qr100 = QRCode.generateQRCode(eventId, 100);
        Bitmap qr300 = QRCode.generateQRCode(eventId, 300);
        
        assertNotNull("small qr should not be null", qr100);
        assertNotNull("large qr should not be null", qr300);
        assertEquals("small qr width", 100, qr100.getWidth());
        assertEquals("large qr width", 300, qr300.getWidth());
    }
}

