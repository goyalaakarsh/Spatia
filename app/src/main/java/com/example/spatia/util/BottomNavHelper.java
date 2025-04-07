package com.example.spatia.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.spatia.R;
import com.example.spatia.activities.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavHelper {
    private static final String TAG = "BottomNavHelper";

    /**
     * Sets up the bottom navigation with proper selection and click listeners
     * @param activity The current activity
     * @param navigationView The bottom navigation view
     */
    public static void setupBottomNavigation(final Activity activity, BottomNavigationView navigationView) {
        if (activity == null || navigationView == null) {
            Log.e(TAG, "setupBottomNavigation called with null parameters");
            return;
        }
        
        try {
            // Set the appropriate item as selected based on the activity class
            String activityName = activity.getClass().getSimpleName();
            Log.d(TAG, "Setting up bottom navigation for: " + activityName);
            
            if (activityName.equals("HomeActivity")) {
                navigationView.setSelectedItemId(R.id.navigation_home);
            } else if (activityName.equals("CartActivity")) {
                navigationView.setSelectedItemId(R.id.navigation_cart);
            } else if (activityName.equals("OrdersActivity")) {
                navigationView.setSelectedItemId(R.id.navigation_orders);
            } else if (activity instanceof ProfileActivity) {
                navigationView.setSelectedItemId(R.id.navigation_profile);
            }

            // Set up navigation item click listener
            navigationView.setOnItemSelectedListener(item -> {
                final int itemId = item.getItemId();
                
                // Don't do anything if we're already on this page
                if (itemId == navigationView.getSelectedItemId()) {
                    Log.d(TAG, "Already on selected page: " + itemId);
                    return true;
                }
                
                Log.d(TAG, "Navigation item clicked: " + itemId);
                
                // Run navigation in a separate thread to avoid UI blocking
                new Handler(Looper.getMainLooper()).post(() -> {
                    // Navigate to the appropriate activity
                    if (itemId == R.id.navigation_home) {
                        navigateToActivity(activity, "com.example.spatia.activities.HomeActivity", "Home");
                    } else if (itemId == R.id.navigation_cart) {
                        navigateToActivity(activity, "com.example.spatia.activities.CartActivity", "Cart");
                    } else if (itemId == R.id.navigation_orders) {
                        navigateToActivity(activity, "com.example.spatia.activities.OrdersActivity", "Orders");
                    } else if (itemId == R.id.navigation_profile) {
                        // We can directly reference ProfileActivity since we already import it
                        Intent intent = new Intent(activity, ProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        activity.startActivity(intent);
                        activity.overridePendingTransition(0, 0);
                    }
                });
                
                return true;
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up bottom navigation", e);
        }
    }
    
    /**
     * Helper method to navigate to an activity using reflection
     */
    private static void navigateToActivity(Activity activity, String className, String featureName) {
        try {
            Class<?> targetClass = Class.forName(className);
            Intent intent = new Intent(activity, targetClass);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);
        } catch (ClassNotFoundException e) {
            Toast.makeText(activity, featureName + " feature coming soon", Toast.LENGTH_SHORT).show();
            Log.d(TAG, className + " not found", e);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to " + className, e);
        }
    }
}
