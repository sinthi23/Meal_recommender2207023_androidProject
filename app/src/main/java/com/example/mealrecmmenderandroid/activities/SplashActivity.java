package com.example.mealrecmmenderandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.helpers.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Check if user is logged in after delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SessionManager sessionManager = new SessionManager(SplashActivity.this);

                Intent intent;
                if (sessionManager.isLoggedIn()) {
                    // User is logged in, go to MainActivity
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    // User is not logged in, go to LoginActivity
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }

                startActivity(intent);
                finish();
            }
        }, SPLASH_DELAY);
    }
}