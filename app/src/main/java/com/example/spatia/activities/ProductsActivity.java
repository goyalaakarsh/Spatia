package com.example.spatia.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spatia.R;
import com.example.spatia.adapters.ProductAdapter;
import com.example.spatia.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductsActivity extends AppCompatActivity {

    private static final String TAG = "ProductsActivity";
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView noProductsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.products);

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.productsRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        productList = new ArrayList<>();
        adapter = new ProductAdapter(this, productList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        loadProducts();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.products_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.menu_cart) {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("products")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        progressBar.setVisibility(View.GONE);

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

                            adapter.notifyDataSetChanged();

                            if (productList.isEmpty()) {
                                Toast.makeText(ProductsActivity.this, "No products available", Toast.LENGTH_SHORT)
                                        .show();
                            } else {
                                Log.d(TAG, "Total products loaded: " + productList.size());
                            }
                        } else {
                            Log.w(TAG, "Error getting products.", task.getException());
                            Toast.makeText(ProductsActivity.this, "Error loading products: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
