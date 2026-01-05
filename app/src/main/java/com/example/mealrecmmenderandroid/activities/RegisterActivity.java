package com.example.mealrecmmenderandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.databinding.ActivityRegisterBinding;
import com.example.mealrecmmenderandroid.models.User;
import com.example.mealrecmmenderandroid.helpers.FirebaseHelper;
import com.example.mealrecmmenderandroid.helpers.SessionManager;
import com.example.mealrecmmenderandroid.utils.ValidationHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

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
        binding.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        binding.loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.userTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
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
            }
        });
    }

    private void registerUser() {
        String name = "";
        String email = "";
        String phone = "";
        String password = "";
        String confirmPassword = "";
        String businessName = "";

        if (binding.nameEditText.getText() != null) {
            name = binding.nameEditText.getText().toString().trim();
        }
        if (binding.emailEditText.getText() != null) {
            email = binding.emailEditText.getText().toString().trim();
        }
        if (binding.phoneEditText.getText() != null) {
            phone = binding.phoneEditText.getText().toString().trim();
        }
        if (binding.passwordEditText.getText() != null) {
            password = binding.passwordEditText.getText().toString().trim();
        }
        if (binding.confirmPasswordEditText.getText() != null) {
            confirmPassword = binding.confirmPasswordEditText.getText().toString().trim();
        }
        if (binding.businessNameEditText.getText() != null) {
            businessName = binding.businessNameEditText.getText().toString().trim();
        }

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
        final String finalName = name;
        final String finalEmail = email;
        final String finalPhone = phone;
        final String finalBusinessName = businessName;

        firebaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = firebaseHelper.getCurrentUserId();
                            createUserInDatabase(userId, finalName, finalEmail, finalPhone, finalBusinessName);
                        } else {
                            showLoading(false);
                            String errorMessage = "Unknown error";
                            if (task.getException() != null) {
                                errorMessage = task.getException().getMessage();
                            }
                            Toast.makeText(RegisterActivity.this,
                                    "Registration failed: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
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
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Save session
                        sessionManager.createLoginSession(userId, selectedUserType, name, email);

                        showLoading(false);
                        Toast.makeText(RegisterActivity.this,
                                "Registration successful!",
                                Toast.LENGTH_SHORT).show();

                        // Navigate to main activity
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showLoading(false);
                        Toast.makeText(RegisterActivity.this,
                                "Failed to save user data: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.registerButton.setEnabled(!show);
    }
}