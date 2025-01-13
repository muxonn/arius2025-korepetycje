package com.example.myapplication.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.HttpModels.HttpResponse;
import com.example.myapplication.HttpQueries.LessonsHttpCommands;
import com.example.myapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class GiveReviewActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the authentication token from SharedPreferences
        SharedPreferences sharedPreferences = this.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        setContentView(R.layout.activity_give_review);

        // Retrieve data from the Intent
        String teacher = getIntent().getStringExtra("teacher");
        String difficultyLevel = getIntent().getStringExtra("difficultyLevel");
        String subject = getIntent().getStringExtra("subject");
        String date = getIntent().getStringExtra("date");
        String teacherId = getIntent().getStringExtra("teacherId");

        // Set the labels with the retrieved data
        TextView teacherLabel = findViewById(R.id.teacherLabel);
        TextView subjectLabel = findViewById(R.id.subjectLabel);
        TextView difficultyLabel = findViewById(R.id.difficultyLabel);
        TextView dateLabel = findViewById(R.id.dateLabel);
        teacherLabel.setText("Teacher: " + teacher);
        subjectLabel.setText("Subject: " + subject);
        difficultyLabel.setText("Difficulty Level: " + difficultyLevel);
        dateLabel.setText("Date: " + date);

        // Initialize the comment EditText and RatingBar
        EditText commentEditText = findViewById(R.id.comment);
        RatingBar ratingBar = findViewById(R.id.rating);

        // Handle the submit button click
        findViewById(R.id.submitButton).setOnClickListener(v -> {
            String comment = commentEditText.getText().toString();
            float rating = ratingBar.getRating();
            JSONObject jsonObject = new JSONObject();
            try {
                // Create JSON object for the review
                jsonObject.put("comment", comment);
                jsonObject.put("rating", rating);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Send the review to the server
            HttpResponse httpResponse;
            try {
                httpResponse = LessonsHttpCommands.giveReview(this, authToken, jsonObject, Integer.parseInt(teacherId));
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            Toast.makeText(this, httpResponse.getMessage().replace("}", ""), Toast.LENGTH_SHORT).show();
        });

        // Handle the back button click to return to the previous activity
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(GiveReviewActivity.this, ViewPastLessonsActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
