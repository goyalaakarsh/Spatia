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
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

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

            for (int i = 0; i < products.size(); i++) {
                Product product = products.get(i);
                if (product.getId() <= 0) {
                    product.setId(i + 1);
                }

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