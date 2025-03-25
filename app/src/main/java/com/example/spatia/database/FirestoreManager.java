package com.example.spatia.database;

import android.content.Context;
import android.util.Log;

import com.example.spatia.model.Product;
import com.example.spatia.util.JsonDataLoader;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Firestore database manager for the application
 */
public class FirestoreManager {
    private static final String TAG = "FirestoreManager";
    private static final String PRODUCTS_COLLECTION = "products";
    
    private static FirestoreManager instance;
    private final Context context;
    private final FirebaseFirestore db;
    
    private FirestoreManager(Context context) {
        this.context = context.getApplicationContext();
        this.db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Get the singleton instance
     */
    public static synchronized FirestoreManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirestoreManager(context);
        }
        return instance;
    }
    
    /**
     * Load products from JSON and save to Firestore
     */
    public void loadProductsFromJson() {
        Log.d(TAG, "Starting to load products from JSON to Firestore");
        
        // Check if products already exist in Firestore
        getProductsCollection().get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot != null && !snapshot.isEmpty()) {
                        Log.d(TAG, "Products already exist in Firestore. Count: " + snapshot.size());
                    } else {
                        Log.d(TAG, "No products found in Firestore. Loading from JSON...");
                        addProductsFromJson();
                    }
                } else {
                    Log.e(TAG, "Error checking for existing products", task.getException());
                }
            });
    }
    
    /**
     * Read products from JSON file and add to Firestore
     */
    private void addProductsFromJson() {
        List<Product> products = JsonDataLoader.loadProductsFromJson(context);
        
        if (products.isEmpty()) {
            Log.e(TAG, "No products loaded from JSON");
            return;
        }
        
        Log.d(TAG, "Loaded " + products.size() + " products from JSON, adding to Firestore");
        
        // Create a batch for efficient writing
        for (Product product : products) {
            addProduct(product);
        }
    }
    
    /**
     * Add a single product to Firestore
     */
    public void addProduct(Product product) {
        getProductsCollection().document(String.valueOf(product.getId()))
            .set(product)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "Product successfully added: " + product.getName()))
            .addOnFailureListener(e -> Log.e(TAG, "Error adding product: " + product.getName(), e));
    }
    
    /**
     * Get all products from Firestore
     */
    public void getAllProducts(final ProductsCallback callback) {
        getProductsCollection().get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Product> products = new ArrayList<>();
                    
                    for (DocumentSnapshot document : task.getResult()) {
                        Product product = document.toObject(Product.class);
                        if (product != null) {
                            products.add(product);
                        }
                    }
                    
                    Log.d(TAG, "Retrieved " + products.size() + " products from Firestore");
                    callback.onProductsLoaded(products);
                } else {
                    Log.e(TAG, "Error getting products from Firestore", task.getException());
                    callback.onProductsLoaded(new ArrayList<>());
                }
            });
    }
    
    /**
     * Get the products collection reference
     */
    private CollectionReference getProductsCollection() {
        return db.collection(PRODUCTS_COLLECTION);
    }
    
    /**
     * Callback for getting products
     */
    public interface ProductsCallback {
        void onProductsLoaded(List<Product> products);
    }
}
