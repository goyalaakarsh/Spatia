package com.example.spatia.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.example.spatia.model.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading data from JSON files
 */
public class JsonDataLoader {
    private static final String TAG = "JsonDataLoader";
    
    /**
     * Load products from the Products.json file
     */
    public static List<Product> loadProductsFromJson(Context context) {
        // Possible locations to try
        String[] possiblePaths = {
            "Products.json",
            "data/Products.json"
        };
        
        for (String path : possiblePaths) {
            try {
                // Try to load from assets
                AssetManager assets = context.getAssets();
                InputStream inputStream = assets.open(path);
                List<Product> products = parseJsonFromStream(inputStream);
                
                if (!products.isEmpty()) {
                    Log.d(TAG, "Successfully loaded " + products.size() + " products from: " + path);
                    return products;
                }
            } catch (IOException e) {
                Log.w(TAG, "Failed to load from asset path: " + path + ", " + e.getMessage());
            }
        }
        
        // Try to load from resources
        try {
            int resId = context.getResources().getIdentifier("products", "raw", context.getPackageName());
            if (resId != 0) {
                InputStream is = context.getResources().openRawResource(resId);
                List<Product> products = parseJsonFromStream(is);
                
                if (!products.isEmpty()) {
                    Log.d(TAG, "Successfully loaded " + products.size() + " products from resources");
                    return products;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to load from resources: " + e.getMessage());
        }
        
        // Fallback: directly read from res/data
        try {
            InputStream inputStream = context.getResources()
                .openRawResource(context.getResources()
                .getIdentifier("data/Products", "raw", context.getPackageName()));
                
            List<Product> products = parseJsonFromStream(inputStream);
            if (!products.isEmpty()) {
                return products;
            }
        } catch (Exception e) {
            Log.e(TAG, "All JSON loading attempts failed", e);
        }
        
        Log.e(TAG, "Could not load products from any location");
        return new ArrayList<>();
    }
    
    /**
     * Parse JSON from an input stream
     */
    private static List<Product> parseJsonFromStream(InputStream inputStream) throws IOException {
        StringBuilder jsonContent = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        
        while ((line = reader.readLine()) != null) {
            // Skip comments in JSON
            if (line.trim().startsWith("//")) {
                continue;
            }
            jsonContent.append(line);
        }
        
        reader.close();
        inputStream.close();
        
        try {
            Gson gson = new Gson();
            Type productListType = new TypeToken<List<Product>>(){}.getType();
            return gson.fromJson(jsonContent.toString(), productListType);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON content: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Log all available asset files for debugging purposes
     */
    public static void logAvailableAssets(Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            String[] files = assetManager.list("");
            
            Log.d(TAG, "Available assets:");
            for (String file : files) {
                Log.d(TAG, "- " + file);
                
                // Try to list subdirectories
                try {
                    String[] subFiles = assetManager.list(file);
                    for (String subFile : subFiles) {
                        Log.d(TAG, "  └─ " + file + "/" + subFile);
                    }
                } catch (IOException e) {
                    // Not a directory, ignore
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error listing assets", e);
        }
    }
}
