package com.example.spatia.database;

import android.content.Context;
import android.util.Log;

import com.example.spatia.model.Product;
import com.example.spatia.util.JsonDataLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Firebase database manager class for the application
 */
public class FirebaseDatabaseManager {
    private static final String TAG = "FirebaseDatabaseMgr";
    private static FirebaseDatabaseManager instance;
    
    private final Context context;
    private final DatabaseReference dbRef;
    
    private FirebaseDatabaseManager(Context context) {
        this.context = context.getApplicationContext();
        this.dbRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference();
    }
    
    /**
     * Get the singleton instance
     */
    public static synchronized FirebaseDatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseDatabaseManager(context);
        }
        return instance;
    }
    
    /**
     * Initialize the database with product data from JSON
     */
    public void initializeProducts() {
        Log.d(TAG, "Starting Firebase database initialization");
        
        // Check if products already exist in Firebase
        checkProductsExist(exists -> {
            if (!exists) {
                Log.d(TAG, "Products don't exist in Firebase. Loading from JSON...");
                
                // Load products from JSON file
                List<Product> products = JsonDataLoader.loadProductsFromJson(context);
                
                if (products.isEmpty()) {
                    Log.e(TAG, "Failed to load any products from JSON");
                } else {
                    // Add products to Firebase
                    for (Product product : products) {
                        addProduct(product);
                    }
                    
                    Log.d(TAG, "Successfully added " + products.size() + " products to Firebase");
                }
            } else {
                Log.d(TAG, "Products already exist in Firebase database");
            }
        });
    }
    
    /**
     * Add a product to the database
     */
    public void addProduct(Product product) {
        dbRef.child("products").child(String.valueOf(product.getId())).setValue(product)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Product added: " + product.getName()))
                .addOnFailureListener(e -> Log.e(TAG, "Error adding product: " + product.getName(), e));
    }
    
    /**
     * Get all products from the database
     */
    public void getAllProducts(final ProductsCallback callback) {
        dbRef.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Product> products = new ArrayList<>();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        products.add(product);
                    }
                }
                
                Log.d(TAG, "Retrieved " + products.size() + " products from Firebase");
                callback.onProductsLoaded(products);
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error getting products from Firebase", databaseError.toException());
                callback.onProductsLoaded(new ArrayList<>());
            }
        });
    }
    
    /**
     * Check if products already exist in the database
     */
    private void checkProductsExist(final ExistsCallback callback) {
        dbRef.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean exists = dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0;
                Log.d(TAG, "Products exist check: " + exists);
                callback.onResult(exists);
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error checking products existence", databaseError.toException());
                callback.onResult(false);
            }
        });
    }
    
    /**
     * Callback for getting products
     */
    public interface ProductsCallback {
        void onProductsLoaded(List<Product> products);
    }
    
    /**
     * Callback for checking if products exist
     */
    private interface ExistsCallback {
        void onResult(boolean exists);
    }
}
