package com.example.myapplication.HttpModels;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LessonHttpResponse extends HttpResponse {
    private List<Lesson> lessons;

    // Default constructor
    public LessonHttpResponse() {
        super();
    }

    // Constructor with parameters
    public LessonHttpResponse(int statusCode, String message, List<Lesson> lessons) {
        super(statusCode, message);
        this.lessons = lessons;
    }

    // Static method to create a LessonHttpResponse object from a JSON object
    public static LessonHttpResponse fromJson(int statusCode, JSONObject jsonObject) throws JSONException {
        // Parse the base response
        HttpResponse baseResponse = HttpResponse.fromJson(statusCode, jsonObject);
        List<Lesson> lessons = new ArrayList<>();
        JSONArray jsonArray = jsonObject.getJSONArray("lesson_list");

        // Convert JSON array to a list of Lesson objects
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject lessonObject = jsonArray.getJSONObject(i);
            lessons.add(Lesson.fromJson(lessonObject));
        }

        return new LessonHttpResponse(statusCode, baseResponse.getMessage(), lessons);
    }

    // Getter for the list of lessons
    public List<Lesson> getLessons() {
        return lessons;
    }
}
