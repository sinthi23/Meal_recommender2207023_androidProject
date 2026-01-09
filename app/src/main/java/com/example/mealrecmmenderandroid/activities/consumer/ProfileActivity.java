package com.example.mealrecmmenderandroid.activities.consumer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.activities.LoginActivity;
import com.example.mealrecmmenderandroid.helpers.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private SessionManager sessionManager;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvUserType;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);

        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        initViews();

        // Load user data
        loadUserData();

        // Setup logout button
        setupLogoutButton();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvUserType = findViewById(R.id.tv_user_type);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Display user email
            String email = currentUser.getEmail();
            tvUserEmail.setText(email != null ? email : "No email");

            // Display user name (you can get this from Firebase Database if stored)
            String displayName = currentUser.getDisplayName();
            tvUserName.setText(displayName != null ? displayName : "User");

            // Display user type
            String userType = sessionManager.isProvider() ? "Provider" : "Consumer";
            tvUserType.setText(userType);
        } else {
            tvUserName.setText("Guest");
            tvUserEmail.setText("Not logged in");
            tvUserType.setText("Unknown");
        }
    }

    private void setupLogoutButton() {
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logoutUser())
                .setNegativeButton("No", null)
                .show();
    }

    private void logoutUser() {
        // Sign out from Firebase Auth
        if (mAuth != null) {
            mAuth.signOut();
        }

        // Clear session
        sessionManager.logoutUser();

        // Show success message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to login and clear back stack
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}