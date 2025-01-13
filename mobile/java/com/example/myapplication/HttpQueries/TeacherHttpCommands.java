package com.example.myapplication.HttpQueries;

import android.content.Context;

import com.example.myapplication.HttpModels.DifficultyHttpResponse;
import com.example.myapplication.HttpModels.SubjectHttpResponse;
import com.example.myapplication.HttpModels.TeacherResponse;
import com.example.myapplication.HttpModels.TeacherReviewsHttpResponse;

import org.json.JSONObject;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class TeacherHttpCommands {
    public static TeacherResponse getTeacherList(Context context,String authToken, String subject, String difficultyLevel) throws ExecutionException, InterruptedException {
        if (Objects.equals(subject, "All")) {
            subject = "";
        }
        if (Objects.equals(difficultyLevel, "All")) {
            difficultyLevel = "";
        }
        String endpoint = String.format("/api/teacher-list?subject=%s&difficulty_id=%s", subject, difficultyLevel);
        return HttpCommands
                .command(context,endpoint, new JSONObject(), "GET", authToken, 200, TeacherResponse.class);
    }

    public static SubjectHttpResponse getAllSubjects(Context context) throws ExecutionException, InterruptedException {
        String endpoint = "/api/subjects";
        return HttpCommands
                .command(context,endpoint, new JSONObject(), "GET", "", 200, SubjectHttpResponse.class);
    }

    public static TeacherReviewsHttpResponse getTeacherReviewsById(Context context,int id, String authToken) throws ExecutionException, InterruptedException {
        String endpoint = "/api/teacher-reviews/" + id;
        return HttpCommands
                .command(context,endpoint, new JSONObject(), "GET", authToken, 200, TeacherReviewsHttpResponse.class);
    }
    public static DifficultyHttpResponse getAllDifficulties(Context context) throws ExecutionException, InterruptedException {
        String endpoint = "/api/difficulty-levels";
        return HttpCommands
                .command(context,endpoint, new JSONObject(), "GET", "", 200, DifficultyHttpResponse.class);
    }

}
