package com.example.myapplication.HttpQueries;

import android.content.Context;

import com.example.myapplication.HttpModels.HttpResponse;
import com.example.myapplication.HttpModels.LoginHttpResponse;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class AuthHttpCommands {
    // Method for user login
    public static LoginHttpResponse login(Context context, JSONObject jsonObject) throws ExecutionException, InterruptedException {
        return HttpCommands
                .command(context, "/auth/login", jsonObject, "POST", "", 200, LoginHttpResponse.class);
    }

    // Method for user registration
    public static HttpResponse register(Context context, JSONObject jsonObject) throws ExecutionException, InterruptedException {
        return HttpCommands
                .command(context, "/auth/register", jsonObject, "POST", "", 201, HttpResponse.class);
    }
}
