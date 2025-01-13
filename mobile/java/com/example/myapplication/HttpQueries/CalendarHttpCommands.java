package com.example.myapplication.HttpQueries;

import android.content.Context;

import com.example.myapplication.HttpModels.CalendarResponse;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class CalendarHttpCommands {
    // Method to retrieve the calendar for a specific teacher
    public static CalendarResponse getCalendar(Context context, String authToken, int teacherId) throws ExecutionException, InterruptedException {
        // Construct the endpoint URL
        String endpoint = "/api/calendar/" + teacherId;

        // Send the HTTP GET request and return the CalendarResponse
        return HttpCommands
                .command(context, endpoint, new JSONObject(), "GET", authToken, 200, CalendarResponse.class);
    }
}
