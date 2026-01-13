package com.example.mealrecmmenderandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.activities.provider.ProviderDashboardActivity;
import com.example.mealrecmmenderandroid.helpers.SessionManager;
import com.example.mealrecmmenderandroid.models.User;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private RadioGroup rgUserType;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private SessionManager sessionManager;
    
    // Password requirement views
    private TextView tvRequirementLength, tvRequirementUppercase, tvRequirementLowercase, tvRequirementSpecial;
    private LinearLayout passwordRequirementsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance()
                .getInstance("https://meal-recommender-android-9801b-default-rtdb.firebaseio.com")
                .getReference("users");
        sessionManager = new SessionManager(this);

        // Initialize views
        initViews();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        rgUserType = findViewById(R.id.rg_user_type);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);
        
        // Initialize password requirement views
        tvRequirementLength = findViewById(R.id.tv_requirement_length);
        tvRequirementUppercase = findViewById(R.id.tv_requirement_uppercase);
        tvRequirementLowercase = findViewById(R.id.tv_requirement_lowercase);
        tvRequirementSpecial = findViewById(R.id.tv_requirement_special);
        passwordRequirementsLayout = findViewById(R.id.passwordRequirementsLayout);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> registerUser());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
        
        // Add password validation listener
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void validatePassword(String password) {
        // Show requirements layout when user starts typing
        if (!password.isEmpty()) {
            passwordRequirementsLayout.setVisibility(View.VISIBLE);
        } else {
            passwordRequirementsLayout.setVisibility(View.GONE);
            return;
        }
        
        // Check length (at least 6 characters)
        boolean hasLength = password.length() >= 6;
        updateRequirementUI(tvRequirementLength, hasLength);
        
        // Check uppercase letter
        boolean hasUppercase = password.matches(".*[A-Z].*");
        updateRequirementUI(tvRequirementUppercase, hasUppercase);
        
        // Check lowercase letter
        boolean hasLowercase = password.matches(".*[a-z].*");
        updateRequirementUI(tvRequirementLowercase, hasLowercase);
        
        // Check special character
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        updateRequirementUI(tvRequirementSpecial, hasSpecial);
    }
    
    private void updateRequirementUI(TextView textView, boolean isMet) {
        if (isMet) {
            textView.setTextColor(ContextCompat.getColor(this, R.color.text_success));
        } else {
            textView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (username.length() < 3) {
            etUsername.setError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone) || phone.length() < 10) {
            etPhone.setError("Valid phone number is required");
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Check if user type is selected
        if (rgUserType.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select account type", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected user type
        int selectedId = rgUserType.getCheckedRadioButtonId();
        RadioButton selectedRadio = findViewById(selectedId);
        String userType = selectedRadio.getText().toString().toLowerCase();

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // Create Firebase Auth user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Create user object with username
                            User user = new User(
                                    firebaseUser.getUid(),
                                    fullName,
                                    email,
                                    phone,
                                    userType
                            );

                            // Set username
                            user.setUsername(username);
                            user.setAccountType(userType);
                            user.setRegistrationDate(System.currentTimeMillis());

                            // Save to database
                            usersRef.child(firebaseUser.getUid()).setValue(user)
                                    .addOnCompleteListener(dbTask -> {
                                        progressBar.setVisibility(View.GONE);
                                        btnRegister.setEnabled(true);

                                        if (dbTask.isSuccessful()) {
                                            // Save session with username
                                            sessionManager.createLoginSession(
                                                    firebaseUser.getUid(),
                                                    userType,
                                                    username,  // Use username instead of fullName
                                                    email
                                            );

                                            Toast.makeText(RegisterActivity.this,
                                                    "Welcome, " + username + "!",
                                                    Toast.LENGTH_SHORT).show();

                                            // Route based on user type
                                            Intent intent;
                                            if ("provider".equalsIgnoreCase(userType)) {
                                                intent = new Intent(RegisterActivity.this, ProviderDashboardActivity.class);
                                            } else {
                                                intent = new Intent(RegisterActivity.this, MainActivity.class);
                                            }

                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(RegisterActivity.this,
                                                    "Failed to save user data: " + dbTask.getException().getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        Toast.makeText(RegisterActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}