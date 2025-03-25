package com.example.spatia.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.spatia.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends Activity {

    private static final String TAG = "EmailPassword";
    private FirebaseAuth mAuth;
    
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
        
        // Start with login view
        showLoginView();
    }

    private void showLoginView() {
        setContentView(R.layout.login);
        isLoginMode = true;
        
        // Initialize UI elements
        emailField = findViewById(R.id.loginEmail);
        passwordField = findViewById(R.id.loginPassword);
        actionButton = findViewById(R.id.loginButton);
        switchAuthMode = findViewById(R.id.switchToSignup);
        
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
        setContentView(R.layout.signup);
        isLoginMode = false;
        
        // Initialize UI elements
        emailField = findViewById(R.id.signupEmail);
        passwordField = findViewById(R.id.signupPassword);
        actionButton = findViewById(R.id.signupButton);
        switchAuthMode = findViewById(R.id.switchToLogin);
        
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
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            sendEmailVerification();
                            Toast.makeText(AuthActivity.this, "Account created successfully!",
                                    Toast.LENGTH_SHORT).show();
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
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
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
        // For testing, simply display a message
        Toast.makeText(AuthActivity.this, "User already signed in",
                Toast.LENGTH_SHORT).show();
        // In a real app, you would navigate to the main activity
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // User is signed in, show a success message
            Toast.makeText(AuthActivity.this, "User: " + user.getEmail(), 
                    Toast.LENGTH_SHORT).show();
            // In a real app, you would navigate to the main activity
        }
    }
}