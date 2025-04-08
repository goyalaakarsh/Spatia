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
//import com.example.spatia.ar.ArDemoUtils;
import com.example.spatia.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductsActivity extends BaseActivity {

    private static final String TAG = "ProductsActivity";
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView noProductsText;
    private boolean isDebugMode = true; // Set to true to enable 3D model testing
    private String filterType;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.products);

        db = FirebaseFirestore.getInstance();
        setupNavigation();

        recyclerView = findViewById(R.id.productsRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Get filter parameters from intent
        Intent intent = getIntent();
        filterType = intent.getStringExtra("filter_type");
        category = intent.getStringExtra("category");
        String title = intent.getStringExtra("title");
        
        if (title != null && !title.isEmpty()) {
            setTitle(title);
        }

        productList = new ArrayList<>();
        adapter = new ProductAdapter(this, productList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // Load products based on filter type
        if (filterType != null) {
            switch (filterType) {
                case "featured":
                    loadFeaturedProducts();
                    break;
                case "popular":
                    loadPopularProducts();
                    break;
                case "category":
                    if (category != null) {
                        loadProductsByCategory(category);
                    } else {
                        loadProducts();
                    }
                    break;
                case "categories":
                    // This would show all categories, but we'll just show all products
                    loadProducts();
                    break;
                default:
                    loadProducts();
                    break;
            }
        } else {
            loadProducts();
        }
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

    private void loadFeaturedProducts() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("products")
                .whereEqualTo("featured", true)
                .get()
                .addOnCompleteListener(task -> processProductsResult(task));
    }
    
    private void loadPopularProducts() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("products")
                .orderBy("viewCount", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        processProductsResult(task);
                    } else {
                        // If no view count field exists or no results, just load all products
                        loadProducts();
                    }
                });
    }
    
    private void loadProductsByCategory(String category) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("products")
                .whereEqualTo("category", category)
                .get()
                .addOnCompleteListener(task -> processProductsResult(task));
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> processProductsResult(task));
    }
    
    private void processProductsResult(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
        progressBar.setVisibility(View.GONE);

        if (task.isSuccessful()) {
            productList.clear();
            for (QueryDocumentSnapshot document : task.getResult()) {
                Product product = document.toObject(Product.class);

                if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                    productList.add(product);
                    Log.d(TAG, "Product added: " + product.getName() + 
                              ", Has 3D model: " + product.hasArModel());
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
                
                // Debug info for AR models
                if (isDebugMode) {
                    int arModels = 0;
                    for (Product p : productList) {
                        if (p.hasArModel()) arModels++;
                    }
                    Log.d(TAG, "Products with AR models: " + arModels);
                }
            }
        } else {
            Log.w(TAG, "Error getting products.", task.getException());
            Toast.makeText(ProductsActivity.this, "Error loading products: " + 
                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    protected String getActivityTitle() {
        return filterType != null && category != null ? category : "Products";
    }
}
