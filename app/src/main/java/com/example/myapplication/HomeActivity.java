package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize CardView buttons
        CardView calendarCard = findViewById(R.id.card_calendar);
        CardView noticeCard = findViewById(R.id.card_notice);

        // Get the current user's name from the intent
        Intent intent = getIntent();
        userName = intent.getStringExtra("NAME");

        calendarCard.setOnClickListener(view -> {
            // Navigate to the Shared Calendar page
            Intent calendarIntent = new Intent(this, CalendarActivity.class);
            calendarIntent.putExtra("NAME", userName);
            startActivity(calendarIntent);
        });

        noticeCard.setOnClickListener(view -> {
            // Navigate to the Notice Board page
            Intent noticeIntent = new Intent(this, NoticeActivity.class);
            noticeIntent.putExtra("NAME", userName);
            startActivity(noticeIntent);
        });

        // Logout button
        Button logoutButton = findViewById(R.id.btn_logout);
        logoutButton.setOnClickListener(view -> {
            // Navigate to the Login page
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
}
