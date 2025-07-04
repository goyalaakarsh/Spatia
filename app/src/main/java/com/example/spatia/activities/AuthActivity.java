package com.example.spatia.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.spatia.MainActivity;
import com.example.spatia.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.example.spatia.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthActivity extends Activity {

    private static final String TAG = "EmailPassword";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI elements
    private EditText emailField;
    private EditText passwordField;
    private Button actionButton;
    private TextView switchAuthMode;

    // To keep track of whether we're in login or signup mode
    private boolean isLoginMode = true;

    // Creates firebase auth instance
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Start with login view
        showLoginView();
    }

    private void showLoginView() {
        setContentView(R.layout.log_in);
        isLoginMode = true;

        // Initialize UI elements with IDs from log_in.xml
        emailField = findViewById(R.id.loginEmail);
        passwordField = findViewById(R.id.loginPassword);
        actionButton = findViewById(R.id.SignupButton); // Note: Button ID is SignupButton even though it's for login
        switchAuthMode = findViewById(R.id.sign_up_text);

        // Set button click listener
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();
                String password = passwordField.getText().toString();

                if (validateForm(email, password)) {
                    signIn(email, password);
                }
            }
        });

        // Set switch mode listener
        switchAuthMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignupView();
            }
        });
    }

    private void showSignupView() {
        setContentView(R.layout.sign_up);
        isLoginMode = false;

        // Initialize UI elements with IDs from sign_up.xml
        emailField = findViewById(R.id.SignUpEmail);
        passwordField = findViewById(R.id.SignUpPassword);
        actionButton = findViewById(R.id.SignupButton);
        switchAuthMode = findViewById(R.id.log_in_text);

        // Set button click listener
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();
                String password = passwordField.getText().toString();

                if (validateForm(email, password)) {
                    createAccount(email, password);
                }
            }
        });

        // Set switch mode listener
        switchAuthMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginView();
            }
        });
    }

    private boolean validateForm(String email, String password) {
        boolean valid = true;

        if (email.isEmpty()) {
            emailField.setError("Email is required");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Enter a valid email address");
            valid = false;
        } else {
            emailField.setError(null);
        }

        if (password.isEmpty()) {
            passwordField.setError("Password is required");
            valid = false;
        } else if (password.length() < 6) {
            passwordField.setError("Password must be at least 6 characters");
            valid = false;
        } else {
            passwordField.setError(null);
        }

        return valid;
    }

    // Checks if user is already signed in
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            reload();
        }
    }

    // Registers new user
    private void createAccount(String email, String password) {
        // Show loading state
        actionButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            // updateUI(firebaseUser);
                            // sendEmailVerification();
                            if (firebaseUser != null) {
                                User user = new User(firebaseUser.getUid(), email);
                                db.collection("users")
                                        .document(firebaseUser.getUid())
                                        .set(user)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(AuthActivity.this, "Account created successfully!",
                                                    Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(AuthActivity.this, ProfileActivity.class);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(AuthActivity.this, "Failed to save user: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        });
                            }
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(AuthActivity.this, "Authentication failed: " +
                                    task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        actionButton.setEnabled(true);
                    }
                });
    }

    // Signs in existing user
    private void signIn(String email, String password) {
        // Show loading state
        actionButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(AuthActivity.this, "Login successful!",
                                    Toast.LENGTH_SHORT).show();
                            onSuccessfulLogin();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(AuthActivity.this, "Authentication failed: " +
                                    task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        actionButton.setEnabled(true);
                    }
                });
    }

    private void sendEmailVerification() {
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AuthActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AuthActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void reload() {
        Toast.makeText(AuthActivity.this, "User already signed in",
                Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(AuthActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(AuthActivity.this, "User: " + user.getEmail(),
                    Toast.LENGTH_SHORT).show();

            setContentView(R.layout.temp_navigation);
        }
    }

    private void onSuccessfulLogin() {
        // After successful login, navigate to MainActivity
        Intent intent = new Intent(AuthActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}