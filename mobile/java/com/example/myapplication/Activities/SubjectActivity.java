package com.example.myapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.mappers.DatabaseMapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SubjectActivity extends AppCompatActivity {
    private int teacherId;
    private List<Integer> subjectsId;
    private List<Integer> difficultiesId;
    private String subjectId;
    private String difficultyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        // Initialize UI components
        Spinner subjectSpinner = findViewById(R.id.subjectSpinner);
        Spinner difficultySpinner = findViewById(R.id.difficultySpinner);
        Button applyButton = findViewById(R.id.applyButton);
        Button backButton = findViewById(R.id.backButton);

        // Retrieve data from the Intent
        teacherId = getIntent().getIntExtra("teacherId", -1);
        subjectId = getIntent().getStringExtra("subjectId");
        difficultyId = getIntent().getStringExtra("difficultyId");

        // Convert subjectId and difficultyId to lists of integers
        subjectsId = Arrays.stream(subjectId.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        difficultiesId = Arrays.stream(difficultyId.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        // Retrieve names of subjects and difficulties
        List<String> subjectsName = DatabaseMapper.getSubjectsName(this, subjectsId);
        List<String> difficultiesName = DatabaseMapper.getDifficultiesName(this, difficultiesId);

        // Set up adapters for spinners
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjectsName);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);

        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, difficultiesName);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);

        // Handle apply button click
        applyButton.setOnClickListener(v -> {
            // Get selected subject and difficulty IDs
            int subjId = DatabaseMapper.getSubjectId(this, subjectSpinner.getSelectedItem().toString());
            int diffId = DatabaseMapper.getDifficultyId(this, difficultySpinner.getSelectedItem().toString());

            // Create Intent to start BookLessonActivity with selected data
            Intent intent = new Intent(SubjectActivity.this, BookLessonActivity.class);
            intent.putExtra("teacherId", teacherId);
            intent.putExtra("subjectId", subjId);
            intent.putExtra("difficultyId", diffId);
            startActivity(intent);
        });

        // Handle back button click to return to the previous activity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SubjectActivity.this, TeachersActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
