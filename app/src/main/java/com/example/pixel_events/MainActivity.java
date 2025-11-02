package com.example.pixel_events;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private DataBaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        db = new DataBaseHandler();

        // TESTING DatabaseHandler behavior:

// 1) Add a new account
        db.addAcc(
                1,                 // userID
                "org",             // accType
                "Naur" ,              // userName
                new Date(),         // DOB
                "other",             // gender
                "test@test.com",    // email
                "Edmonton",          // city
                "AB",               // province
                2223144,            // phoneNum
                true                // notifications on
        );

// 2) Fetch and display that account
        db.getAccInfo(10, accountData -> {
            if (accountData != null) {
                System.out.println("Account Fetched:");
                System.out.println("  ID: " + accountData.id);
                System.out.println("  Name: " + accountData.userName);
                System.out.println("  Email: " + accountData.email);

                // 3) Add an event once the user exists
                db.addEvent(10, 200, 0);    // eventID = 200, status upcoming (0)

                // 4) Read again to confirm event was added
                db.getAccInfo(10, updated -> {
                    if (updated != null) {
                        System.out.println("✅ Updated Events:");
                        System.out.println(updated.events);
                    }
                });
            } else {
                System.out.println("❌ Account not found!");
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}