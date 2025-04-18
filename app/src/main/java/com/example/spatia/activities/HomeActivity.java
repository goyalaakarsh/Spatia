package com.example.spatia.activities;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spatia.R;
import com.example.spatia.adapters.CategoryAdapter;
import com.example.spatia.adapters.HomeAdapter;
import com.example.spatia.model.Category;
import com.example.spatia.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class HomeActivity extends BaseActivity {

    private static final String TAG = "HomeActivity";
    private RecyclerView recyclerViewPopular, recyclerViewCategories;
    private HomeAdapter popularAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Product> popularProducts;
    private List<Category> categories;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView popularSeeAll, categoriesSeeAll, searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        db = FirebaseFirestore.getInstance();

        // Connect the search button to the SearchActivity
        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Initialize views
        recyclerViewPopular = findViewById(R.id.recyclerViewPopular);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        progressBar = findViewById(R.id.progressBar);
        popularSeeAll = findViewById(R.id.popularSeeAllButton);
        categoriesSeeAll = findViewById(R.id.categoriesSeeAllButton);
        
        setupNavigation();

        // Initialize lists
        popularProducts = new ArrayList<>();
        categories = new ArrayList<>();

        // Initialize adapters
        popularAdapter = new HomeAdapter(this, popularProducts, this::navigateToProductDetail);
        categoryAdapter = new CategoryAdapter(this, categories, this::navigateToProductsByCategory);

        // Setup RecyclerViews
        recyclerViewPopular.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewPopular.setAdapter(popularAdapter);
        
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewCategories.setAdapter(categoryAdapter);

        // Set up click listeners for "See All" buttons
        setupSeeAllButtons();

        // Load data
        loadCategories();
        loadPopularProducts();
    }
    
    private void setupSeeAllButtons() {
        popularSeeAll.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProductsActivity.class);
            intent.putExtra("title", "Popular Products");
            intent.putExtra("filter_type", "popular");
            startActivity(intent);
        });
        
        categoriesSeeAll.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProductsActivity.class);
            intent.putExtra("title", "All Categories");
            intent.putExtra("filter_type", "categories");
            startActivity(intent);
        });
    }
    
    private void navigateToProductDetail(Product product) {
        Intent intent = new Intent(HomeActivity.this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }
    
    private void navigateToProductsByCategory(Category category) {
        Intent intent = new Intent(HomeActivity.this, ProductsActivity.class);
        intent.putExtra("title", category.getName());
        intent.putExtra("filter_type", "category");
        intent.putExtra("category", category.getName());
        startActivity(intent);
    }

    private void loadCategories() {
        showProgress(true);
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, String> categoryImages = new HashMap<>();
                        
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            if (product.getCategory() != null && !product.getCategory().isEmpty()) {
                                String categoryName = product.getCategory();
                                if (!categoryImages.containsKey(categoryName)) {
                                    categoryImages.put(categoryName, product.getImageUrl());
                                }
                            }
                        }
                        
                        categories.clear();
                        
                        // Add each unique category with the first image we find for that category
                        for (Map.Entry<String, String> entry : categoryImages.entrySet()) {
                            Category category = new Category(entry.getKey(), entry.getValue());
                            categories.add(category);
                        }
                        
                        categoryAdapter.notifyDataSetChanged();
                        showProgress(false);
                        
                    } else {
                        Log.w(TAG, "Error getting categories.", task.getException());
                        showProgress(false);
                        Toast.makeText(HomeActivity.this, 
                                "Error loading categories: " + task.getException().getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPopularProducts() {
        showProgress(true);
        db.collection("products")
                .orderBy("viewCount", Query.Direction.DESCENDING)
                .limit(8) // Increased limit since we removed featured section
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        popularProducts.clear();
                        
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            
                            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                                popularProducts.add(product);
                                Log.d(TAG, "Popular product added: " + product.getName());
                            }
                        }
                        
                        popularAdapter.notifyDataSetChanged();
                        showProgress(false);
                        
                        // If no products have view count, try loading some random products
                        if (popularProducts.isEmpty()) {
                            loadRandomPopularProducts();
                        }
                        
                    } else {
                        Log.w(TAG, "Error getting popular products.", task.getException());
                        showProgress(false);
                        // Fallback to random products
                        loadRandomPopularProducts();
                    }
                });
    }
    
    private void loadRandomPopularProducts() {
        db.collection("products")
                .limit(8) // Increased limit since we removed featured section
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        popularProducts.clear();
                        
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            
                            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                                popularProducts.add(product);
                            }
                        }
                        
                        popularAdapter.notifyDataSetChanged();
                        
                    } else {
                        Log.w(TAG, "Error getting random popular products.", task.getException());
                    }
                });
    }
    
    private void showProgress(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected String getActivityTitle() {
        return "Spatia";
    }
    
    @Override
    protected boolean shouldShowBackButton() {
        // Hide back button on the home page
        return false;
    }
}
