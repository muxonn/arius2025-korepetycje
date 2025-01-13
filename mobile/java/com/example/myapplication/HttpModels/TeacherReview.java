package com.example.myapplication.HttpModels;

import org.json.JSONException;
import org.json.JSONObject;

public class TeacherReview {
    private String comment;
    private String createdAt;
    private int rating;

    // Constructor with parameters
    public TeacherReview(String comment, String createdAt, int rating) {
        this.comment = comment;
        this.createdAt = createdAt;
        this.rating = rating;
    }

    // Static method to create a TeacherReview object from a JSON object
    public static TeacherReview fromJson(JSONObject jsonObject) throws JSONException {
        String comment = jsonObject.optString("comment");
        int rating = jsonObject.getInt("rating");
        String date = jsonObject.getString("created_at");
        return new TeacherReview(comment, date, rating);
    }

    // Getters
    public String getComment() {
        return comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getRating() {
        return rating;
    }
}
