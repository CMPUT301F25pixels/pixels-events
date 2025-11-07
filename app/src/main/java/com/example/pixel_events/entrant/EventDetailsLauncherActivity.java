package com.example.pixel_events.entrant;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixel_events.R;

public class EventDetailsLauncherActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_hosts);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EventDetailsFragment())
                    .commit();
        }
    }
}

