package com.example.pixel_events.qr;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.pixel_events.events.EventActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

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
    private static final String TAG = "QRCode";
    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 400;
    private String data;
    private Bitmap qrCodeBitmap;
    private final int width;
    private final int height;

    /**
     * Constructor to create a QR code from text data
     * @param data The text data to encode in the QR code
     */
    public QRCode(String data) {
        this(data, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Constructor to create a QR code with custom dimensions
     * @param data The text data to encode in the QR code
     * @param width The width of the QR code in pixels
     * @param height The height of the QR code in pixels
     */
    public QRCode(String data, int width, int height) {
        this.data = data;
        this.width = width;
        this.height = height;
    }

    /**
     * Generate a QR code bitmap from text data
     * @param text The text to encode
     * @param width The width of the QR code
     * @param height The height of the QR code
     * @return Bitmap of the QR code, or null if generation fails
     */
    private Bitmap generateQRCode(String text, int width, int height) {
        if (text == null || text.isEmpty()) {
            Log.e(TAG, "Cannot generate QR code: text is null or empty");
            return null;
        }

        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            Bitmap bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, width, height);
            Log.d(TAG, "QR code generated successfully for data: " + text);
            return bitmap;
        } catch (WriterException e) {
            Log.e(TAG, "Error generating QR code", e);
            return null;
        }
    }

    /**
     * Get the QR code as a Bitmap
     * @return The QR code bitmap, or null if generation fails
     */
    public Bitmap getBitmap() {
        if (qrCodeBitmap == null) {
            qrCodeBitmap = generateQRCode(data, width, height);
        }
        return qrCodeBitmap;
    }

    /**
     * Get the data encoded in this QR code
     * @return The encoded data string
     */
    public String getData() {
        return data;
    }

    /**
     * Check if the QR code was generated successfully
     * @return true if bitmap is not null, false otherwise
     */
    public boolean isValid() {
        return qrCodeBitmap != null;
    }

    /**
     * Static helper method to generate a QR code bitmap directly
     * @param text The text to encode
     * @return Bitmap of the QR code, or null if generation fails
     */
    public static Bitmap generateQRCodeBitmap(String text) {
        return new QRCode(text).getBitmap();
    }

    /**
     * Generate QR code as Base64 string
     * @param text The text to encode
     * @return Base64 string of QR code bitmap
     */
    public static String generateQRCodeBase64(String text) {
        QRCode qrcode = new QRCode(text);
        return EventActivity.bitmapToBase64(qrcode.getBitmap());
    }

    /**
     * Static helper method to generate a QR code bitmap with custom dimensions
     * @param text The text to encode
     * @param width The width of the QR code
     * @param height The height of the QR code
     * @return Bitmap of the QR code, or null if generation fails
     */
    public static Bitmap generateQRCodeBitmap(String text, int width, int height) {
        return new QRCode(text, width, height).getBitmap();
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
 *      Provide Base64 encoding for storage
 *
 * Collaborators:
 *      QRScannerActivity
 *      Event
 *      EventActivity
 */

