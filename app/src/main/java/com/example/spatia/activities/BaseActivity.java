package com.example.spatia.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spatia.R;
import com.example.spatia.util.BottomNavHelper;
import com.example.spatia.util.NavbarHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Base activity that handles common UI elements like top navbar and bottom navigation
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    /**
     * Setup both navigation bars after content view is set
     */
    protected void setupNavigation() {
        // Setup the top navbar
        setupTopNavbar();
        
        // Setup the bottom navigation
        setupBottomNavigation();
    }
    
    /**
     * Setup the top navbar with dynamic title and optional back button
     */
    protected void setupTopNavbar() {
        String title = getActivityTitle();
        boolean showBackButton = shouldShowBackButton();
        NavbarHelper.setupNavbar(this, title, showBackButton);
    }
    
    /**
     * Setup the bottom navigation 
     */
    protected void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            BottomNavHelper.setupBottomNavigation(this, bottomNavigationView);
        }
    }
    
    /**
     * Should be implemented by child activities to provide the title for the top navbar
     * @return The title for the current activity
     */
    protected abstract String getActivityTitle();
    
    /**
     * Can be overridden by child activities to determine if back button should be shown
     * Default is true (show back button) except for home screen
     * @return true if back button should be shown, false otherwise
     */
    protected boolean shouldShowBackButton() {
        return true;
    }
    
    /**
     * Updates the title in the navbar programmatically
     * @param title New title to set
     */
    protected void updateNavbarTitle(String title) {
        TextView tvTitle = findViewById(R.id.tv_title);
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }
}
