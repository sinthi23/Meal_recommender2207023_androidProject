package com.example.mealrecmmenderandroid.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mealrecmmenderandroid.databinding.ActivityForgotPasswordBinding;
import com.example.mealrecmmenderandroid.helpers.FirebaseHelper;
import com.example.mealrecmmenderandroid.utils.ValidationHelper;
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

        String emailError = ValidationHelper.getEmailError(email);
        if (emailError != null) {
            binding.emailEditText.setError(emailError);
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
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Password reset email sent to " + finalEmail,
                                    Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            String error = "Unknown error";
                            if (task.getException() != null) {
                                error = task.getException().getMessage();
                            }
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Failed to send reset email: " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.resetPasswordButton.setEnabled(!show);
        binding.emailEditText.setEnabled(!show);
    }
}