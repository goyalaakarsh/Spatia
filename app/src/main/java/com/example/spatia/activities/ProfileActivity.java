package com.example.spatia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spatia.R;
import com.example.spatia.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private User userProfile;
    
    // UI components
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText addressEditText;
    private Button editSaveButton;
    private ProgressBar progressBar;
    
    // State tracking
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        
        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        
        if (currentUser == null) {
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize UI components
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        addressEditText = findViewById(R.id.addressEditText);
        editSaveButton = findViewById(R.id.editSaveButton);
        progressBar = findViewById(R.id.profileProgressBar);
        
        // Set up button click listener
        editSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    saveUserProfile();
                } else {
                    enableEditMode();
                }
            }
        });
        
        // Load user data
        loadUserProfile();
    }
    
    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);
        
        String userId = currentUser.getUid();
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        progressBar.setVisibility(View.GONE);
                        
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                userProfile = document.toObject(User.class);
                                updateUI();
                            } else {
                                Log.d(TAG, "No user document found");
                                
                                Intent intent = new Intent(ProfileActivity.this, AuthActivity.class);
                                startActivity(intent);  
                                finish();
                            }
                        } else {
                            Log.w(TAG, "Error loading user profile", task.getException());
                            Toast.makeText(ProfileActivity.this, 
                                    "Failed to load profile: " + task.getException().getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    
    private void updateUI() {
        // Set values in the UI fields
        nameEditText.setText(userProfile.getName());
        emailEditText.setText(userProfile.getEmail());
        phoneEditText.setText(userProfile.getPhone());
        addressEditText.setText(userProfile.getAddress());
        
        // Email is always disabled, as per requirements
        emailEditText.setEnabled(false);
        
        // Apply any theme/style updates
        resetEditMode();
    }
    
    private void enableEditMode() {
        isEditMode = true;
        
        // Enable the fields that should be editable
        nameEditText.setEnabled(true);
        phoneEditText.setEnabled(true);
        addressEditText.setEnabled(true);
        
        // Change button text
        editSaveButton.setText("Save Profile");
        
        // Focus on name field
        nameEditText.requestFocus();
    }
    
    private void resetEditMode() {
        isEditMode = false;
        
        // Disable all fields
        nameEditText.setEnabled(false);
        phoneEditText.setEnabled(false);
        addressEditText.setEnabled(false);
        
        // Reset button text
        editSaveButton.setText("Edit Profile");
    }
    
    private void saveUserProfile() {
        // Validate input fields
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        
        // Update user object with new values
        userProfile.setName(name);
        userProfile.setPhone(phone);
        userProfile.setAddress(address);
        
        progressBar.setVisibility(View.VISIBLE);
        
        // Save updated profile to Firebase
        db.collection("users").document(currentUser.getUid())
                .set(userProfile)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, 
                                    "Profile updated successfully", 
                                    Toast.LENGTH_SHORT).show();
                            resetEditMode();
                        } else {
                            Log.w(TAG, "Error updating profile", task.getException());
                            Toast.makeText(ProfileActivity.this, 
                                    "Failed to update profile: " + task.getException().getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
