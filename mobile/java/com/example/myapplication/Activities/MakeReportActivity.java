package com.example.myapplication.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.HttpModels.HttpResponse;
import com.example.myapplication.HttpQueries.ReportHttpCommands;
import com.example.myapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class MakeReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_make_report);

        // Retrieve the authentication token from SharedPreferences
        SharedPreferences sharedPreferences = this.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");

        // Retrieve data from the Intent
        String lessonId = getIntent().getStringExtra("lessonId");
        String teacher = getIntent().getStringExtra("teacher");
        String difficultyLevel = getIntent().getStringExtra("difficultyLevel");
        String subject = getIntent().getStringExtra("subject");
        String date = getIntent().getStringExtra("date");
        String studentId = getIntent().getStringExtra("studentId");

        // Set the labels with the retrieved data
        TextView subjectLabel = findViewById(R.id.subjectLabel);
        TextView teacherLabel = findViewById(R.id.teacherLabel);
        TextView studentIdLabel = findViewById(R.id.studentIdLabel);
        TextView difficultyLabel = findViewById(R.id.difficultyLabel);
        TextView dateLabel = findViewById(R.id.dateLabel);
        EditText commentEditText = findViewById(R.id.comment);
        EditText homeworkEditText = findViewById(R.id.homework);
        RatingBar ratingBar = findViewById(R.id.rating);
        Button backButton = findViewById(R.id.backButton);
        subjectLabel.setText("Subject: " + subject);
        teacherLabel.setText("Teacher: " + teacher);
        studentIdLabel.setText("Student ID: " + studentId);
        difficultyLabel.setText("Difficulty Level: " + difficultyLevel);
        dateLabel.setText("Date: " + date);

        // Handle the submit button click
        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> {
            String comment = commentEditText.getText().toString();
            String homework = homeworkEditText.getText().toString();
            int lessonIdInt = Integer.parseInt(lessonId);
            int progressRating = (int) ratingBar.getRating();
            JSONObject jsonObject = new JSONObject();
            try {
                // Create JSON object for the report
                jsonObject.put("comment", comment);
                jsonObject.put("homework", homework);
                jsonObject.put("lesson_id", lessonIdInt);
                jsonObject.put("progress_rating", progressRating);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Send the report to the server
            HttpResponse httpResponse;
            try {
                httpResponse = ReportHttpCommands.makeReport(this, authToken, jsonObject);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            Toast.makeText(this, httpResponse.getMessage().replace("}", ""), Toast.LENGTH_SHORT).show();
        });

        // Handle the back button click to return to the previous activity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(MakeReportActivity.this, ViewPastLessonsActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
