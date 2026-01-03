package com.example.mealrecmmenderandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.healthymeal.recommender.R;
import com.healthymeal.recommender.databinding.ActivityRegisterBinding;
import com.healthymeal.recommender.models.User;
import com.healthymeal.recommender.utils.FirebaseHelper;
import com.healthymeal.recommender.utils.SessionManager;
import com.healthymeal.recommender.utils.ValidationHelper;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private String selectedUserType = "consumer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = new SessionManager(this);

        setupListeners();
    }

    private void setupListeners() {
        binding.registerButton.setOnClickListener(v -> registerUser());

        binding.loginTextView.setOnClickListener(v -> {
            finish();
        });

        binding.userTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton radioButton = findViewById(checkedId);
            if (radioButton != null) {
                selectedUserType = radioButton.getTag().toString();

                // Show/hide business fields for providers
                if ("provider".equals(selectedUserType)) {
                    binding.businessNameLayout.setVisibility(View.VISIBLE);
                } else {
                    binding.businessNameLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void registerUser() {
        String name = binding.nameEditText.getText().toString().trim();
        String email = binding.emailEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordEditText.getText().toString().trim();
        String businessName = binding.businessNameEditText.getText().toString().trim();

        // Validate inputs
        String nameError = ValidationHelper.getNameError(name);
        if (nameError != null) {
            binding.nameEditText.setError(nameError);
            binding.nameEditText.requestFocus();
            return;
        }

        String emailError = ValidationHelper.getEmailError(email);
        if (emailError != null) {
            binding.emailEditText.setError(emailError);
            binding.emailEditText.requestFocus();
            return;
        }

        String phoneError = ValidationHelper.getPhoneError(phone);
        if (phoneError != null) {
            binding.phoneEditText.setError(phoneError);
            binding.phoneEditText.requestFocus();
            return;
        }

        String passwordError = ValidationHelper.getPasswordError(password);
        if (passwordError != null) {
            binding.passwordEditText.setError(passwordError);
            binding.passwordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.confirmPasswordEditText.setError("Passwords do not match");
            binding.confirmPasswordEditText.requestFocus();
            return;
        }

        if ("provider".equals(selectedUserType) && businessName.isEmpty()) {
            binding.businessNameEditText.setError("Business name is required");
            binding.businessNameEditText.requestFocus();
            return;
        }

        showLoading(true);

        // Create Firebase Auth user
        firebaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = firebaseHelper.getCurrentUserId();
                        createUserInDatabase(userId, name, email, phone, businessName);
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Registration failed: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createUserInDatabase(String userId, String name, String email,
                                      String phone, String businessName) {
        User user = new User(userId, email, name, selectedUserType);
        user.setPhoneNumber(phone);

        if ("provider".equals(selectedUserType)) {
            user.setBusinessName(businessName);
        }

        firebaseHelper.getUserRef(userId).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    // Save session
                    sessionManager.createLoginSession(userId, selectedUserType, name, email);

                    showLoading(false);
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();

                    // Navigate to main activity
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to save user data: " +
                            e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.registerButton.setEnabled(!show);
    }
}