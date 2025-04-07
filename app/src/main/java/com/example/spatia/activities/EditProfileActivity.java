package com.example.spatia.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
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

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private User userProfile;
    private Uri imageUri;
    private ActivityResultLauncher<String> imagePicker;

    // UI components
    private CircleImageView profileImageView;
    private CardView uploadImageButton;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText addressEditText;
    private Button editSaveButton;
    private View loadingSpinner; // Updated to use the new loading spinner
    private BottomNavigationView bottomNavigationView;

    private boolean isEditMode = false;
    private boolean isImageChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupImagePicker();
        setupClickListeners();
        setupBottomNavigation();
        loadUserProfile();
    }

    private void initializeViews() {
        profileImageView = findViewById(R.id.profileImageView);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        addressEditText = findViewById(R.id.addressEditText);
        editSaveButton = findViewById(R.id.editSaveButton);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        // Setup back navigation in navbar if it exists
        View backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void setupImagePicker() {
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            imageUri = result;
                            profileImageView.setImageURI(imageUri);
                            isImageChanged = true;
                        }
                    }
                });
    }

    private void setupClickListeners() {
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

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    imagePicker.launch("image/*");
                }
            }
        });
    }

    private void setupBottomNavigation() {
        // Setup bottom navigation
        BottomNavHelper.setupBottomNavigation(this, bottomNavigationView);
    }

    private void loadUserProfile() {
        // Show the loading spinner
        loadingSpinner.setVisibility(View.VISIBLE);

        String userId = currentUser.getUid();
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        // Hide the loading spinner
                        loadingSpinner.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                userProfile = document.toObject(User.class);
                                updateUI();
                            } else {
                                Log.d(TAG, "No user document found");
                                userProfile = new User(currentUser.getUid(), currentUser.getEmail());
                                updateUI();
                            }
                        } else {
                            Log.w(TAG, "Error loading user profile", task.getException());
                            Toast.makeText(EditProfileActivity.this,
                                    "Failed to load profile: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUI() {
        nameEditText.setText(userProfile.getName());
        emailEditText.setText(userProfile.getEmail());
        phoneEditText.setText(userProfile.getPhone());
        addressEditText.setText(userProfile.getAddress());

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

        resetEditMode();
    }

    private void enableEditMode() {
        isEditMode = true;

        nameEditText.setEnabled(true);
        phoneEditText.setEnabled(true);
        addressEditText.setEnabled(true);
        uploadImageButton.setVisibility(View.VISIBLE);

        editSaveButton.setText("Save Profile");

        nameEditText.requestFocus();
    }

    private void resetEditMode() {
        isEditMode = false;

        nameEditText.setEnabled(false);
        phoneEditText.setEnabled(false);
        addressEditText.setEnabled(false);
        uploadImageButton.setVisibility(View.GONE);

        editSaveButton.setText("Edit Profile");
    }

    private void saveUserProfile() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        userProfile.setName(name);
        userProfile.setPhone(phone);
        userProfile.setAddress(address);

        if (isImageChanged && imageUri != null) {
            uploadImage();
        } else {
            updateProfileInFirestore();
        }
    }

    private void uploadImage() {
        // Show loading spinner during upload
        loadingSpinner.setVisibility(View.VISIBLE);

        String fileName = "profile_" + currentUser.getUid();

        // Upload to Cloudinary
        MediaManager.get().upload(imageUri)
                .option("public_id", fileName)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Image upload started");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Log.d(TAG, "Image upload successful");

                        String imageUrl;
                        if (resultData.containsKey("secure_url")) {
                            imageUrl = (String) resultData.get("secure_url");
                        } else {
                            imageUrl = (String) resultData.get("url");
                        }

                        if (imageUrl != null && !imageUrl.startsWith("http")) {
                            imageUrl = "https:" + imageUrl;
                        }

                        userProfile.setProfileImageUrl(imageUrl);
                        Toast.makeText(EditProfileActivity.this, "Image upload successful", Toast.LENGTH_SHORT).show();
                        updateProfileInFirestore();

                        isImageChanged = false;
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Image upload error: " + error.getDescription());
                        loadingSpinner.setVisibility(View.GONE);
                        Toast.makeText(EditProfileActivity.this,
                                "Failed to upload image: " + error.getDescription(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.d(TAG, "Image upload rescheduled");
                    }
                })
                .dispatch();
    }

    private void updateProfileInFirestore() {
        // Show loading spinner during update
        loadingSpinner.setVisibility(View.VISIBLE);

        db.collection("users").document(currentUser.getUid())
                .set(userProfile)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Hide loading spinner
                        loadingSpinner.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this,
                                    "Profile updated successfully",
                                    Toast.LENGTH_SHORT).show();
                            resetEditMode();
                            finish(); // Return to profile activity
                        } else {
                            Log.w(TAG, "Error updating profile", task.getException());
                            Toast.makeText(EditProfileActivity.this,
                                    "Failed to update profile: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
