package com.example.spatia.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.spatia.database.Database;
import com.example.spatia.model.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing product data access
 */
public class ProductRepository {
    private static final String TAG = "ProductRepository";
    private static final String PREF_NAME = "product_prefs";
    private static final String KEY_ALL_PRODUCTS = "all_products";
    private static final String KEY_PRODUCT_PREFIX = "product_";
    
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    private final Database database;
    
    public ProductRepository(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.database = Database.getInstance(context);
    }
    
    /**
     * Save all products to local storage
     */
    public void saveAllProducts(List<Product> products) {
        try {
            // Save to shared preferences
            String json = gson.toJson(products);
            sharedPreferences.edit().putString(KEY_ALL_PRODUCTS, json).apply();
            
            // Save to in-memory database
            for (Product product : products) {
                database.addProduct(product);
            }
            
            Log.d(TAG, "Saved " + products.size() + " products to local storage");
        } catch (Exception e) {
            Log.e(TAG, "Error saving products", e);
        }
    }
    
    /**
     * Get all products from local storage
     */
    public List<Product> getAllProducts() {
        // Get from SQLite database
        List<Product> dbProducts = database.getAllProducts();
        if (!dbProducts.isEmpty()) {
            return dbProducts;
        }
        
        // Fall back to shared preferences as backup
        try {
            String json = sharedPreferences.getString(KEY_ALL_PRODUCTS, null);
            if (json != null) {
                Type type = new TypeToken<List<Product>>(){}.getType();
                return gson.fromJson(json, type);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting products", e);
        }
        return new ArrayList<>();
    }
    
    /**
     * Save a single product to local storage
     */
    public void saveProduct(Product product) {
        try {
            String json = gson.toJson(product);
            sharedPreferences.edit().putString(KEY_PRODUCT_PREFIX + product.getId(), json).apply();
            Log.d(TAG, "Saved product with ID: " + product.getId());
        } catch (Exception e) {
            Log.e(TAG, "Error saving product", e);
        }
    }
    
    /**
     * Get a product by ID from local storage
     */
    public Product getProductById(int productId) {
        // Try to get from SQLite database first
        Product product = database.getProductById(productId);
        if (product != null) {
            return product;
        }
        
        // Fall back to shared preferences as backup
        try {
            String json = sharedPreferences.getString(KEY_PRODUCT_PREFIX + productId, null);
            if (json != null) {
                return gson.fromJson(json, Product.class);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting product with ID: " + productId, e);
        }
        return null;
    }
}
