package com.example.spatia.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.spatia.R;
import com.example.spatia.fragments.HomeFragment;
import com.example.spatia.fragments.ProfileFragment;
import com.example.spatia.fragments.CartFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainContainerActivity extends AppCompatActivity {
    private static final String TAG = "MainContainerActivity";
    private TextView tvTitle;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_container);
        
        try {
            // Set up the title in navbar
            tvTitle = findViewById(R.id.tv_title);
            
            // Initialize the bottom navigation view
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            
            // Set up bottom navigation click listener
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                
                // Navigate based on selected item
                if (itemId == R.id.navigation_home) {
                    loadFragment(new HomeFragment(), "Home");
                    return true;
                } else if (itemId == R.id.navigation_cart) {
                    loadFragment(new CartFragment(), "My Shopping Bag");
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    loadFragment(new ProfileFragment(), "Profile");
                    return true;
                }
                
                return false;
            });
            
            // Check if we're being restored from a previous state
            if (savedInstanceState == null) {
                // Default fragment
                loadFragment(new HomeFragment(), "Home");
            }
            
            // Setup top navbar actions
            setupTopNavbar();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
        }
    }
    
    private void loadFragment(Fragment fragment, String title) {
        try {
            // Update the page title
            if (tvTitle != null) {
                tvTitle.setText(title);
            }
            
            // Begin the fragment transition
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error loading fragment: " + e.getMessage(), e);
        }
    }
    
    private void setupTopNavbar() {
        try {
            // Setup cart button
            View cartBtn = findViewById(R.id.nav_btn_cart);
            if (cartBtn != null) {
                cartBtn.setOnClickListener(v -> {
                    BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
                    bottomNav.setSelectedItemId(R.id.navigation_cart);
                });
            }
            
            // Setup profile button
            View profileBtn = findViewById(R.id.nav_btn_profile);
            if (profileBtn != null) {
                profileBtn.setOnClickListener(v -> {
                    BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
                    bottomNav.setSelectedItemId(R.id.navigation_profile);
                });
            }
            
            // Setup back button - Hide it for main container
            View backBtn = findViewById(R.id.btn_back);
            if (backBtn != null) {
                backBtn.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up navbar: " + e.getMessage(), e);
        }
    }
}
