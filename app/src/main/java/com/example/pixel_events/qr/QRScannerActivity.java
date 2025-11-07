package com.example.pixel_events.qr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.MainActivity;
import com.example.pixel_events.events.EventDetailsActivity;
import com.example.pixel_events.events.EventsListActivity;
import com.example.pixel_events.settings.MainSettingsActivity;
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
        
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        
        setupBottomNav();
    }
    
    private void setupBottomNav() {
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navEvents = findViewById(R.id.nav_events);
        LinearLayout navScanner = findViewById(R.id.nav_scanner);
        LinearLayout navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        navEvents.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventsListActivity.class);
            startActivity(intent);
        });

        navScanner.setOnClickListener(v -> {
            Toast.makeText(this, "Already on Scanner", Toast.LENGTH_SHORT).show();
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainSettingsActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Callback for QR code scanning results
     * Extracts event ID from scanned QR code and opens event details
     */
    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                String scannedText = result.getText();
                String eventId;
                
                // Check if QR code has EVENT: prefix
                if (scannedText.startsWith("EVENT:")) {
                    eventId = scannedText.substring(6); // Remove "EVENT:" prefix
                } else {
                    // Assume the text is the event ID directly
                    eventId = scannedText;
                }
                
                Toast.makeText(QRScannerActivity.this, "Opening event...", Toast.LENGTH_SHORT).show();
                
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
