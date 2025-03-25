package com.example.spatia.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.spatia.model.Product;
import com.example.spatia.model.User;
import com.example.spatia.util.JsonDataLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Database manager class for the application
 */
public class Database {
    private static final String TAG = "Database";
    private static Database instance;
    
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private final Context context;
    
    private Database(Context context) {
        this.context = context.getApplicationContext();
        dbHelper = DatabaseHelper.getInstance(context);
    }
    
    /**
     * Get the singleton instance
     */
    public static synchronized Database getInstance(Context context) {
        if (instance == null) {
            instance = new Database(context);
        }
        return instance;
    }
    
    /**
     * Initialize the database with product data from JSON
     */
    public void init() {
        try {
            Log.d(TAG, "Starting database initialization");
            db = dbHelper.getWritableDatabase();
            
            // Log available assets to help debug
            JsonDataLoader.logAvailableAssets(context);
            
            // Only add products if the table is empty
            List<Product> existingProducts = getAllProducts();
            Log.d(TAG, "Found " + existingProducts.size() + " existing products in database");
            
            if (existingProducts.isEmpty()) {
                Log.d(TAG, "Loading products from JSON file");
                
                // Load products from JSON
                List<Product> products = JsonDataLoader.loadProductsFromJson(context);
                
                if (products.isEmpty()) {
                    Log.e(TAG, "Failed to load any products from JSON");
                } else {
                    // Insert products into database
                    for (Product product : products) {
                        long result = addProduct(product);
                        if (result == -1) {
                            Log.e(TAG, "Failed to insert product: " + product.getName());
                        }
                    }
                    
                    Log.d(TAG, "Successfully added " + products.size() + " products to database");
                }
            }
            
            Log.d(TAG, "Database initialization completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error during database initialization", e);
        }
    }
    
    /**
     * Verify database setup is successful
     */
    public boolean verifyDatabaseSetup() {
        try {
            Log.d(TAG, "Starting database verification");
            
            boolean isHealthy = dbHelper.checkDatabaseHealth();
            Log.d(TAG, "Database health check result: " + isHealthy);
            
            if (!isHealthy) {
                Log.e(TAG, "Database health check failed");
                return false;
            }
            
            // Test inserting and querying data
            db = dbHelper.getWritableDatabase();
            Log.d(TAG, "Got writable database for verification");
            
            // Insert a test product
            Product testProduct = new Product(999, "Test Product", "For testing only", 0.99, "Test", "");
            long testProductId = addProduct(testProduct);
            Log.d(TAG, "Inserted test product with result: " + testProductId);
            
            // Verify product was inserted
            Product retrievedProduct = getProductById(999);
            boolean productSuccess = retrievedProduct != null && 
                                    retrievedProduct.getName().equals("Test Product");
            
            Log.d(TAG, "Retrieved test product: " + 
                       (retrievedProduct != null ? retrievedProduct.getName() : "null") + 
                       ", success: " + productSuccess);
            
            // Clean up test data
            int deleteResult = db.delete(
                DatabaseContract.ProductEntry.TABLE_NAME,
                DatabaseContract.ProductEntry.COLUMN_ID + " = ?",
                new String[]{"999"}
            );
            
            Log.d(TAG, "Deleted test product with result: " + deleteResult);
            
            if (!productSuccess) {
                Log.e(TAG, "Database product verification failed");
                return false;
            }
            
            Log.d(TAG, "Database setup verified successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Database verification failed with exception", e);
            return false;
        }
    }
    
