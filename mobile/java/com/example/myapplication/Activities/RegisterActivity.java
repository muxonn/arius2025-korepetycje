package com.example.myapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.HttpModels.HttpResponse;
import com.example.myapplication.HttpQueries.AuthHttpCommands;
import com.example.myapplication.R;
import com.example.myapplication.mappers.DatabaseMapper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Example data for spinners
        String[] subjects = DatabaseMapper
                .getSubjectsName(this, new ArrayList<>())
                .toArray(new String[0]);
        String[] difficultyLevels = DatabaseMapper
                .getDifficultiesName(this, new ArrayList<>())
                .toArray(new String[0]);

        Button subjectsButton = findViewById(R.id.subjectsButton);
        Button difficultyButton = findViewById(R.id.difficultyButton);
        RadioGroup roleRadioGroup = findViewById(R.id.roleRadioGroup);
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText hourlyRateEditText = findViewById(R.id.hourlyRateEditText);
        Button registerButton = findViewById(R.id.registerButton);

        boolean[] selectedSubjects = new boolean[subjects.length];
        boolean[] selectedDifficulties = new boolean[difficultyLevels.length];

        // Multi-choice dialog for Subjects
        subjectsButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Subjects");
            builder.setMultiChoiceItems(subjects, selectedSubjects, (dialog, which, isChecked) -> {
                selectedSubjects[which] = isChecked;
            });
            builder.setPositiveButton("OK", (dialog, which) -> {
                Toast.makeText(this, "Subjects selected", Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });

        // Multi-choice dialog for Difficulty Levels
        difficultyButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Difficulty Levels");
            builder.setMultiChoiceItems(difficultyLevels, selectedDifficulties, (dialog, which, isChecked) -> {
                selectedDifficulties[which] = isChecked;
            });
            builder.setPositiveButton("OK", (dialog, which) -> {
                Toast.makeText(this, "Difficulties selected", Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });

        // Handle role radio group change
        roleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.teacherRadioButton) {
                subjectsButton.setVisibility(View.VISIBLE);
                difficultyButton.setVisibility(View.VISIBLE);
                hourlyRateEditText.setVisibility(View.VISIBLE);
            } else {
                subjectsButton.setVisibility(View.GONE);
                difficultyButton.setVisibility(View.GONE);
                hourlyRateEditText.setVisibility(View.GONE);
            }
        });

        // Handle register button click
        registerButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String hourlyRate = hourlyRateEditText.getText().toString().trim();
            int selectedRoleId = roleRadioGroup.getCheckedRadioButtonId();
            String role = "";
            StringBuilder selectedSubjectsList = new StringBuilder();
            StringBuilder selectedDifficultiesList = new StringBuilder();

            if (selectedRoleId == R.id.studentRadioButton) {
                role = "student";
            } else if (selectedRoleId == R.id.teacherRadioButton) {
                role = "teacher";
                for (int i = 0; i < subjects.length; i++) {
                    if (selectedSubjects[i]) {
                        if (selectedSubjectsList.length() > 0) selectedSubjectsList.append(", ");
                        selectedSubjectsList.append(subjects[i]);
                    }
                }

                // Get selected difficulty levels
                for (int i = 0; i < difficultyLevels.length; i++) {
                    if (selectedDifficulties[i]) {
                        if (selectedDifficultiesList.length() > 0)
                            selectedDifficultiesList.append(", ");
                        selectedDifficultiesList.append(difficultyLevels[i]);
                    }
                }
            }

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("email", email);
                jsonObject.put("name", name);
                jsonObject.put("password", password);
                jsonObject.put("role", role);
                if (role.equals("teacher")) {
                    List<String> selectedSubj = Arrays.stream(selectedSubjectsList.toString().split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());
                    List<String> selectedDiff = Arrays.stream(selectedDifficultiesList.toString().split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());
                    List<Integer> chosenSubjects = DatabaseMapper.getSubjectsId(this, selectedSubj);
                    List<Integer> chosenDiff = DatabaseMapper.getDifficultiesId(this, selectedDiff);
                    jsonObject.put("subject_ids", "{" + chosenSubjects.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(",")) + "}"
                    );
                    jsonObject.put("difficulty_ids", "{" + chosenDiff.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(",")) + "}"
                    );
                    jsonObject.put("hourly_rate", hourlyRate);
                }
            } catch (Exception e) {
                Toast.makeText(this, "JSON creating error!", Toast.LENGTH_SHORT).show();
                return;
            }

            runOnUiThread(() -> {
                try {
                    HttpResponse response = AuthHttpCommands.register(this, jsonObject);
                    if (response.getStatusCode() == 201) {
                        Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Error: " + response.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Incorrect server response!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
