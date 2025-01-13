package com.example.myapplication.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.Activities.GiveReviewActivity;
import com.example.myapplication.Activities.MakeReportActivity;
import com.example.myapplication.Activities.SeeReportActivity;
import com.example.myapplication.HttpModels.HttpResponse;
import com.example.myapplication.HttpModels.Lesson;
import com.example.myapplication.HttpQueries.InvoiceHttpCommands;
import com.example.myapplication.R;
import com.example.myapplication.mappers.DatabaseMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class PastLessonsAdapter extends ArrayAdapter<Lesson> {

    public PastLessonsAdapter(@NonNull Context context, @NonNull List<Lesson> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_past_lessons, parent, false);
        }
        Lesson lesson = getItem(position);
        TextView status = convertView.findViewById(R.id.status);
        TextView date = convertView.findViewById(R.id.date);
        TextView subject = convertView.findViewById(R.id.subject);
        TextView teacherName = convertView.findViewById(R.id.teacherName);
        TextView difficultyName = convertView.findViewById(R.id.difficultyName);
        TextView studentId = convertView.findViewById(R.id.studentId);
        Button review = convertView.findViewById(R.id.review);
        Button report = convertView.findViewById(R.id.report);
        Button invoice = convertView.findViewById(R.id.invoice);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userRole = sharedPreferences.getString("role", "");
        String authToken = sharedPreferences.getString("authToken", "");

        // Fill views with lesson data
        if (lesson != null) {
            status.setText("Lesson " + lesson.getStatus());
            date.setText("Date: " + lesson.getDate());
            String diff = DatabaseMapper.getDifficultyName(getContext(), lesson.getDifficultyId());
            difficultyName.setText("Difficulty level: " + diff);
            String name = DatabaseMapper.getSubjectName(getContext(), lesson.getSubjectId());
            subject.setText("Subject: " + name);
            String teacherNam;
            try {
                teacherNam = DatabaseMapper.getTeacherNameById(getContext(), lesson.getTeacherId(), authToken);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            teacherName.setText("Teacher: " + teacherNam);
            studentId.setText("Student id: " + lesson.getStudentId());

            if ("student".equals(userRole)) {
                review.setVisibility(View.VISIBLE);
                review.setOnClickListener(v -> {
                    // Navigate to GiveReviewActivity
                    Intent intent = new Intent(getContext(), GiveReviewActivity.class);
                    intent.putExtra("teacher", teacherNam);
                    String diffLevel = DatabaseMapper.getDifficultyName(getContext(), lesson.getDifficultyId());
                    intent.putExtra("difficultyLevel", diffLevel);
                    String subj = DatabaseMapper.getSubjectName(getContext(), lesson.getSubjectId());
                    intent.putExtra("subject", subj);
                    intent.putExtra("date", String.valueOf(lesson.getDate()));
                    intent.putExtra("teacherId", String.valueOf(lesson.getTeacherId()));
                    getContext().startActivity(intent);
                });
                invoice.setVisibility(View.VISIBLE);
                invoice.setOnClickListener(v -> {
                    // Request and display invoice
                    JSONObject jsonObject = new JSONObject();
                    HttpResponse httpResponse;
                    try {
                        jsonObject.put("lesson_id", lesson.getId());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        httpResponse = InvoiceHttpCommands.getInvoice(getContext(), authToken, jsonObject);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(getContext(), httpResponse.getMessage().replace("}", ""), Toast.LENGTH_SHORT).show();
                });
            } else {
                review.setVisibility(View.GONE);
                invoice.setVisibility(View.GONE);
            }

            if (Objects.equals(lesson.getIsReported(), "false")) {
                if ("teacher".equals(userRole)) {
                    report.setVisibility(View.VISIBLE);
                    report.setText("Make a report");
                    report.setOnClickListener(v -> {
                        // Navigate to MakeReportActivity
                        Intent intent = new Intent(getContext(), MakeReportActivity.class);
                        intent.putExtra("lessonId", String.valueOf(lesson.getId()));
                        intent.putExtra("teacher", teacherNam);
                        String diffLevel = DatabaseMapper.getDifficultyName(getContext(), lesson.getDifficultyId());
                        intent.putExtra("difficultyLevel", diffLevel);
                        String subj = DatabaseMapper.getSubjectName(getContext(), lesson.getSubjectId());
                        intent.putExtra("subject", subj);
                        intent.putExtra("date", String.valueOf(lesson.getDate()));
                        intent.putExtra("studentId", String.valueOf(lesson.getStudentId()));
                        getContext().startActivity(intent);
                    });
                } else {
                    report.setEnabled(false);
                    report.setText("The teacher hasn't submitted the report for this lesson yet");
                    report.setBackgroundColor(Color.GRAY);
                }
            } else {
                report.setVisibility(View.VISIBLE);
                report.setOnClickListener(v -> {
                    // Navigate to SeeReportActivity
                    Intent intent = new Intent(getContext(), SeeReportActivity.class);
                    intent.putExtra("lessonId", String.valueOf(lesson.getId()));
                    intent.putExtra("teacher", teacherNam);
                    String diffLevel = DatabaseMapper.getDifficultyName(getContext(), lesson.getDifficultyId());
                    intent.putExtra("difficultyLevel", diffLevel);
                    String subj = DatabaseMapper.getSubjectName(getContext(), lesson.getSubjectId());
                    intent.putExtra("subject", subj);
                    intent.putExtra("date", String.valueOf(lesson.getDate()));
                    intent.putExtra("studentId", String.valueOf(lesson.getStudentId()));
                    getContext().startActivity(intent);
                });
            }
        }

        return convertView;
    }
}
