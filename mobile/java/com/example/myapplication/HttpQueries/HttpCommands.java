package com.example.myapplication.HttpQueries;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.myapplication.Activities.MainActivity;
import com.example.myapplication.HttpModels.HttpResponse;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HttpCommands {
    static final String address = "http://10.0.2.2:5000";

    // Generic method to execute HTTP commands
    public static <T extends HttpResponse> T command(Context context, String endpoint, JSONObject jsonObject, String method, String authToken, int successResponseCode, Class<T> type) throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Callable<T> task = () -> {
            HttpURLConnection connection = null;
            T httpResponse;
            int statusCode = 0;
            String message = "";
            try {
                connection = connection(endpoint, jsonObject, method, authToken);
                StringBuilder response = response(connection, successResponseCode);
                statusCode = connection.getResponseCode();
                if (statusCode == successResponseCode) {
                    JSONObject jsonObject1 = new JSONObject(response.toString());
                    message = jsonObject1.optString("message", "");
                    Method fromJsonMethod = type.getDeclaredMethod("fromJson", int.class, JSONObject.class);
                    httpResponse = (T) fromJsonMethod.invoke(type, statusCode, jsonObject1);
                } else {
                    message = messageConverter(response.toString());
                    httpResponse = type.getDeclaredConstructor().newInstance();
                    httpResponse.setStatusCode(statusCode);
                    httpResponse.setMessage(message);
                }
            } catch (Exception e) {
                httpResponse = type.getDeclaredConstructor().newInstance();
                httpResponse.setStatusCode(statusCode);
                httpResponse.setMessage(message);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            if (statusCode == 401) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("authToken");
                editor.remove("role");
                editor.apply();
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, "You have been logged out.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                });
            }
            return httpResponse;
        };

        Future<T> future = executorService.submit(task);

        T response = future.get();

        executorService.shutdown();

        return response;
    }

    // Method to convert response message
    private static String messageConverter(String response) {
        return response.split(":")[1].replace("\"", "");
    }

    // Method to establish a connection
    static HttpURLConnection connection(String endpoint, JSONObject jsonObject, String method, String authToken) throws IOException {
        String jsonData = jsonObject.toString();
        URL url = new URL(address + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        if (!authToken.isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + authToken);
        }
        if (!method.equalsIgnoreCase("GET")) {
            connection.setDoOutput(true);
            connection.getOutputStream().write(jsonData.getBytes("utf-8"));
        }
        return connection;
    }

    // Method to read the response
    static StringBuilder response(HttpURLConnection connection, int successResponseCode) throws IOException {
        StringBuilder response = new StringBuilder();
        int responseCode = connection.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                responseCode == successResponseCode ? connection.getInputStream() : connection.getErrorStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response;
    }
}
