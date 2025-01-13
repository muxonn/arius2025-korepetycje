package com.example.myapplication.HttpModels;

import org.json.JSONException;
import org.json.JSONObject;

public class Lesson {
    // Fields for the Lesson class
    private final int studentId;
    private String date;
    private String status;
    private float price;
    private int id;
    private int subjectId;
    private int difficultyId;
    private int teacherId;
    private String isReviewed;
    private String isReported;

    // Constructor with parameters
    public Lesson(String date, String status, float price, int id, int subjectId, int teacherId, String isReviewed, String isReported, int difficultyId, int studentId) {
        this.date = date;
        this.status = status;
        this.price = price;
        this.id = id;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.isReviewed = isReviewed;
        this.isReported = isReported;
        this.difficultyId = difficultyId;
        this.studentId = studentId;
    }

    // Static method to create a Lesson object from a JSON object
    public static Lesson fromJson(JSONObject jsonObject) throws JSONException {
        String date = jsonObject.getString("date");
        String status = jsonObject.getString("status");
        float price = Float.parseFloat(jsonObject.getString("price"));
        int id = jsonObject.getInt("id");
        int subjectId = jsonObject.getInt("subject");
        int teacherId = jsonObject.getInt("teacher_id");
        String isReviewed = jsonObject.getString("is_reviewed");
        String isReported = jsonObject.getString("is_reported");
        int difficultyId = jsonObject.getInt("difficulty_id");
        int studentId = jsonObject.getInt("student_id");
        return new Lesson(date, status, price, id, subjectId, teacherId, isReviewed, isReported, difficultyId, studentId);
    }

    // Getters
    public int getStudentId() {
        return studentId;
    }

    public int getDifficultyId() {
        return difficultyId;
    }

    public String getIsReviewed() {
        return isReviewed;
    }

    public String getIsReported() {
        return isReported;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public float getPrice() {
        return price;
    }

    public int getId() {
        return id;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public int getTeacherId() {
        return teacherId;
    }
}
