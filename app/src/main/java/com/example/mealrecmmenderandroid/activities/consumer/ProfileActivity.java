package com.example.mealrecmmenderandroid.activities.consumer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mealrecmmenderandroid.activities.LoginActivity;
import com.example.mealrecmmenderandroid.databinding.ActivityProfileBinding;
import com.example.mealrecmmenderandroid.helpers.FirebaseHelper;
import com.example.mealrecmmenderandroid.helpers.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private SessionManager sessionManager;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        firebaseHelper = FirebaseHelper.getInstance();

        setupToolbar();
        loadUserProfile();
        setupListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadUserProfile() {
        // Show loading
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.profileCard.setVisibility(View.GONE);

        // First, load from session (fast)
        String sessionUsername = sessionManager.getUserName();
        String sessionEmail = sessionManager.getUserEmail();
        String sessionAccountType = sessionManager.getAccountType();

        if (sessionUsername != null && !sessionUsername.isEmpty()) {
            binding.tvUserName.setText(sessionUsername);
        }
        if (sessionEmail != null && !sessionEmail.isEmpty()) {
            binding.tvUserEmail.setText(sessionEmail);
        }
        if (sessionAccountType != null && !sessionAccountType.isEmpty()) {
            binding.tvUserType.setText(capitalize(sessionAccountType));
        }

        // Then, load from Firebase (accurate)
        String userId = sessionManager.getUserId();
        if (userId != null) {
            firebaseHelper.getUserRef(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.profileCard.setVisibility(View.VISIBLE);

                            if (snapshot.exists()) {
                                // Get all possible name fields
                                String username = snapshot.child("username").getValue(String.class);
                                String fullName = snapshot.child("fullName").getValue(String.class);
                                String email = snapshot.child("email").getValue(String.class);
                                String userType = snapshot.child("userType").getValue(String.class);
                                String accountType = snapshot.child("accountType").getValue(String.class);
                                String phone = snapshot.child("phone").getValue(String.class);

                                // Priority: username → fullName → email prefix
                                String displayName = username;
                                if (displayName == null || displayName.isEmpty()) {
                                    displayName = fullName;
                                }
                                if (displayName == null || displayName.isEmpty() && email != null) {
                                    displayName = email.split("@")[0];
                                }

                                // Display username
                                if (displayName != null && !displayName.isEmpty()) {
                                    binding.tvUserName.setText(displayName);

                                    // Update session with username if it was missing
                                    if (username == null || username.isEmpty()) {
                                        sessionManager.updateUsername(displayName);
                                        // Also update Firebase
                                        firebaseHelper.getUserRef(userId)
                                                .child("username")
                                                .setValue(displayName);
                                    }
                                }

                                // Display email
                                if (email != null && !email.isEmpty()) {
                                    binding.tvUserEmail.setText(email);
                                }

                                // Display account type
                                String finalAccountType = accountType;
                                if (finalAccountType == null || finalAccountType.isEmpty()) {
                                    finalAccountType = userType;
                                }
                                if (finalAccountType != null && !finalAccountType.isEmpty()) {
                                    binding.tvUserType.setText(capitalize(finalAccountType));
                                }

                                // Display phone (if available)
                                if (phone != null && !phone.isEmpty() && binding.tvUserPhone != null) {
                                    binding.tvUserPhone.setText(phone);
                                    if (binding.phoneContainer != null) {
                                        binding.phoneContainer.setVisibility(View.VISIBLE);
                                    }
                                }

                                // Display user stats (if they exist)
                                loadUserStats(snapshot);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.profileCard.setVisibility(View.VISIBLE);
                            Toast.makeText(ProfileActivity.this,
                                    "Error loading profile: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.profileCard.setVisibility(View.VISIBLE);
        }
    }

    private void loadUserStats(DataSnapshot snapshot) {
        // For providers: show recipe stats
        if (sessionManager.isProvider() && binding.statsContainer != null) {
            Integer totalRecipes = snapshot.child("totalRecipesProvided").getValue(Integer.class);
            Double averageRating = snapshot.child("averageRating").getValue(Double.class);
            Integer ratingCount = snapshot.child("ratingCount").getValue(Integer.class);

            if (totalRecipes != null && totalRecipes > 0) {
                binding.statsContainer.setVisibility(View.VISIBLE);
                if (binding.tvTotalRecipes != null) {
                    binding.tvTotalRecipes.setText(String.valueOf(totalRecipes));
                }

                if (binding.tvAverageRating != null) {
                    if (averageRating != null && ratingCount != null && ratingCount > 0) {
                        binding.tvAverageRating.setText(String.format("%.1f ★ (%d)", averageRating, ratingCount));
                    } else {
                        binding.tvAverageRating.setText("No ratings yet");
                    }
                }
            } else {
                binding.statsContainer.setVisibility(View.GONE);
            }
        }
    }

    private void setupListeners() {
        binding.logoutButton.setOnClickListener(v -> logout());
    }

    private void logout() {
        // Sign out from Firebase
        firebaseHelper.getAuth().signOut();

        // Clear session
        sessionManager.logoutUser();

        // Navigate to login
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}