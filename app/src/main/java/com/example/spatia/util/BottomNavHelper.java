package com.example.spatia.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
    public static void setupBottomNavigation(Activity activity, BottomNavigationView navigationView) {
        try {
            // Set the appropriate item as selected based on the activity
            if (activity.getClass().getSimpleName().equals("HomeActivity")) {
                navigationView.setSelectedItemId(R.id.navigation_home);
            } else if (activity.getClass().getSimpleName().equals("CartActivity")) {
                navigationView.setSelectedItemId(R.id.navigation_cart);
            } else if (activity.getClass().getSimpleName().equals("OrdersActivity")) {
                navigationView.setSelectedItemId(R.id.navigation_orders);
            } else if (activity instanceof ProfileActivity) {
                navigationView.setSelectedItemId(R.id.navigation_profile);
            }

            // Set up navigation item click listener
            navigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                
                // Don't do anything if we're already on this page
                if (itemId == navigationView.getSelectedItemId()) {
                    return true;
                }
                
                Context context = activity.getApplicationContext();
                
                // Navigate to the appropriate activity
                Intent intent = null;
                if (itemId == R.id.navigation_home) {
                    try {
                        // Use reflection to avoid direct dependency in case the class is not created yet
                        Class<?> homeActivityClass = Class.forName("com.example.spatia.activities.HomeActivity");
                        intent = new Intent(activity, homeActivityClass);
                    } catch (ClassNotFoundException e) {
                        Toast.makeText(context, "Home screen coming soon", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "HomeActivity not found", e);
                    }
                } else if (itemId == R.id.navigation_cart) {
                    try {
                        Class<?> cartActivityClass = Class.forName("com.example.spatia.activities.CartActivity");
                        intent = new Intent(activity, cartActivityClass);
                    } catch (ClassNotFoundException e) {
                        Toast.makeText(context, "Cart feature coming soon", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "CartActivity not found", e);
                    }
                } else if (itemId == R.id.navigation_orders) {
                    try {
                        Class<?> ordersActivityClass = Class.forName("com.example.spatia.activities.OrdersActivity");
                        intent = new Intent(activity, ordersActivityClass);
                    } catch (ClassNotFoundException e) {
                        Toast.makeText(context, "Orders feature coming soon", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "OrdersActivity not found", e);
                    }
                } else if (itemId == R.id.navigation_profile) {
                    intent = new Intent(activity, ProfileActivity.class);
                }
                
                if (intent != null) {
                    // Clear back stack when navigating between bottom nav items
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(intent);
                    return true;
                }
                
                return false;
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up bottom navigation", e);
        }
    }
}
