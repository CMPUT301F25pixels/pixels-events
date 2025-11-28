package com.example.pixel_events.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class ImageConversion {
    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); // or JPEG
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    /**
     * Converts a bitmap to a Base64 string with resizing and JPEG compression.
     * Suitable for photos and posters to keep the size small (e.g. under 1MB for Firestore).
     */
    public static String bitmapToCompressedBase64(Bitmap bitmap) {
        if (bitmap == null) return "";
        
        // Resize if too big (max 800px)
        int maxDimension = 800;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width > maxDimension || height > maxDimension) {
            float ratio = (float) width / height;
            if (width > height) {
                width = maxDimension;
                height = (int) (width / ratio);
            } else {
                height = maxDimension;
                width = (int) (height * ratio);
            }
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Use JPEG for better compression of photos, quality 70
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); 
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public static Bitmap base64ToBitmap(String base64Str) throws IllegalArgumentException {
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
