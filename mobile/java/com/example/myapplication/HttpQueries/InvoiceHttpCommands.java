package com.example.myapplication.HttpQueries;

import android.content.Context;

import com.example.myapplication.HttpModels.HttpResponse;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class InvoiceHttpCommands {
    // Method to request an invoice
    public static HttpResponse getInvoice(Context context, String authToken, JSONObject jsonObject) throws ExecutionException, InterruptedException {
        String endpoint = "/api/invoice";
        return HttpCommands
                .command(context, endpoint, jsonObject, "POST", authToken, 201, HttpResponse.class);
    }
}
