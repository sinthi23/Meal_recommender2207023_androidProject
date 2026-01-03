package com.example.mealrecmmenderandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mealrecmmenderandroid.databinding.ActivityLoginBinding;
import com.example.mealrecmmenderandroid.models.User;
import com.example.mealrecmmenderandroid.helpers.SessionManager;
import com.example.mealrecmmenderandroid.helpers.FirebaseHelper;
import com.example.mealrecmmenderandroid.utils.ValidationHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = new SessionManager(this);

        // Check if user already logged in
        if (firebaseHelper.isUserLoggedIn() && sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        setupListeners();
    }

    private void setupListeners() {
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        binding.registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        binding.forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
    }

    private void loginUser() {
        String email = "";
        String password = "";

        if (binding.emailEditText.getText() != null) {
            email = binding.emailEditText.getText().toString().trim();
        }
        if (binding.passwordEditText.getText() != null) {
            password = binding.passwordEditText.getText().toString().trim();
        }

        // Validate inputs
        String emailError = ValidationHelper.getEmailError(email);
        if (emailError != null) {
            binding.emailEditText.setError(emailError);
            binding.emailEditText.requestFocus();
            return;
        }

        String passwordError = ValidationHelper.getPasswordError(password);
        if (passwordError != null) {
            binding.passwordEditText.setError(passwordError);
            binding.passwordEditText.requestFocus();
            return;
        }

        showLoading(true);

        firebaseHelper.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = firebaseHelper.getCurrentUserId();
                            if (userId != null) {
                                fetchUserDataAndNavigate(userId);
                            } else {
                                showLoading(false);
                                Toast.makeText(LoginActivity.this,
                                        "Login failed: Unable to get user ID",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            showLoading(false);
                            String errorMessage = "Unknown error";
                            if (task.getException() != null) {
                                errorMessage = task.getException().getMessage();
                            }
                            Toast.makeText(LoginActivity.this,
                                    "Login failed: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void fetchUserDataAndNavigate(String userId) {
        firebaseHelper.getUserRef(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                if (!user.isActive()) {
                                    showLoading(false);
                                    Toast.makeText(LoginActivity.this,
                                            "Your account is deactivated. Please contact admin.",
                                            Toast.LENGTH_LONG).show();
                                    firebaseHelper.getAuth().signOut();
                                    return;
                                }

                                // Update last login
                                firebaseHelper.getUserRef(userId)
                                        .child("lastLoginDate")
                                        .setValue(System.currentTimeMillis());

                                // Save session
                                String userType = user.getUserType() != null ? user.getUserType() : "consumer";
                                String userName = user.getName() != null ? user.getName() : "User";
                                String userEmail = user.getEmail() != null ? user.getEmail() : "";

                                sessionManager.createLoginSession(userId, userType, userName, userEmail);

                                showLoading(false);
                                Toast.makeText(LoginActivity.this,
                                        "Welcome back, " + userName + "!",
                                        Toast.LENGTH_SHORT).show();
                                navigateToMain();
                            } else {
                                showLoading(false);
                                Toast.makeText(LoginActivity.this,
                                        "Unable to load user data",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            showLoading(false);
                            Toast.makeText(LoginActivity.this,
                                    "User data not found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showLoading(false);
                        Toast.makeText(LoginActivity.this,
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.loginButton.setEnabled(!show);
        binding.emailEditText.setEnabled(!show);
        binding.passwordEditText.setEnabled(!show);
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}