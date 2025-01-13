package com.example.myapplication.HttpModels;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SubjectHttpResponse extends HttpResponse {
    private List<Subject> subjects;

    // Constructor with parameters
    public SubjectHttpResponse(int statusCode, String message, List<Subject> subjects) {
        super(statusCode, message);
        this.subjects = subjects;
    }

    // Default constructor
    public SubjectHttpResponse() {
        super();
    }

    // Static method to create a SubjectHttpResponse object from a JSON object
    public static SubjectHttpResponse fromJson(int statusCode, JSONObject jsonObject) throws JSONException {
        // Parse the base response
        HttpResponse baseResponse = HttpResponse.fromJson(statusCode, jsonObject);
        JSONArray subjectsList = jsonObject.getJSONArray("subjects");
        List<Subject> subjects = new ArrayList<>();

        // Convert JSON array to a list of Subject objects
        for (int i = 0; i < subjectsList.length(); i++) {
            JSONObject subjectJson = subjectsList.getJSONObject(i);
            subjects.add(Subject.fromJson(subjectJson));
        }

        return new SubjectHttpResponse(baseResponse.getStatusCode(), baseResponse.getMessage(), subjects);
    }

    // Getter for the list of subjects
    public List<Subject> getSubjects() {
        return subjects;
    }
}
