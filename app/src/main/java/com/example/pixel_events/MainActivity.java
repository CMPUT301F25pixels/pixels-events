package com.example.pixel_events;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.A;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private DataBaseHandler db;
    private CollectionReference accountsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DataBaseHandler();

        db.addAcc(1, "admin","Boba Bob", new Date(), "Male",
                "bobabob@gmail.com", "Edmonton", "AB", 1233224531,
                true);
        db.addEvent(1, 1, 0);
        db.addEvent(1, 2, 1);
        db.addEvent(1, 3, 2);


        db.getAcc(1, account -> {
            if (account != null) {
                Log.d("TEST", "Got user: " + account.userName);
            } else {
                Log.e("TEST", "User not found");
            }
        });

    }
}