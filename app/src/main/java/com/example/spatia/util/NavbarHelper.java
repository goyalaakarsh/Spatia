package com.example.spatia.util;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spatia.R;
import com.example.spatia.activities.ProfileActivity;

/**
 * Helper class for navbar functionality
 */
public class NavbarHelper {
    private static final String TAG = "NavbarHelper";

    /**
     * Sets up the navbar with proper click listeners
     * @param activity The current activity
     * @param title Optional title to set (null to keep existing)
     * @param showBackButton Whether to show the back button
     */
    public static void setupNavbar(final Activity activity, String title, boolean showBackButton) {
        if (activity == null) {
            Log.e(TAG, "setupNavbar called with null activity");
            return;
        }
        
        try {
            Log.d(TAG, "Setting up navbar for: " + activity.getClass().getSimpleName());
            
            // Find views (with null checks for each)
            ImageButton btnBack = activity.findViewById(R.id.btn_back);
            ImageButton btnCart = activity.findViewById(R.id.nav_btn_cart);
            ImageButton btnProfile = activity.findViewById(R.id.nav_btn_profile);
            TextView tvTitle = activity.findViewById(R.id.tv_title);
            
            // Set title if provided and if TextView exists
            if (title != null && tvTitle != null) {
                tvTitle.setText(title);
                Log.d(TAG, "Set navbar title to: " + title);
            }
            
            // Configure back button if it exists
            if (btnBack != null) {
                btnBack.setVisibility(showBackButton ? View.VISIBLE : View.INVISIBLE);
                Log.d(TAG, "Back button visibility set to: " + (showBackButton ? "VISIBLE" : "INVISIBLE"));
                
                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Back button clicked");
                        activity.onBackPressed();
                    }
                });
            } else {
                Log.w(TAG, "Back button view not found");
            }
            
            // Configure cart button if it exists
            if (btnCart != null) {
                btnCart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Cart button clicked");
                        navigateToActivity(activity, "com.example.spatia.activities.CartActivity", "Cart");
                    }
                });
            } else {
                Log.w(TAG, "Cart button view not found");
            }
            
            // Configure profile button if it exists but only if we're not already on profile
            if (btnProfile != null) {
                // Hide profile button if we're already on ProfileActivity
                if (activity instanceof ProfileActivity) {
                    btnProfile.setVisibility(View.INVISIBLE);
                } else {
                    btnProfile.setVisibility(View.VISIBLE);
                    btnProfile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "Profile button clicked");
                            Intent intent = new Intent(activity, ProfileActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            activity.startActivity(intent);
                        }
                    });
                }
            } else {
                Log.w(TAG, "Profile button view not found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up navbar", e);
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
    
    /**
     * Simplified setup that shows the back button
     * @param activity The current activity
     * @param title Title to display in navbar
     */
    public static void setupNavbarWithBack(Activity activity, String title) {
        setupNavbar(activity, title, true);
    }
    
    /**
     * Simplified setup that hides the back button (for home screen)
     * @param activity The current activity
     * @param title Title to display in navbar
     */
    public static void setupNavbarNoBack(Activity activity, String title) {
        setupNavbar(activity, title, false);
    }
}
