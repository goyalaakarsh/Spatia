package com.example.spatia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spatia.MainActivity;
import com.example.spatia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {
    
    private FirebaseAuth mAuth;
    private static final int SPLASH_TIMEOUT = 2000; // 2 seconds
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        
        mAuth = FirebaseAuth.getInstance();
        Button btnNext = findViewById(R.id.btnNext);
        
        // Check if user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        if (currentUser != null) {
            // User is logged in, hide the button and set auto-navigation
            btnNext.setVisibility(View.GONE);
            
            // Set a timer to automatically navigate to Home
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }, SPLASH_TIMEOUT);
        } else {
            // User is not logged in, keep button visible
            btnNext.setVisibility(View.VISIBLE);
            
            // Set click listener for manual navigation
            btnNext.setOnClickListener(view -> {
                Intent intent = new Intent(WelcomeActivity.this, Onboarding1Activity.class);
                startActivity(intent);
            });
        }
    }
}
