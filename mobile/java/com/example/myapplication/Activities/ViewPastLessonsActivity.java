package com.example.myapplication.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Adapters.PastLessonsAdapter;
import com.example.myapplication.HttpModels.Lesson;
import com.example.myapplication.HttpQueries.LessonsHttpCommands;
import com.example.myapplication.R;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ViewPastLessonsActivity extends AppCompatActivity {
    PastLessonsAdapter pastLessonsAdapter;
    List<Lesson> originalItems;
    String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_past_lessons);

        // Initialize UI components
        Button backButton = findViewById(R.id.backButton);
        ListView listView = findViewById(R.id.listView);
        TextView textView = findViewById(R.id.text);

        // Retrieve the authentication token from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");

        // Fetch completed lessons from the API
        try {
            originalItems = LessonsHttpCommands.getCompletedLessons(this, authToken).getLessons();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Update UI based on whether lessons are available
        if (originalItems == null || originalItems.isEmpty()) {
            textView.setText("You have not had any lessons yet.");
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
            pastLessonsAdapter = new PastLessonsAdapter(this, originalItems);
            listView.setAdapter(pastLessonsAdapter);
        }

        // Handle back button click to return to the previous activity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewPastLessonsActivity.this, PortalActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
