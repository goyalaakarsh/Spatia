package com.example.spatia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spatia.R;

public class Onboarding1Activity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding_1);
        
        Button btnNext = findViewById(R.id.btnNext); // Update this ID to match your actual button ID
        btnNext.setOnClickListener(view -> {
            Intent intent = new Intent(Onboarding1Activity.this, Onboarding2Activity.class);
            startActivity(intent);
        });
    }
}
