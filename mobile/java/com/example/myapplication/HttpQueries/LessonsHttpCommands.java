package com.example.myapplication.HttpQueries;

import android.content.Context;

import com.example.myapplication.HttpModels.HttpResponse;
import com.example.myapplication.HttpModels.LessonHttpResponse;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class LessonsHttpCommands {
    // Method to get lessons by teacher ID
    public static LessonHttpResponse getLessonsByTeacherId(Context context, String authToken, int teacherId) throws ExecutionException, InterruptedException {
        String endpoint = "/api/lesson/" + teacherId;
        return HttpCommands
                .command(context, endpoint, new JSONObject(), "GET", authToken, 200, LessonHttpResponse.class);
    }

    // Method to post a new lesson
    public static LessonHttpResponse postLesson(Context context, JSONObject jsonObject, String authToken) throws ExecutionException, InterruptedException {
        String endpoint = "/api/lesson";
        return HttpCommands
                .command(context, endpoint, jsonObject, "POST", authToken, 200, LessonHttpResponse.class);
    }

    // Method to get completed lessons
    public static LessonHttpResponse getCompletedLessons(Context context, String authToken) throws ExecutionException, InterruptedException {
        String endpoint = "/api/lesson?status=completed";
        return HttpCommands
                .command(context, endpoint, new JSONObject(), "GET", authToken, 200, LessonHttpResponse.class);
    }

    // Method to give a review
    public static HttpResponse giveReview(Context context, String authToken, JSONObject jsonObject, int teacherId) throws ExecutionException, InterruptedException {
        String endpoint = "/api/teacher-reviews/" + teacherId;
        return HttpCommands
                .command(context, endpoint, jsonObject, "POST", authToken, 200, HttpResponse.class);
    }
}
