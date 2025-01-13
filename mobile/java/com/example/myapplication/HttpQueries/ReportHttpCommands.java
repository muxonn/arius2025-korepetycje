package com.example.myapplication.HttpQueries;

import android.content.Context;

import com.example.myapplication.HttpModels.HttpResponse;
import com.example.myapplication.HttpModels.ReportHttpResponse;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class ReportHttpCommands {
    // Method to make a report
    public static HttpResponse makeReport(Context context, String authToken, JSONObject jsonObject) throws ExecutionException, InterruptedException {
        String endpoint = "/api/report";
        return HttpCommands
                .command(context, endpoint, jsonObject, "POST", authToken, 201, HttpResponse.class);
    }

    // Method to see a report by lesson ID
    public static ReportHttpResponse seeReport(Context context, String authToken, int lessonId) throws ExecutionException, InterruptedException {
        String endpoint = "/api/report/" + lessonId;
        return HttpCommands
                .command(context, endpoint, new JSONObject(), "GET", authToken, 200, ReportHttpResponse.class);
    }
}
