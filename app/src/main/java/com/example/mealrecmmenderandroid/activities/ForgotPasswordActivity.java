package com.example.mealrecmmenderandroid.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mealrecmmenderandroid.databinding.ActivityForgotPasswordBinding;
import com.example.mealrecmmenderandroid.helpers.FirebaseHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();

        setupListeners();
    }

    private void setupListeners() {
        binding.resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        binding.backToLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void resetPassword() {
        String email = "";
        if (binding.emailEditText.getText() != null) {
            email = binding.emailEditText.getText().toString().trim();
        }

        if (TextUtils.isEmpty(email)) {
            binding.emailEditText.setError("Email is required");
            binding.emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.setError("Please enter a valid email address");
            binding.emailEditText.requestFocus();
            return;
        }

        showLoading(true);

        final String finalEmail = email;
        firebaseHelper.getAuth().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showLoading(false);

                        if (task.isSuccessful()) {
                            showSuccessDialog(finalEmail);
                        } else {
                            String errorMessage = "Failed to send reset email";
                            if (task.getException() != null) {
                                errorMessage = task.getException().getMessage();

                                if (errorMessage.contains("no user record") || errorMessage.contains("user-not-found")) {
                                    errorMessage = "No account found with this email address. Please check your email or register a new account.";
                                } else if (errorMessage.contains("invalid-email")) {
                                    errorMessage = "Invalid email address format.";
                                } else if (errorMessage.contains("network")) {
                                    errorMessage = "Network error. Please check your internet connection.";
                                }
                            }
                            showErrorDialog(errorMessage);
                        }
                    }
                });
    }

    private void showSuccessDialog(String email) {
        new AlertDialog.Builder(this)
                .setTitle("Email Sent")
                .setMessage("Password reset link has been sent to " + email +
                        ". Please check your email inbox and spam folder.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.resetPasswordButton.setEnabled(!show);
        binding.emailEditText.setEnabled(!show);

        if (show) {
            binding.resetPasswordButton.setText("SENDING...");
        } else {
            binding.resetPasswordButton.setText("SEND RESET LINK");
        }
    }
}