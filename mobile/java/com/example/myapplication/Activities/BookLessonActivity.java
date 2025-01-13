package com.example.myapplication.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapters.DayScheduleAdapter;
import com.example.myapplication.HttpModels.CalendarResponse;
import com.example.myapplication.HttpModels.DaySchedule;
import com.example.myapplication.HttpModels.LessonHttpResponse;
import com.example.myapplication.HttpQueries.CalendarHttpCommands;
import com.example.myapplication.HttpQueries.LessonsHttpCommands;
import com.example.myapplication.R;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class BookLessonActivity extends AppCompatActivity {
    private int teacherId;
    private DaySchedule daySchedule;
    private String authToken;
    private int difficultyId;
    private String teacherName;
    private int subjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_lesson);
        Button bookLessonButton = findViewById(R.id.bookLessonButton);
        Button backButton = findViewById(R.id.backButton);

        // Retrieve data from the Intent
        teacherId = getIntent().getIntExtra("teacherId", -1);
        subjectId = getIntent().getIntExtra("subjectId", -1);
        difficultyId = getIntent().getIntExtra("difficultyId", -1);
        teacherName = getIntent().getStringExtra("teacherName");

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");

        // Handle back button to return to the previous activity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(BookLessonActivity.this, TeachersActivity.class);
            startActivity(intent);
            finish();
        });

        CalendarResponse calendarResponse;
        LessonHttpResponse lessonHttpResponse;
        try {
            // Fetch teacher's schedule from the API
            calendarResponse = CalendarHttpCommands.getCalendar(this, authToken, teacherId);
            // Fetch lessons by teacher's ID from the API
            lessonHttpResponse = LessonsHttpCommands.getLessonsByTeacherId(this, authToken, teacherId);
            daySchedule = new DaySchedule(calendarResponse, lessonHttpResponse.getLessons());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        RecyclerView scheduleRecyclerView = findViewById(R.id.scheduleRecyclerView);
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<String> days = new ArrayList<>();
        List<List<String>> allTimeSlots = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat dateFormatForGenerator = new SimpleDateFormat("dd/MM/yyyy");

        for (int i = 0; i < 30; i++) {
            int day = (calendar.get(Calendar.DAY_OF_WEEK) - 1 == 0) ? 7 : calendar.get(Calendar.DAY_OF_WEEK) - 1;
            if (daySchedule.getWorkingDays().contains(day)) {
                int hourAvailableFrom = Integer.parseInt(daySchedule.getAvailableFrom().split(":")[0]);
                int hourAvailableUntil = Integer.parseInt(daySchedule.getAvailableUntil().split(":")[0]);
                String date = dateFormat.format(calendar.getTime()) + " " + getDayName(calendar.get(Calendar.DAY_OF_WEEK));
                days.add(date);
                List<String> occupiedHours = daySchedule.getOccupiedDates().stream()
                        .filter(s -> s.contains(dateFormatForGenerator.format(calendar.getTime())))
                        .map(s -> s.split(" ")[1])
                        .collect(Collectors.toList());
                if (i == 0 && calendar.get(Calendar.HOUR_OF_DAY) > hourAvailableFrom) {
                    hourAvailableFrom = calendar.get(Calendar.HOUR_OF_DAY) + 1;
                }
                List<String> timeSlots = generateTimeSlots(occupiedHours, hourAvailableFrom, hourAvailableUntil);
                allTimeSlots.add(timeSlots.isEmpty() ? List.of("No available times") : timeSlots);
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Set the RecyclerView adapter
        DayScheduleAdapter adapter = new DayScheduleAdapter(this, days, allTimeSlots);
        scheduleRecyclerView.setAdapter(adapter);

        // Handle lesson booking button click
        bookLessonButton.setOnClickListener(v -> {
            String selectedDay = adapter.getSelectedSlot();
            if (selectedDay != null) {
                try {
                    bookLesson(selectedDay);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    // Method to return the day name based on the day of the week number
    private String getDayName(int dayOfWeek) {
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",};
        return days[(dayOfWeek - 1) % 7];
    }

    // Method to generate available time slots
    private List<String> generateTimeSlots(List<String> occupiedHours, int startHour, int endHour) {
        List<String> timeSlots = new ArrayList<>();
        for (int hour = startHour; hour < endHour; hour++) {
            String slot = hour + ":00 - " + (hour + 1) + ":00";
            if (!occupiedHours.contains(hour + ":00")) {
                timeSlots.add(slot);
            }
        }
        return timeSlots;
    }

    // Method to book a lesson
    private void bookLesson(String selectedDay) throws ParseException {
        LessonHttpResponse lessonHttpResponse;
        JSONObject jsonObject = new JSONObject();
        String dateParsed = selectedDay.replace("-", "/");
        try {
            jsonObject.put("date", dateParsed);
            jsonObject.put("subject_id", subjectId);
            jsonObject.put("teacher_id", teacherId);
            jsonObject.put("difficulty_id", difficultyId);
        } catch (Exception e) {
            Toast.makeText(this, "JSON creating error!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // Call API to book the lesson
            lessonHttpResponse = LessonsHttpCommands.postLesson(this, jsonObject, authToken);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Implement the action to book a lesson
        if (lessonHttpResponse.getMessage().equals("")) {
            Toast.makeText(this, "Lesson successfully booked on " + selectedDay, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(BookLessonActivity.this, PortalActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, lessonHttpResponse.getMessage().replace("}", ""), Toast.LENGTH_SHORT).show();
        }
    }
}
