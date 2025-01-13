package com.example.myapplication.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.HttpModels.ReportHttpResponse;
import com.example.myapplication.HttpQueries.ReportHttpCommands;
import com.example.myapplication.R;

import java.util.concurrent.ExecutionException;

public class SeeReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_see_report);

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

        // Fetch the report from the API
        ReportHttpResponse httpResponse;
        try {
            httpResponse = ReportHttpCommands.seeReport(this, authToken, Integer.parseInt(lessonId));
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Retrieve details from the report
        String comment = httpResponse.getComment();
        String homework = httpResponse.getHomework();
        int rating = httpResponse.getProgressRating();

        // Initialize and set data for UI components
        TextView subjectLabel = findViewById(R.id.subjectLabel);
        TextView teacherLabel = findViewById(R.id.teacherLabel);
        TextView studentIdLabel = findViewById(R.id.studentIdLabel);
        TextView difficultyLabel = findViewById(R.id.difficultyLabel);
        TextView dateLabel = findViewById(R.id.dateLabel);
        TextView commentLabel = findViewById(R.id.comment);
        TextView homeworkLabel = findViewById(R.id.homework);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        Button backButton = findViewById(R.id.backButton);

        subjectLabel.setText("Subject: " + subject);
        teacherLabel.setText("Teacher: " + teacher);
        studentIdLabel.setText("Student ID: " + studentId);
        difficultyLabel.setText("Difficulty Level: " + difficultyLevel);
        dateLabel.setText("Lesson date: " + date);
        commentLabel.setText("Comment: " + comment);
        homeworkLabel.setText("Homework: " + homework);
        ratingBar.setRating(rating);

        // Handle back button click to return to the previous activity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SeeReportActivity.this, ViewPastLessonsActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
