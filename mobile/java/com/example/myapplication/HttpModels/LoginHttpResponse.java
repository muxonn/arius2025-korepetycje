package com.example.myapplication.HttpModels;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginHttpResponse extends HttpResponse {
    String authToken;
    String role;

    public LoginHttpResponse(int statusCode, String message, String authToken, String role) {
        super(statusCode, message);
        this.authToken = authToken;
        this.role = role;
    }

    public static LoginHttpResponse fromJson(int statusCode, JSONObject jsonObject) throws JSONException {
        HttpResponse baseResponse = HttpResponse.fromJson(statusCode, jsonObject);
        String authToken = jsonObject.optString("access_token");
        String role = jsonObject.optString("role");
        return new LoginHttpResponse(baseResponse.getStatusCode(), baseResponse.getMessage(), authToken,role);
    }

    public LoginHttpResponse() {
        super();
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    public String getRole(){
        return role;
    }
}
