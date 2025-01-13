package com.example.myapplication.HttpModels;

import org.json.JSONException;
import org.json.JSONObject;

public class HttpResponse {
    private int statusCode;
    private String message;

    // Default constructor
    public HttpResponse() {

    }

    // Constructor with parameters
    public HttpResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    // Static method to create an HttpResponse object from a JSON object
    public static HttpResponse fromJson(int statusCode, JSONObject jsonObject) throws JSONException {
        // Extract message from the JSON object and clean up the braces
        String message = jsonObject.optString("message").replace("{", "").replace("}", "");
        return new HttpResponse(statusCode, message);
    }

    // Getter for statusCode
    public int getStatusCode() {
        return statusCode;
    }

    // Setter for statusCode
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    // Getter for message
    public String getMessage() {
        return message;
    }

    // Setter for message
    public void setMessage(String message) {
        this.message = message;
    }
}
