package com.example.spatia.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.spatia.R;
import com.example.spatia.adapters.HomeAdapter;
import com.example.spatia.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private RecyclerView recyclerViewFeatured, recyclerViewPopular;
    private HomeAdapter featuredAdapter, popularAdapter, productAdapter;
    private List<Product> featuredProducts, popularProducts, productList;
    private FirebaseFirestore db;
//    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        db = FirebaseFirestore.getInstance();

        recyclerViewFeatured = findViewById(R.id.recyclerViewFeatured);
        recyclerViewPopular = findViewById(R.id.recyclerViewPopular);
//        progressBar = findViewById(R.id.progressBar);

        productList = new ArrayList<>();
        featuredProducts = new ArrayList<>();
        popularProducts = new ArrayList<>();
        featuredAdapter = new HomeAdapter(this, featuredProducts);
        popularAdapter = new HomeAdapter(this, popularProducts);
        productAdapter = new HomeAdapter(this, productList);

        recyclerViewFeatured.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewPopular.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        recyclerViewFeatured.setAdapter(productAdapter);
//        recyclerViewFeatured.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewPopular.setAdapter(productAdapter);
//        recyclerViewPopular.setLayoutManager(new GridLayoutManager(this, 2));

        loadProducts();
    }

    private void loadProducts() {
//        progressBar.setVisibility(View.VISIBLE);

        db.collection("products")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            productList.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Product product = document.toObject(Product.class);

                                if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                                    productList.add(product);
                                    Log.d(TAG, "Product added: " + product.getName() + ", Image: "
                                            + product.getImageUrl());
                                } else {
                                    Log.w(TAG, "Product skipped (no image): " + product.getName());
                                }
                            }

                            productAdapter.notifyDataSetChanged();

                            if (productList.isEmpty()) {
                                Toast.makeText(HomeActivity.this, "No products available", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w(TAG, "Error getting products.", task.getException());
                            Toast.makeText(HomeActivity.this, "Error loading products: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
