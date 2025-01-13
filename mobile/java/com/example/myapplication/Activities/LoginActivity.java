package com.example.myapplication.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.HttpModels.LoginHttpResponse;
import com.example.myapplication.HttpQueries.AuthHttpCommands;
import com.example.myapplication.R;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEditText, passwordEditText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the SharedPreferences instance
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        setContentView(R.layout.activity_login);

        // Initialize UI components
        loginEditText = findViewById(R.id.loginEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button loginSubmitButton = findViewById(R.id.loginSubmitButton);

        // Handle login button click
        loginSubmitButton.setOnClickListener(v -> {
            // Get email and password from the input fields
            String email = loginEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Create JSON object with email and password
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("email", email);
                jsonObject.put("password", password);
            } catch (Exception e) {
                Toast.makeText(this, "JSON creating error!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Execute network request on UI thread
            runOnUiThread(() -> {
                try {
                    // Perform login HTTP request
                    LoginHttpResponse response = AuthHttpCommands.login(this, jsonObject);
                    if (response.getStatusCode() == 200) {
                        Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();

                        // Save auth token and role in SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("authToken", response.getAuthToken());
                        editor.putString("role", response.getRole());
                        editor.apply();

                        // Redirect to the main activity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Error: " + response.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Incorrect server response!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
