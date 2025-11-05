package com.example.pixel_events.qr;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

/**
 * QRCode class for generating QR codes from any text data
 * This class can be used to convert any string data into a QR code bitmap
 */
public class QRCode {
    private static final String TAG = "QRCode";
    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 400;
    
    private String data;
    private Bitmap qrCodeBitmap;
    
    /**
     * Constructor to create a QR code from text data
     * @param data The text data to encode in the QR code
     */
    public QRCode(String data) {
        this.data = data;
        this.qrCodeBitmap = generateQRCode(data, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    /**
     * Constructor to create a QR code with custom dimensions
     * @param data The text data to encode in the QR code
     * @param width The width of the QR code in pixels
     * @param height The height of the QR code in pixels
     */
    public QRCode(String data, int width, int height) {
        this.data = data;
        this.qrCodeBitmap = generateQRCode(data, width, height);
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
     * @return The QR code bitmap, or null if generation failed
     */
    public Bitmap getBitmap() {
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
 *      Generate QR codes from any text data
 *      Convert string data to QR code bitmaps
 *      Provide utilities for QR code generation with custom dimensions
 *
 * Usage:
 *      QRCode qrCode = new QRCode("Event-12345");
 *      Bitmap bitmap = qrCode.getBitmap();
 *      
 *      // Or use static method:
 *      Bitmap bitmap = QRCode.generateQRCodeBitmap("Event-12345");
 */
