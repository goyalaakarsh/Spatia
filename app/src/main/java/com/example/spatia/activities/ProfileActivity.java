package com.example.spatia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.example.spatia.R;
import com.example.spatia.model.User;
import com.example.spatia.util.BottomNavHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private User userProfile;

    // UI components
    private CircleImageView profileImageView;
    private TextView userNameTextView;
    private TextView userEmailTextView;
    private ConstraintLayout editProfileButton;
    private ConstraintLayout cartButton;
    private ConstraintLayout ordersButton;
    private ConstraintLayout logoutButton;
    private View loadingSpinner;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.profile);

            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            currentUser = mAuth.getCurrentUser();

            if (currentUser == null) {
                Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show();
                navigateToLogin();
                return;
            }

            initializeViews();
            setupClickListeners();
            setupBottomNavigation();
            loadUserProfile();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
            navigateToLogin();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data when returning to this activity
        if (currentUser != null) {
            loadUserProfile();
        }
    }

    private void initializeViews() {
        try {
            profileImageView = findViewById(R.id.profileImageView);
            userNameTextView = findViewById(R.id.user_name);
            userEmailTextView = findViewById(R.id.user_email);
            editProfileButton = findViewById(R.id.edit_profile_button);
            cartButton = findViewById(R.id.cart_button);
            ordersButton = findViewById(R.id.orders_button);
            logoutButton = findViewById(R.id.logout_button);
            
            // Getting loading spinner and bottom navigation
            loadingSpinner = findViewById(R.id.loadingSpinner);
            bottomNavigationView = findViewById(R.id.bottom_navigation);
            
            // Safety check for specific views
            if (profileImageView == null) Log.w(TAG, "profileImageView is null");
            if (userNameTextView == null) Log.w(TAG, "userNameTextView is null");
            if (userEmailTextView == null) Log.w(TAG, "userEmailTextView is null");
            if (editProfileButton == null) Log.w(TAG, "editProfileButton is null");
            if (loadingSpinner == null) Log.w(TAG, "loadingSpinner is null");
            if (bottomNavigationView == null) Log.w(TAG, "bottomNavigationView is null");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        try {
            if (editProfileButton != null) {
                editProfileButton.setOnClickListener(v -> navigateToEditProfile());
            }

            if (cartButton != null) {
                cartButton.setOnClickListener(v -> navigateToCart());
            }

            if (ordersButton != null) {
                ordersButton.setOnClickListener(v -> navigateToOrders());
            }

            if (logoutButton != null) {
                logoutButton.setOnClickListener(v -> logout());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners", e);
        }
    }

    private void setupBottomNavigation() {
        try {
            if (bottomNavigationView != null) {
                // Use our helper class to set up the bottom navigation
                BottomNavHelper.setupBottomNavigation(this, bottomNavigationView);
            } else {
                Log.w(TAG, "bottomNavigationView is null. Could not set up bottom navigation.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up bottom navigation", e);
        }
    }

    private void loadUserProfile() {
        // Show loading spinner
        if (loadingSpinner != null) {
            loadingSpinner.setVisibility(View.VISIBLE);
        }

        try {
            String userId = currentUser.getUid();
            db.collection("users").document(userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            // Hide loading spinner
                            if (loadingSpinner != null) {
                                loadingSpinner.setVisibility(View.GONE);
                            }

                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    userProfile = document.toObject(User.class);
                                    updateUI();
                                } else {
                                    Log.d(TAG, "No user document found");
                                    userProfile = new User(currentUser.getUid(), currentUser.getEmail());
                                    db.collection("users").document(userId).set(userProfile);
                                    updateUI();
                                }
                            } else {
                                Log.w(TAG, "Error loading user profile", task.getException());
                                Toast.makeText(ProfileActivity.this,
                                        "Failed to load profile: " + 
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error loading user profile", e);
            if (loadingSpinner != null) {
                loadingSpinner.setVisibility(View.GONE);
            }
            Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        try {
            if (userProfile == null) return;
            
            if (userNameTextView != null) {
                userNameTextView.setText(userProfile.getName() != null && !userProfile.getName().isEmpty() ?
                        userProfile.getName() : "User");
            }
            
            if (userEmailTextView != null) {
                userEmailTextView.setText(userProfile.getEmail());
            }

            loadProfileImage();
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI", e);
        }
    }
    
    private void loadProfileImage() {
        try {
            if (profileImageView == null || userProfile == null) return;
            
            String profileImageUrl = userProfile.getProfileImageUrl();
            
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                try {
                    if (!profileImageUrl.startsWith("http")) {
                        profileImageUrl = "https:" + profileImageUrl;
                    }

                    Glide.with(this)
                        .load(profileImageUrl.trim())
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(profileImageView);
                } catch (Exception e) {
                    Log.e(TAG, "Exception loading profile image: " + e.getMessage(), e);
                    profileImageView.setImageResource(R.drawable.default_profile);
                }
            } else {
                profileImageView.setImageResource(R.drawable.default_profile);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile image", e);
        }
    }

    private void navigateToEditProfile() {
        try {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to EditProfile", e);
            Toast.makeText(this, "Error opening edit profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToCart() {
        // Navigate to cart activity
        Toast.makeText(this, "Cart feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void navigateToOrders() {
        // Navigate to orders activity
        Toast.makeText(this, "Orders feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        try {
            mAuth.signOut();
            navigateToLogin();
        } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
            Toast.makeText(this, "Error logging out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToLogin() {
        try {
            // Using reflection to avoid direct dependencies
            Class<?> authActivityClass;
            try {
                authActivityClass = Class.forName("com.example.spatia.activities.AuthActivity");
            } catch (ClassNotFoundException e) {
                authActivityClass = Class.forName("com.example.spatia.activities.LoginActivity");
            }
            
            Intent intent = new Intent(ProfileActivity.this, authActivityClass);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to login screen", e);
            // As a fallback, just finish this activity
            finish();
        }
    }
}
