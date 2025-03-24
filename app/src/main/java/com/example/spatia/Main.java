package com.example.spatia;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Main extends Activity {
    private static final String TAG = "SpatiaMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Application started");

        // Setup Retrofit for API communication
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://your-api-url.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Now you can use service to make API calls
        Log.d(TAG, "API service initialized");
    }
}