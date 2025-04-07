package com.example.spatia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spatia.activities.AuthActivity;
import com.example.spatia.activities.CartActivity;
import com.example.spatia.activities.ProfileActivity;
import com.example.spatia.activities.ProductsActivity;
import com.example.spatia.activities.TempNavigationActivity;
import com.example.spatia.activities.WelcomeActivity;
import com.example.spatia.model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Enable up navigation/back button in action bar
        // if (getSupportActionBar() != null) {
        //     getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //     getSupportActionBar().setDisplayShowHomeEnabled(true);
        // }

        FirebaseUser currentUser = mAuth.getCurrentUser();

        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
        finish();

//        if (currentUser == null) {
//            Intent intent = new Intent(this, AuthActivity.class);
//            startActivity(intent);
//            finish();
//        } else {
//            setContentView(R.layout.temp_navigation);
//            Toast.makeText(this, "Welcome back, " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
//
//        }
//         insertProducts();
    }
    
    // Handle back button click in the action bar
    // @Override
    // public boolean onSupportNavigateUp() {
    //     onBackPressed();
    //     return true;
    // }
    
    private void setupNavigationButtons() {
        // Profile button
        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        
        // Cart button
        findViewById(R.id.btnCart).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            startActivity(intent);
        });
        
        // Products button
        findViewById(R.id.btnProducts).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProductsActivity.class);
            startActivity(intent);
        });
        
        // Home button (stays in MainActivity)
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "You are already at Home", Toast.LENGTH_SHORT).show();
        });
        
        // Auth button
        findViewById(R.id.btnAuth).setOnClickListener(v -> {
            // Sign out if user is logged in
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
            startActivity(intent);
            finish();
        });
        
        // Handle other buttons as needed
        handleOtherButtons();
    }
    
    private void handleOtherButtons() {
        // Search button
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Search feature coming soon", Toast.LENGTH_SHORT).show();
        });
        
        // Orders button
        findViewById(R.id.btnOrders).setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Orders feature coming soon", Toast.LENGTH_SHORT).show();
        });
        
        // Settings button
        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Settings feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    // Inserts products from JSON file into Firestore collection "products" (not supposed to be used everytime)
    private void insertProducts() {
        try {
            InputStream inputStream = getAssets().open("products.json");
            InputStreamReader reader = new InputStreamReader(inputStream);
            Gson gson = new Gson();
            Product[] productArray = gson.fromJson(reader, Product[].class);
            List<Product> products = Arrays.asList(productArray);
            reader.close();
            inputStream.close();

            WriteBatch batch = db.batch();
            
            // Assign sequential IDs to products if they don't have them
            for (int i = 0; i < products.size(); i++) {
                Product product = products.get(i);
                if (product.getId() <= 0) {
                    product.setId(i + 1); // Set sequential ID starting from 1
                }
                
                // Use auto-generated document ID instead of product ID
                // This ensures each document is unique
                batch.set(db.collection("products").document(), product);
            }

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "All " + products.size() + " products added successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error adding products: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });

        } catch (IOException e) {
            Toast.makeText(this, "Error reading JSON file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}