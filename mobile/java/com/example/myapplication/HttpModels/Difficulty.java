package com.example.myapplication.HttpModels;

import org.json.JSONException;
import org.json.JSONObject;

public class Difficulty {
    private int id;
    private String name;

    // Constructor with parameters
    public Difficulty(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Static method to create a Difficulty object from a JSON object
    public static Difficulty fromJson(JSONObject jsonObject) throws JSONException {
        int id = jsonObject.getInt("id");
        String name = jsonObject.getString("name");
        return new Difficulty(id, name);
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
