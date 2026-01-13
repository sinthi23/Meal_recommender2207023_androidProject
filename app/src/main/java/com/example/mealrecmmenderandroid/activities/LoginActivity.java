package com.example.mealrecmmenderandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.activities.provider.ProviderDashboardActivity;
import com.example.mealrecmmenderandroid.helpers.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmailOrUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase first
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance()
                .getInstance("https://meal-recommender-android-9801b-default-rtdb.firebaseio.com")
                .getReference("users");
        sessionManager = new SessionManager(this);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            redirectToAppropriateActivity();
            return;
        }

        // Initialize views
        initViews();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        etEmailOrUsername = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    private void loginUser() {
        String emailOrUsername = etEmailOrUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(emailOrUsername)) {
            etEmailOrUsername.setError("Email or username is required");
            etEmailOrUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Check if input is email or username
        if (emailOrUsername.contains("@")) {
            // It's an email, login directly
            loginWithEmail(emailOrUsername, password);
        } else {
            // It's a username, find the email first
            findEmailByUsername(emailOrUsername, password);
        }
    }

    private void findEmailByUsername(String username, String password) {
        Query query = usersRef.orderByChild("username").equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Get the first matching user
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String email = userSnapshot.child("email").getValue(String.class);
                        if (email != null) {
                            // Found the email, now login
                            loginWithEmail(email, password);
                            return;
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this,
                            "Username not found",
                            Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this,
                            "Username not found",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this,
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginWithEmail(String email, String password) {
        // Authenticate with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Get user data from database
                            usersRef.child(user.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            progressBar.setVisibility(View.GONE);
                                            btnLogin.setEnabled(true);

                                            if (snapshot.exists()) {
                                                // Get user data
                                                String username = snapshot.child("username").getValue(String.class);
                                                String fullName = snapshot.child("fullName").getValue(String.class);
                                                String userType = snapshot.child("userType").getValue(String.class);
                                                String accountType = snapshot.child("accountType").getValue(String.class);

                                                // Determine display name
                                                String displayName = username;
                                                if (displayName == null || displayName.isEmpty()) {
                                                    displayName = fullName;
                                                }
                                                if (displayName == null || displayName.isEmpty()) {
                                                    displayName = email.split("@")[0];
                                                }

                                                // Determine account type
                                                String finalAccountType = accountType;
                                                if (finalAccountType == null || finalAccountType.isEmpty()) {
                                                    finalAccountType = userType;
                                                }
                                                if (finalAccountType == null || finalAccountType.isEmpty()) {
                                                    finalAccountType = "consumer";
                                                }

                                                // Migrate username if doesn't exist
                                                if (username == null || username.isEmpty()) {
                                                    usersRef.child(user.getUid())
                                                            .child("username")
                                                            .setValue(displayName);
                                                }

                                                // Save session with username
                                                sessionManager.createLoginSession(
                                                        user.getUid(),
                                                        finalAccountType,
                                                        displayName,
                                                        user.getEmail()
                                                );

                                                Toast.makeText(LoginActivity.this,
                                                        "Welcome back, " + displayName + "!",
                                                        Toast.LENGTH_SHORT).show();

                                                // Route based on user type
                                                redirectToAppropriateActivity();
                                            } else {
                                                Toast.makeText(LoginActivity.this,
                                                        "Failed to retrieve user data",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            progressBar.setVisibility(View.GONE);
                                            btnLogin.setEnabled(true);
                                            Toast.makeText(LoginActivity.this,
                                                    "Error: " + error.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);

                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Login failed";

                        // User-friendly error messages
                        if (errorMessage.contains("no user record") || errorMessage.contains("user-not-found")) {
                            errorMessage = "Account not found. Please check your credentials.";
                        } else if (errorMessage.contains("password is invalid") || errorMessage.contains("wrong-password")) {
                            errorMessage = "Incorrect password. Please try again.";
                        } else if (errorMessage.contains("network")) {
                            errorMessage = "Network error. Please check your connection.";
                        }

                        Toast.makeText(LoginActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void redirectToAppropriateActivity() {
        Intent intent;
        if (sessionManager.isProvider()) {
            intent = new Intent(LoginActivity.this, ProviderDashboardActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}