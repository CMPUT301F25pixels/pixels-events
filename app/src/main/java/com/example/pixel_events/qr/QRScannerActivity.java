package com.example.pixel_events.qr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.events.EventDetailsActivity;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.example.pixel_events.R;

/**
 * QRScannerActivity
 *
 * Scans QR codes using device camera to retrieve event IDs.
 * Opens event details screen when valid QR code is scanned.
 * Uses ZXing library for barcode scanning functionality.
 * 
 * Implements US 01.06.01 (view event details via QR scan).
 */
public class QRScannerActivity extends AppCompatActivity {
    private DecoratedBarcodeView barcodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        barcodeView = findViewById(R.id.barcode_scanner);
        barcodeView.decodeContinuous(callback);
    }

    /**
     * Callback for QR code scanning results
     * Extracts event ID from scanned QR code and opens event details
     */
    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                String eventId = result.getText();
                
                Intent intent = new Intent(QRScannerActivity.this, EventDetailsActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
                finish();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}

/*
 * Class:
 *      QRScannerActivity
 *
 * Responsibilities:
 *      Scan QR codes using device camera
 *      Extract event ID from scanned codes
 *      Navigate to event details screen
 *      Manage camera lifecycle
 *
 * Collaborators:
 *      EventDetailsActivity
 *      QRCode (for generation on organizer side)
 */