    /**
     * Add a product to the database
     */
    public long addProduct(Product product) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ProductEntry.COLUMN_ID, product.getId());
        values.put(DatabaseContract.ProductEntry.COLUMN_NAME, product.getName());
        values.put(DatabaseContract.ProductEntry.COLUMN_DESCRIPTION, product.getDescription());
        values.put(DatabaseContract.ProductEntry.COLUMN_PRICE, product.getPrice());
        values.put(DatabaseContract.ProductEntry.COLUMN_CATEGORY, product.getCategory());
        values.put(DatabaseContract.ProductEntry.COLUMN_IMAGE_URL, product.getImageUrl());
        
        return db.insertWithOnConflict(
            DatabaseContract.ProductEntry.TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        );
    }
    
    /**
     * Get all products from the database
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        
        try (Cursor cursor = db.query(
                DatabaseContract.ProductEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_NAME));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_DESCRIPTION));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_PRICE));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_CATEGORY));
                    String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_IMAGE_URL));
                    
                    products.add(new Product(id, name, description, price, category, imageUrl));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting products from database", e);
        }
        
        return products;
    }
    
    /**
     * Get a product by ID
     */
    public Product getProductById(int productId) {
        try (Cursor cursor = db.query(
                DatabaseContract.ProductEntry.TABLE_NAME,
                null,
                DatabaseContract.ProductEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(productId)},
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_DESCRIPTION));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_PRICE));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_CATEGORY));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_IMAGE_URL));
                
                return new Product(productId, name, description, price, category, imageUrl);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting product with ID: " + productId, e);
        }
        
        return null;
    }
    
    /**
     * Register a new user
     */
    public User addUser(String username, String password) {
        // In a real app, you would hash the password
        String passwordHash = password; // Just demo - don't do this in production!
        
        // Check if username already exists
        try (Cursor cursor = db.query(
                DatabaseContract.UserEntry.TABLE_NAME,
                new String[]{DatabaseContract.UserEntry.COLUMN_ID},
                DatabaseContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                Log.w(TAG, "Username already exists: " + username);
                return null;
            }
        }
        
        // Insert new user
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.UserEntry.COLUMN_USERNAME, username);
        values.put(DatabaseContract.UserEntry.COLUMN_PASSWORD_HASH, passwordHash);
        
        long id = db.insert(DatabaseContract.UserEntry.TABLE_NAME, null, values);
        
        if (id != -1) {
            User user = new User(id, username, "");
            Log.d(TAG, "Added new user: " + username + " with ID: " + id);
            return user;
        } else {
            Log.e(TAG, "Failed to add user: " + username);
            return null;
        }
    }
    
    /**
     * Authenticate a user
     */
    public User getUser(String username, String password) {
        // In a real app, you would hash the password and compare hashes
        try (Cursor cursor = db.query(
                DatabaseContract.UserEntry.TABLE_NAME,
                null,
                DatabaseContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_ID));
                String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_PASSWORD_HASH));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_EMAIL));
                String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_FIRST_NAME));
                String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_LAST_NAME));
                
                // Simple password verification (not secure for production)
                if (password.equals(storedPassword)) {
                    return new User(id, username, email, firstName, lastName);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error authenticating user: " + username, e);
        }
        
        return null;
    }
    
    /**
     * Add a product to a user's cart
     */
    public boolean addToCart(long userId, long productId, int quantity) {
        try {
            // First check if product exists
            try (Cursor cursor = db.query(
                    DatabaseContract.ProductEntry.TABLE_NAME,
                    new String[]{DatabaseContract.ProductEntry.COLUMN_ID},
                    DatabaseContract.ProductEntry.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(productId)},
                    null,
                    null,
                    null
            )) {
                if (cursor == null || !cursor.moveToFirst()) {
                    Log.e(TAG, "Product not found with ID: " + productId);
                    return false;
                }
            }
            
            // Check if item already in cart
            try (Cursor cursor = db.query(
                    DatabaseContract.CartEntry.TABLE_NAME,
                    new String[]{DatabaseContract.CartEntry.COLUMN_QUANTITY},
                    DatabaseContract.CartEntry.COLUMN_USER_ID + " = ? AND " + 
                    DatabaseContract.CartEntry.COLUMN_PRODUCT_ID + " = ?",
                    new String[]{String.valueOf(userId), String.valueOf(productId)},
                    null,
                    null,
                    null
            )) {
                ContentValues values = new ContentValues();
                values.put(DatabaseContract.CartEntry.COLUMN_USER_ID, userId);
                values.put(DatabaseContract.CartEntry.COLUMN_PRODUCT_ID, productId);
                
                if (cursor != null && cursor.moveToFirst()) {
                    // Update existing cart item
                    int currentQty = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.CartEntry.COLUMN_QUANTITY));
                    values.put(DatabaseContract.CartEntry.COLUMN_QUANTITY, currentQty + quantity);
                    
                    db.update(
                        DatabaseContract.CartEntry.TABLE_NAME,
                        values,
                        DatabaseContract.CartEntry.COLUMN_USER_ID + " = ? AND " + 
                        DatabaseContract.CartEntry.COLUMN_PRODUCT_ID + " = ?",
                        new String[]{String.valueOf(userId), String.valueOf(productId)}
                    );
                } else {
                    // Add new cart item
                    values.put(DatabaseContract.CartEntry.COLUMN_QUANTITY, quantity);
                    db.insert(DatabaseContract.CartEntry.TABLE_NAME, null, values);
                }
            }
            
            Log.d(TAG, "Added product " + productId + " (qty: " + quantity + ") to user " + userId + "'s cart");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error adding to cart", e);
            return false;
        }
    }
}