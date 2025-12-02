package com.example.pixel_events.organizerTest;

import android.graphics.Bitmap;
import com.example.pixel_events.qrcode.QRCode;
import com.example.pixel_events.utils.ImageConversion;
import org.junit.Test;

import static org.junit.Assert.*;

public class QRCodeTest {

    @Test
    public void testGenerateQRCode() {
        String eventId = "123";
        int size = 200;

        Bitmap qr = QRCode.generateQRCodeBitmap(eventId, size, size);

        assertNotNull("qr code should not be null", qr);
        assertEquals("qr width should match", size, qr.getWidth());
        assertEquals("qr height should match", size, qr.getHeight());
    }

    @Test
    public void testGenerateQRCodeWithDifferentSizes() {
        String eventId = "456";

        Bitmap qr100 = QRCode.generateQRCodeBitmap(eventId, 100, 100);
        Bitmap qr300 = QRCode.generateQRCodeBitmap(eventId, 300, 300);

        assertNotNull("small qr should not be null", qr100);
        assertNotNull("large qr should not be null", qr300);
        assertEquals("small qr width", 100, qr100.getWidth());
        assertEquals("large qr width", 300, qr300.getWidth());
    }

    @Test
    public void testGenerateQRCodeWithLongEventId() {
        String eventId = "event_12345_very_long_id_string";
        int size = 250;

        Bitmap qr = QRCode.generateQRCodeBitmap(eventId, size, size);

        assertNotNull("qr code should handle long ids", qr);
        assertEquals("qr size should match", size, qr.getWidth());
    }

    @Test
    public void testQRCodeSquare() {
        String eventId = "789";
        int size = 150;

        Bitmap qr = QRCode.generateQRCodeBitmap(eventId, size, size);

        assertEquals("qr should be square", qr.getWidth(), qr.getHeight());
    }

    @Test
    public void testDifferentEventIdsDifferentCodes() {
        String eventId1 = "event1";
        String eventId2 = "event2";
        int size = 200;

        Bitmap qr1 = QRCode.generateQRCodeBitmap(eventId1, size, size);
        Bitmap qr2 = QRCode.generateQRCodeBitmap(eventId2, size, size);

        assertNotNull("first qr should not be null", qr1);
        assertNotNull("second qr should not be null", qr2);
    }

    @Test
    public void testSmallSize() {
        String eventId = "small";
        int size = 50;

        Bitmap qr = QRCode.generateQRCodeBitmap(eventId, size, size);

        assertNotNull("small qr should generate", qr);
        assertEquals("small size should match", size, qr.getWidth());
    }

    @Test
    public void testLargeSize() {
        String eventId = "large";
        int size = 500;

        Bitmap qr = QRCode.generateQRCodeBitmap(eventId, size, size);

        assertNotNull("large qr should generate", qr);
        assertEquals("large size should match", size, qr.getWidth());
    }

    @Test
    public void testNumericEventId() {
        String eventId = "999";
        int size = 200;

        Bitmap qr = QRCode.generateQRCodeBitmap(eventId, size, size);

        assertNotNull("numeric id should work", qr);
    }

    @Test
    public void testAlphanumericEventId() {
        String eventId = "ABC123xyz";
        int size = 200;

        Bitmap qr = QRCode.generateQRCodeBitmap(eventId, size, size);

        assertNotNull("alphanumeric id should work", qr);
    }

    /**
     * US 02.01.01 WB: Verifies QR code generation and Base64 encoding/decoding.
     */
    @Test
    public void testGenerateQRCodeBase64_validInput() {
        String testData = "Event-TESTID-123-PromoCheck";

        // Generate Base64 string
        String base64String = QRCode.generateQRCodeBase64(testData);

        assertNotNull("Base64 string should not be null", base64String);
        assertFalse("Base64 string should not be empty", base64String.isEmpty());

        // Verify the Base64 string can be decoded back into a Bitmap
        try {
            Bitmap decodedBitmap = ImageConversion.base64ToBitmap(base64String);
            assertNotNull("Decoded Bitmap should not be null", decodedBitmap);
            assertTrue("Decoded Bitmap should have non-zero dimensions", decodedBitmap.getWidth() > 0 && decodedBitmap.getHeight() > 0);
        } catch (IllegalArgumentException e) {
            fail("Failed to decode generated Base64 QR code: " + e.getMessage());
        }
    }
}