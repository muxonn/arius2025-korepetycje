package com.example.myapplication.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Adapters.ReviewAdapter;
import com.example.myapplication.HttpModels.TeacherReview;
import com.example.myapplication.HttpModels.TeacherReviewsHttpResponse;
import com.example.myapplication.HttpQueries.TeacherHttpCommands;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ReviewActivity extends AppCompatActivity {
    private ListView listView;
    ReviewAdapter reviewAdapter;
    private List<TeacherReview> originalItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Retrieve teacherId from the Intent
        int teacherId = getIntent().getIntExtra("teacherId", -1);
        listView = findViewById(R.id.listView);
        Button backButton = findViewById(R.id.backButton);
        TextView textHolder = findViewById(R.id.textHolder);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        originalItems = new ArrayList<>();

        // Fetch teacher reviews from the API
        try {
            TeacherReviewsHttpResponse httpResponse = TeacherHttpCommands.getTeacherReviewsById(this,
                    teacherId, sharedPreferences.getString("authToken", "")
            );
            if (httpResponse != null && httpResponse.getTeacherReviews() != null) {
                originalItems = httpResponse.getTeacherReviews();
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Update UI based on whether reviews are available
        textHolder.setText("");
        if (originalItems.isEmpty()) {
            textHolder.setVisibility(View.VISIBLE);
            textHolder.setText("This teacher has no reviews yet.");
        } else {
            textHolder.setVisibility(View.GONE);
        }

        // Set the adapter for the ListView
        reviewAdapter = new ReviewAdapter(this, originalItems);
        listView.setAdapter(reviewAdapter);

        // Handle the back button click
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ReviewActivity.this, TeachersActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
