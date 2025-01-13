package com.example.myapplication.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Adapters.TeacherAdapter;
import com.example.myapplication.HttpModels.Teacher;
import com.example.myapplication.HttpModels.TeacherResponse;
import com.example.myapplication.HttpQueries.TeacherHttpCommands;
import com.example.myapplication.R;
import com.example.myapplication.mappers.DatabaseMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TeachersActivity extends AppCompatActivity {

    private ListView listView;
    TeacherAdapter teacherAdapter;
    private List<Teacher> originalItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teachers);

        // Initialize UI components
        listView = findViewById(R.id.listView);
        Button backButton = findViewById(R.id.backButton);
        Spinner subjectSpinner = findViewById(R.id.subjectSpinner);
        Spinner difficultySpinner = findViewById(R.id.difficultySpinner);
        TextView applyFiltersButton = findViewById(R.id.applyFiltersButton);

        // Retrieve the authentication token from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        originalItems = new ArrayList<>();

        // Fetch teacher list from the API
        try {
            TeacherResponse httpResponse = TeacherHttpCommands.getTeacherList(this, sharedPreferences.getString("authToken", ""), "", "");
            if (httpResponse != null && httpResponse.getTeacherList() != null) {
                originalItems = httpResponse.getTeacherList();
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Set the adapter for the ListView
        teacherAdapter = new TeacherAdapter(this, originalItems);
        listView.setAdapter(teacherAdapter);

        // Prepare lists for spinner choices
        List<String> chooseDifficulty = new ArrayList<>();
        chooseDifficulty.add("All");
        chooseDifficulty.addAll(DatabaseMapper.getDifficultiesName(this, new ArrayList<>()));

        List<String> chooseSubject = new ArrayList<>();
        chooseSubject.add("All");
        chooseSubject.addAll(DatabaseMapper.getSubjectsName(this, new ArrayList<>()));

        // Set up adapters for spinners
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, chooseSubject);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);

        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, chooseDifficulty);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);

        // Handle "Apply Filters" button click
        applyFiltersButton.setOnClickListener(v -> {
            Integer subjectId = DatabaseMapper.getSubjectId(this, subjectSpinner.getSelectedItem().toString());
            Integer difficultyId = DatabaseMapper.getDifficultyId(this, difficultySpinner.getSelectedItem().toString());
            try {
                String subject = (subjectId == 0) ? "All" : subjectId.toString();
                String difficulty = (difficultyId == 0) ? "All" : difficultyId.toString();
                TeacherResponse httpResponse = TeacherHttpCommands.getTeacherList(this, sharedPreferences.getString("authToken", ""), subject, difficulty);
                teacherAdapter.clear();
                if (httpResponse != null && httpResponse.getTeacherList() != null) {
                    originalItems = httpResponse.getTeacherList();
                    teacherAdapter.addAll(originalItems);
                    teacherAdapter.notifyDataSetChanged();
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // Handle "Back" button click to return to the previous activity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(TeachersActivity.this, PortalActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
