package com.example.pixel_events.organizerTest;

import android.graphics.Bitmap;

import com.example.pixel_events.qrcode.QRCode;
import com.example.pixel_events.utils.ImageConversion;

import org.junit.Test;
import static org.junit.Assert.*;

public class QRCodeGenerationTest {

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