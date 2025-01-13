package com.example.myapplication.HttpModels;

import org.json.JSONException;
import org.json.JSONObject;

public class ReportHttpResponse extends HttpResponse {
    private String studentName;
    private String teacherName;
    private int subjectId;
    private String date;
    private String homework;
    private int progressRating;
    private String comment;

    // Default constructor
    public ReportHttpResponse() {
        super();
    }

    // Constructor with parameters
    public ReportHttpResponse(int statusCode, String message, String studentName, String teacherName, int subjectId, String date, String homework, int progressRating, String comment) {
        super(statusCode, message);
        this.studentName = studentName;
        this.teacherName = teacherName;
        this.subjectId = subjectId;
        this.date = date;
        this.homework = homework;
        this.progressRating = progressRating;
        this.comment = comment;
    }

    // Static method to create a ReportHttpResponse object from a JSON object
    public static ReportHttpResponse fromJson(int statusCode, JSONObject jsonObject) throws JSONException {
        JSONObject jsonParsed = new JSONObject(jsonObject.getString("report"));
        HttpResponse httpResponse = HttpResponse.fromJson(statusCode, jsonParsed);
        String studentName = jsonParsed.getString("student_name");
        String teacherName = jsonParsed.getString("teacher_name");
        int subjectId = jsonParsed.getInt("subject");
        String date = jsonParsed.getString("date");
        String homework = jsonParsed.getString("homework");
        int progressRating = jsonParsed.getInt("progress_rating");
        String comment = jsonParsed.getString("comment");
        return new ReportHttpResponse(statusCode, httpResponse.getMessage(), studentName, teacherName, subjectId, date, homework, progressRating, comment);
    }

    // Getters
    public String getStudentName() {
        return studentName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getDate() {
        return date;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public String getHomework() {
        return homework;
    }

    public int getProgressRating() {
        return progressRating;
    }

    public String getComment() {
        return comment;
    }
}
