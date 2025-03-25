package com.example.spatia;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.spatia.database.FirebaseDatabaseManager;
import com.example.spatia.database.FirestoreManager;
import com.example.spatia.model.Product;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Main extends Activity {
    private static final String TAG = "SpatiaMain";
    private FirestoreManager firestoreManager;
    // If you need to use Realtime Database as well
    private FirebaseDatabaseManager firebaseDBManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Application started - initializing Firebase");
        
        try {
            // Initialize Firestore
            firestoreManager = FirestoreManager.getInstance(this);
            
            // Load products from JSON to Firestore
            firestoreManager.loadProductsFromJson();
            
            // Once data is added, retrieve and display
            loadAndDisplayProducts();
            
        } catch (Exception e) {
            Log.e(TAG, "Error during application initialization", e);
            Toast.makeText(this, "Application error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void loadAndDisplayProducts() {
        // Wait a moment to ensure products are loaded
        findViewById(android.R.id.content).postDelayed(() -> {
            firestoreManager.getAllProducts(products -> {
                if (products.isEmpty()) {
                    Toast.makeText(Main.this, "No products found in Firestore", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Log.d(TAG, "Loaded " + products.size() + " products from Firestore");
                
                // Count products by category
                Map<String, Integer> categoryCounts = new HashMap<>();
                for (Product product : products) {
                    String category = product.getCategory();
                    categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
                }
                
                // Build message
                StringBuilder message = new StringBuilder("Loaded " + products.size() + " products from Firestore:\n");
                for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                    message.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
                
                // Show results
                Toast.makeText(Main.this, message.toString(), Toast.LENGTH_LONG).show();
            });
        }, 2000); // Wait 2 seconds to ensure products are loaded
    }
}