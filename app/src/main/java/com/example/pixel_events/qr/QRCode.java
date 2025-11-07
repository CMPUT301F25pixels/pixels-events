package com.example.pixel_events.qr;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * QRCode
 *
 * Utility class for generating QR code images from event IDs.
 * Used by organizers to create promotional QR codes for events.
 * Uses Google ZXing library for QR code generation.
 * 
 * Implements US 02.01.01 (generate unique promotional QR code).
 */
public class QRCode {
    
    /**
     * Generate QR code bitmap from event ID
     * @param eventId The unique identifier of the event
     * @param size Width and height of the QR code image in pixels
     * @return Bitmap image of QR code, or null if generation fails
     * 
     * Usage example:
     *     Bitmap qrCode = QRCode.generateQRCode("event123", 300);
     *     if (qrCode != null) {
     *         imageView.setImageBitmap(qrCode);
     *     }
     */
    public static Bitmap generateQRCode(String eventId, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(eventId, BarcodeFormat.QR_CODE, size, size);
            
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            return null;
        }
    }
}

/*
 * Class:
 *      QRCode
 *
 * Responsibilities:
 *      Generate QR code images from event IDs
 *      Create promotional QR codes for organizers (US 02.01.01)
 *      Convert event ID strings to scannable bitmaps
 *
 * Collaborators:
 *      QRScannerActivity (for scanning generated codes)
 *      Event (for event ID source)
 */

