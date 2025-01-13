package com.example.myapplication.HttpModels;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TeacherReviewsHttpResponse extends HttpResponse {
    private List<TeacherReview> teacherReviews;

    // Getter for the list of teacher reviews
    public List<TeacherReview> getTeacherReviews() {
        return teacherReviews;
    }

    // Constructor with parameters
    public TeacherReviewsHttpResponse(int statusCode, String message, List<TeacherReview> teacherReviews) {
        super(statusCode, message);
        this.teacherReviews = teacherReviews;
    }

    // Static method to create a TeacherReviewsHttpResponse object from a JSON object
    public static TeacherReviewsHttpResponse fromJson(int statusCode, JSONObject jsonObject) throws JSONException {
        // Parse the base response
        HttpResponse baseResponse = HttpResponse.fromJson(statusCode, jsonObject);
        JSONArray teacherArray = jsonObject.getJSONArray("reviews");
        List<TeacherReview> teacherReviews = new ArrayList<>();

        // Convert JSON array to a list of TeacherReview objects
        for (int i = 0; i < teacherArray.length(); i++) {
            JSONObject teacherJson = teacherArray.getJSONObject(i);
            teacherReviews.add(TeacherReview.fromJson(teacherJson));
        }

        return new TeacherReviewsHttpResponse(baseResponse.getStatusCode(), baseResponse.getMessage(), teacherReviews);
    }
}
