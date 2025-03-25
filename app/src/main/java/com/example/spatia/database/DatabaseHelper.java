package com.example.spatia.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SQLite database helper for creating and managing database
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    
    // Database Info
    private static final String DATABASE_NAME = "SpatiaDatabase.db";
    private static final int DATABASE_VERSION = 1;
    
    private static DatabaseHelper instance;
    
    // Singleton pattern
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables");
        
        // Create Products table
        db.execSQL(DatabaseContract.ProductEntry.SQL_CREATE_TABLE);
        
        // Create Users table
        db.execSQL(DatabaseContract.UserEntry.SQL_CREATE_TABLE);
        
        // Create Cart table
        db.execSQL(DatabaseContract.CartEntry.SQL_CREATE_TABLE);
        
        Log.d(TAG, "Database tables created successfully");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        
        // Drop tables and recreate
        db.execSQL(DatabaseContract.ProductEntry.SQL_DROP_TABLE);
        db.execSQL(DatabaseContract.UserEntry.SQL_DROP_TABLE);
        db.execSQL(DatabaseContract.CartEntry.SQL_DROP_TABLE);
        
        onCreate(db);
    }
    
    /**
     * Check if the database exists and can be read
     */
    public boolean checkDatabaseHealth() {
        SQLiteDatabase db = null;
        try {
            db = getReadableDatabase();
            return db != null && db.isOpen();
        } catch (Exception e) {
            Log.e(TAG, "Database health check failed", e);
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
}
