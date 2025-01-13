package com.example.myapplication.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.Activities.ReviewActivity;
import com.example.myapplication.Activities.SubjectActivity;
import com.example.myapplication.HttpModels.Teacher;
import com.example.myapplication.R;
import com.example.myapplication.mappers.DatabaseMapper;

import java.util.List;
import java.util.stream.Collectors;

public class TeacherAdapter extends ArrayAdapter<Teacher> {

    public TeacherAdapter(@NonNull Context context, @NonNull List<Teacher> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Inflate layout if needed
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_teacher, parent, false);
        }

        // Get the Teacher object at the current position
        Teacher teacher = getItem(position);

        // Find views in the layout
        TextView name = convertView.findViewById(R.id.teacherName);
        TextView subjects = convertView.findViewById(R.id.teacherSubjects);
        TextView difficulty = convertView.findViewById(R.id.teacherDifficulty);
        TextView hourlyRate = convertView.findViewById(R.id.hourlyRate);
        Button button = convertView.findViewById(R.id.reviews);
        Button bookLessonButton = convertView.findViewById(R.id.bookLessonButton);

        // Retrieve the user role from SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userRole = sharedPreferences.getString("role", "");

        // Populate views with teacher data
        if (teacher != null) {
            name.setText(teacher.getName());
            List<String> diff = DatabaseMapper.getDifficultiesName(getContext(),
                    teacher.getDifficultyLevels().stream().map(Integer::parseInt).collect(Collectors.toList()));
            List<String> subj = DatabaseMapper.getSubjectsName(getContext(),
                    teacher.getSubjects().stream().map(Integer::parseInt).collect(Collectors.toList()));

            subjects.setText("Subjects: " + (subj != null ? String.join(", ", subj) : "No subjects"));
            difficulty.setText("Difficulty Levels: " + (diff != null ? String.join(", ", diff) : "No difficulty levels"));
            hourlyRate.setText("Hourly Rate: " + teacher.getHourlyRate() + "$");
            button.setOnClickListener(v -> {
                // Navigate to ReviewActivity
                Intent intent = new Intent(getContext(), ReviewActivity.class);
                intent.putExtra("teacherId", teacher.getId());
                getContext().startActivity(intent);
            });

            // Hide the bookLessonButton if the role is "teacher"
            if ("teacher".equals(userRole)) {
                bookLessonButton.setVisibility(View.GONE);
            } else {
                bookLessonButton.setOnClickListener(v -> {
                    // Navigate to SubjectActivity
                    Intent intent = new Intent(getContext(), SubjectActivity.class);
                    intent.putExtra("teacherId", teacher.getId());
                    intent.putExtra("teacherName", teacher.getName());
                    intent.putExtra("subjectId", String.join(",", teacher.getSubjects()));
                    intent.putExtra("difficultyId", String.join(",", teacher.getDifficultyLevels()));
                    getContext().startActivity(intent);
                });
            }
        }

        return convertView;
    }
}
