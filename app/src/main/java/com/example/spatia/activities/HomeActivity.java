package com.example.spatia.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spatia.R;
import com.example.spatia.adapters.CategoryAdapter;
import com.example.spatia.adapters.HomeAdapter;
import com.example.spatia.model.Category;
import com.example.spatia.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

public class HomeActivity extends BaseActivity {

    private static final String TAG = "HomeActivity";
    private RecyclerView recyclerViewFeatured, recyclerViewPopular, recyclerViewCategories;
    private HomeAdapter featuredAdapter, popularAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Product> featuredProducts, popularProducts;
    private List<Category> categories;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView featuredSeeAll, popularSeeAll, categoriesSeeAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        db = FirebaseFirestore.getInstance();

        //Connecting the search page to the home page here
        EditText searchBar = findViewById(R.id.searchBar);
        searchBar.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Initialize views
        recyclerViewFeatured = findViewById(R.id.recyclerViewFeatured);
        recyclerViewPopular = findViewById(R.id.recyclerViewPopular);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        progressBar = findViewById(R.id.progressBar);
        featuredSeeAll = findViewById(R.id.featuredSeeAllButton);
        popularSeeAll = findViewById(R.id.popularSeeAllButton);
        categoriesSeeAll = findViewById(R.id.categoriesSeeAllButton);
        
        setupNavigation();

        // Initialize lists
        featuredProducts = new ArrayList<>();
        popularProducts = new ArrayList<>();
        categories = new ArrayList<>();

        // Initialize adapters
        featuredAdapter = new HomeAdapter(this, featuredProducts, product -> {
            navigateToProductDetail(product);
        });
        
        popularAdapter = new HomeAdapter(this, popularProducts, product -> {
            navigateToProductDetail(product);
        });
        
        categoryAdapter = new CategoryAdapter(this, categories, category -> {
            navigateToProductsByCategory(category);
        });

        // Setup RecyclerViews
        recyclerViewFeatured.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewFeatured.setAdapter(featuredAdapter);
        
        recyclerViewPopular.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewPopular.setAdapter(popularAdapter);
        
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewCategories.setAdapter(categoryAdapter);

        // Set up click listeners for "See All" buttons
        setupSeeAllButtons();

        // Load data
        loadCategories();
        loadFeaturedProducts();
        loadPopularProducts();
    }
    
    private void setupSeeAllButtons() {
        featuredSeeAll.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProductsActivity.class);
            intent.putExtra("title", "Featured Products");
            intent.putExtra("filter_type", "featured");
            startActivity(intent);
        });
        
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
    
    private String getCategoryImage(String categoryName) {
        // This is a simplistic approach. In a real app, you'd store category images in the database
        // or have a more sophisticated mapping
        
        switch (categoryName.toLowerCase()) {
            case "sofa":
                return "https://firebasestorage.googleapis.com/v0/b/spatia-8f41a.appspot.com/o/sofa_category.jpg?alt=media";
            case "chair":
                return "https://firebasestorage.googleapis.com/v0/b/spatia-8f41a.appspot.com/o/chair_category.jpg?alt=media";
            case "table":
                return "https://firebasestorage.googleapis.com/v0/b/spatia-8f41a.appspot.com/o/table_category.jpg?alt=media";
            case "bed":
                return "https://firebasestorage.googleapis.com/v0/b/spatia-8f41a.appspot.com/o/bed_category.jpg?alt=media";
            case "storage":
                return "https://firebasestorage.googleapis.com/v0/b/spatia-8f41a.appspot.com/o/storage_category.jpg?alt=media";
            case "decor":
                return "https://firebasestorage.googleapis.com/v0/b/spatia-8f41a.appspot.com/o/decor_category.jpg?alt=media";
            default:
                return "https://firebasestorage.googleapis.com/v0/b/spatia-8f41a.appspot.com/o/furniture_default.jpg?alt=media";
        }
    }

    private void loadFeaturedProducts() {
        showProgress(true);
        db.collection("products")
                .whereEqualTo("featured", true)
                .limit(6)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        featuredProducts.clear();
                        
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            
                            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                                featuredProducts.add(product);
                                Log.d(TAG, "Featured product added: " + product.getName());
                            }
                        }
                        
                        Log.d(TAG, "Featured products found: " + featuredProducts.size());
                        
                        featuredAdapter.notifyDataSetChanged();
                        showProgress(false);
                        
                        // If no featured products were found, load random products as featured
                        if (featuredProducts.isEmpty()) {
                            Log.w(TAG, "No featured products found, loading random products");
                            loadRandomFeaturedProducts();
                        }
                        
                    } else {
                        Log.w(TAG, "Error getting featured products.", task.getException());
                        showProgress(false);
                        // Fallback to random products
                        loadRandomFeaturedProducts();
                    }
                });
    }

    private void loadRandomFeaturedProducts() {
        db.collection("products")
                .limit(6)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        featuredProducts.clear();
                        
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            
                            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                                featuredProducts.add(product);
                                Log.d(TAG, "Random featured product added: " + product.getName());
                            }
                        }
                        
                        featuredAdapter.notifyDataSetChanged();
                        
                    } else {
                        Log.w(TAG, "Error getting random featured products.", task.getException());
                    }
                });
    }

    private void loadPopularProducts() {
        // For demonstration, we'll use the view count or sold count to determine popularity
        // You might need to adjust this based on your database structure
        showProgress(true);
        db.collection("products")
                .orderBy("viewCount", Query.Direction.DESCENDING)  // Assuming you have a viewCount field
                .limit(6)
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
                .limit(6)
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
        return "Home";
    }
}
