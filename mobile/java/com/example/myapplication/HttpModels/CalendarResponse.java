package com.example.myapplication.HttpModels;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CalendarResponse extends HttpResponse {
    String availableFrom;
    String availableUntil;
    List<Integer> workingDays;

    public CalendarResponse() {
        super();
    }

    public CalendarResponse(int statusCode, String message, String availableFrom, String availableUntil, List<Integer> workingDays) {
        super(statusCode, message);
        this.availableFrom = availableFrom;
        this.availableUntil = availableUntil;
        this.workingDays = workingDays;
    }

    public String getAvailableUntil() {
        return availableUntil;
    }

    public String getAvailableFrom() {
        return availableFrom;
    }

    public List<Integer> getWorkingDays() {
        return workingDays;
    }

    public static CalendarResponse fromJson(int statusCode, JSONObject jsonObject) throws JSONException {
        HttpResponse baseResponse = HttpResponse.fromJson(statusCode, jsonObject);
        String availableFrom = jsonObject.getString("available_from");
        String availableUntil = jsonObject.getString("available_until");
        String workingDaysString = jsonObject.getString("working_days");
        List<Integer> workingDays = Arrays.stream(workingDaysString.replace("{", "").replace("}", "").split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        return new CalendarResponse(statusCode, baseResponse.getMessage(), availableFrom, availableUntil, workingDays);
    }
}
