package com.example.myapplication.HttpModels;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class Teacher {
    private String bio;
    private List<String> difficultyLevels;
    private int id;
    private String name;
    private List<String> subjects;
    private String hourlyRate;

    // Constructor with parameters
    public Teacher(String bio, List<String> difficultyLevels, int id, String name, List<String> subjects, String hourlyRate) {
        this.bio = bio;
        this.difficultyLevels = difficultyLevels;
        this.id = id;
        this.name = name;
        this.subjects = subjects;
        this.hourlyRate = hourlyRate;
    }

    // Static method to create a Teacher object from a JSON object
    public static Teacher fromJson(JSONObject jsonObject) throws JSONException {
        String bio = jsonObject.optString("bio");
        List<String> difficultyLevels = Arrays.asList(jsonObject.getString("difficulty_levels").replace("{", "").replace("}", "").split(","));
        int id = jsonObject.getInt("id");
        String name = jsonObject.getString("name");
        String hourlyRate = jsonObject.getString("hourly_rate");
        List<String> subjects = Arrays.asList(jsonObject.getString("subjects").replace("{", "").replace("}", "").split(","));
        return new Teacher(bio, difficultyLevels, id, name, subjects, hourlyRate);
    }

    // Override toString method to provide a string representation of the Teacher object
    @NonNull
    @Override
    public String toString() {
        return name + "\nSubjects: " + subjects + "\nDifficulty Levels: " + difficultyLevels;
    }

    // Getters and setters
    public List<String> getDifficultyLevels() {
        return difficultyLevels;
    }

    public void setDifficultyLevels(List<String> difficultyLevels) {
        this.difficultyLevels = difficultyLevels;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public String getHourlyRate() {
        return hourlyRate;
    }
}
