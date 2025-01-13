package com.example.myapplication.HttpModels;

import org.json.JSONException;
import org.json.JSONObject;

public class Subject {
    private int id;
    private String name;

    // Constructor with parameters
    public Subject(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Static method to create a Subject object from a JSON object
    public static Subject fromJson(JSONObject jsonObject) throws JSONException {
        int id = jsonObject.getInt("id");
        String name = jsonObject.getString("name");
        return new Subject(id, name);
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
