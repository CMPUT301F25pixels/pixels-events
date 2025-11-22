package com.example.pixel_events.qrcode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pixel_events.R;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.pixel_events.events.EventDetailedFragment;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;

import java.util.Arrays;
import java.util.List;

public class ScannerFragment extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST = 1001;
    private DecoratedBarcodeView barcodeView;
    private boolean navigated = false; // prevent double navigation on multiple scans

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // You can find and initialize your views here, e.g., view.findViewById(...)
        barcodeView = view.findViewById(R.id.barcode_scanner);

        // Ensure we only decode QR codes
        barcodeView.getBarcodeView().setDecoderFactory(
                new DefaultDecoderFactory(Arrays.asList(BarcodeFormat.QR_CODE)));

        // Permission check
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[] { Manifest.permission.CAMERA }, CAMERA_PERMISSION_REQUEST);
        } else {
            startScanner();
        }
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result == null)
                return;
            String text = result.getText();
            if (text == null || navigated)
                return;

            if (text.startsWith("Event-")) {
                String[] parts = text.split("-");
                if (parts.length >= 3) {
                    try {
                        int eventId = Integer.parseInt(parts[1]);
                        navigated = true;
                        barcodeView.pause();
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.nav_host_fragment_activity_dashboard, new EventDetailedFragment(eventId))
                                .addToBackStack(null)
                                .commit();
                    } catch (NumberFormatException e) {
                        Log.w("ScannerFragment", "Invalid event id in QR: " + parts[1]);
                        toast("Scanned QR has invalid event id");
                    }
                } else {
                    toast("Scanned QR has unexpected format");
                }
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
            // no-op
        }
    };

    private void startScanner() {
        if (barcodeView != null) {
            barcodeView.decodeContinuous(callback);
        }
    }

    private void toast(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (barcodeView != null && !navigated)
            barcodeView.resume();
    }

    @Override
    public void onPause() {
        if (barcodeView != null)
            barcodeView.pause();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanner();
            } else {
                toast("Camera permission is required to scan QR codes");
            }
        }
    }
}
