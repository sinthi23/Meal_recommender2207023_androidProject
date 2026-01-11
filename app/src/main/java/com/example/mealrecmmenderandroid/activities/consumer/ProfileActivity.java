package com.example.mealrecmmenderandroid.activities.consumer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.activities.LoginActivity;
import com.example.mealrecmmenderandroid.helpers.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private SessionManager sessionManager;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvUserType;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance()
                .getInstance("https://meal-recommender-android-9801b-default-rtdb.firebaseio.com")
                .getReference("users");
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
        // Display from session first (fast)
        String userName = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();
        String accountType = sessionManager.getAccountType();

        tvUserName.setText(userName != null ? userName : "User");
        tvUserEmail.setText(email != null ? email : "No email");
        tvUserType.setText(accountType != null ? accountType : "Consumer");

        // Then load from Firebase for accuracy
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            usersRef.child(currentUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Try to get username, fallback to fullName
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
                                    displayName = currentUser.getEmail().split("@")[0];
                                }

                                // Determine account type
                                String finalAccountType = accountType;
                                if (finalAccountType == null || finalAccountType.isEmpty()) {
                                    finalAccountType = userType;
                                }
                                if (finalAccountType == null || finalAccountType.isEmpty()) {
                                    finalAccountType = "Consumer";
                                }

                                // Update UI
                                tvUserName.setText(displayName);
                                tvUserEmail.setText(currentUser.getEmail());
                                tvUserType.setText(finalAccountType);

                                // Update session if needed
                                if (!displayName.equals(userName)) {
                                    sessionManager.createLoginSession(
                                            currentUser.getUid(),
                                            finalAccountType,
                                            displayName,
                                            currentUser.getEmail()
                                    );
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ProfileActivity.this,
                                    "Error loading profile: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
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