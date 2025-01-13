package com.example.myapplication.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class PortalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_portal);

        // Initialize UI components
        Button logoutButton = findViewById(R.id.logoutButton);
        Button teacherButton = findViewById(R.id.teacherButton);
        Button viewPastLessonsButton = findViewById(R.id.viewPastLessonsButton);

        // Handle the teacher button click
        teacherButton.setOnClickListener(v -> {
            Intent intent = new Intent(PortalActivity.this, TeachersActivity.class);
            startActivity(intent);
            finish();
        });

        // Handle the logout button click
        logoutButton.setOnClickListener(v -> {
            // Clear the auth token and role from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("authToken", "");
            editor.putString("role", "");
            editor.apply();

            // Redirect to the main activity
            Intent intent = new Intent(PortalActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Retrieve the user role from SharedPreferences and set button text accordingly
        SharedPreferences sharedPreferences = this.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userRole = sharedPreferences.getString("role", "");
        if ("teacher".equals(userRole)) {
            teacherButton.setText("View Teachers");
        } else {
            teacherButton.setText("View Teachers and Book Lessons");
        }

        // Handle the view past lessons button click
        viewPastLessonsButton.setOnClickListener(v -> {
            Intent intent = new Intent(PortalActivity.this, ViewPastLessonsActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
