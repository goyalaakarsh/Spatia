package com.example.spatia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spatia.MainActivity;
import com.example.spatia.R;
import com.google.firebase.auth.FirebaseAuth;

public class TempNavigationActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temp_navigation);
        
        Button btnWelcome = findViewById(R.id.btnWelcome);
        btnWelcome.setOnClickListener(view -> {
            Intent intent = new Intent(TempNavigationActivity.this, WelcomeActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            Intent intent = new Intent(TempNavigationActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Cart button
        findViewById(R.id.btnCart).setOnClickListener(v -> {
            Intent intent = new Intent(TempNavigationActivity.this, CartActivity.class);
            startActivity(intent);
        });

        // Products button
        findViewById(R.id.btnProducts).setOnClickListener(v -> {
            Intent intent = new Intent(TempNavigationActivity.this, ProductsActivity.class);
            startActivity(intent);
        });

        // Home button (stays in MainActivity)
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(TempNavigationActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        // Auth button
        findViewById(R.id.btnAuth).setOnClickListener(v -> {
            // Sign out if user is logged in
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(TempNavigationActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(TempNavigationActivity.this, AuthActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Toast.makeText(TempNavigationActivity.this, "Search feature coming soon", Toast.LENGTH_SHORT).show();
        });

        // Orders button
        findViewById(R.id.btnOrders).setOnClickListener(v -> {
            Toast.makeText(TempNavigationActivity.this, "Orders feature coming soon", Toast.LENGTH_SHORT).show();
        });

        // Settings button
        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            Toast.makeText(TempNavigationActivity.this, "Settings feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }
}
