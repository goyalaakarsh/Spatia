package com.example.spatia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spatia.activities.AuthActivity;
import com.example.spatia.activities.ProductsActivity;
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

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish(); 
        } else {
            Intent intent = new Intent(this, ProductsActivity.class);
            startActivity(intent);
            finish(); 
            Toast.makeText(this, "Welcome back, " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
        }
        // insertProducts();
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

            for (Product product : products) {
                batch.set(db.collection("products").document(String.valueOf(product.getId())), product);
            }

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "All products added successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error adding products: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });

        } catch (IOException e) {
            Toast.makeText(this, "Error reading JSON file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}