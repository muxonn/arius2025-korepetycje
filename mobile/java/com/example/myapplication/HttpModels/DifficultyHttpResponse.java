package com.example.myapplication.HttpModels;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DifficultyHttpResponse extends HttpResponse {
    private List<Difficulty> subjects;

    // Constructor with parameters
    public DifficultyHttpResponse(int statusCode, String message, List<Difficulty> subjects) {
        super(statusCode, message);
        this.subjects = subjects;
    }

    // Default constructor
    public DifficultyHttpResponse() {
        super();
    }

    // Static method to create a DifficultyHttpResponse object from a JSON object
    public static DifficultyHttpResponse fromJson(int statusCode, JSONObject jsonObject) throws JSONException {
        // Parse the base response
        HttpResponse baseResponse = HttpResponse.fromJson(statusCode, jsonObject);
        JSONArray subjectsList = jsonObject.getJSONArray("difficulty_levels");
        List<Difficulty> subjects = new ArrayList<>();

        // Convert JSON array to a list of Difficulty objects
        for (int i = 0; i < subjectsList.length(); i++) {
            JSONObject subjectJson = subjectsList.getJSONObject(i);
            subjects.add(Difficulty.fromJson(subjectJson));
        }

        return new DifficultyHttpResponse(baseResponse.getStatusCode(), baseResponse.getMessage(), subjects);
    }

    // Getter for the list of difficulties
    public List<Difficulty> getDifficulties() {
        return subjects;
    }
}
