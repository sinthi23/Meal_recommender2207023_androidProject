package com.example.mealrecmmenderandroid.activities.consumer;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.example.mealrecmmenderandroid.databinding.ActivityAnalyticsBinding;
import com.example.mealrecmmenderandroid.models.CookHistory;
import com.example.mealrecmmenderandroid.helpers.FirebaseHelper;
import com.example.mealrecmmenderandroid.helpers.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AnalyticsActivity extends AppCompatActivity {

    private ActivityAnalyticsBinding binding;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private List<CookHistory> allHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalyticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = new SessionManager(this);
        allHistory = new ArrayList<>();

        setupToolbar();
        loadAnalytics();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Analytics");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadAnalytics() {
        binding.progressBar.setVisibility(View.VISIBLE);

        String userId = sessionManager.getUserId();

        firebaseHelper.getUserCookHistoryRef(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allHistory.clear();

                        for (DataSnapshot historySnapshot : snapshot.getChildren()) {
                            CookHistory history = historySnapshot.getValue(CookHistory.class);
                            if (history != null) {
                                allHistory.add(history);
                            }
                        }

                        calculateAndDisplayAnalytics();
                        binding.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(AnalyticsActivity.this,
                                "Error loading analytics: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void calculateAndDisplayAnalytics() {
        if (allHistory.isEmpty()) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.chartsLayout.setVisibility(View.GONE);
            return;
        }

        binding.emptyStateLayout.setVisibility(View.GONE);
        binding.chartsLayout.setVisibility(View.VISIBLE);

        // Calculate statistics
        int totalMeals = allHistory.size();
        int totalCalories = 0;
        int weeklyCalories = 0;
        int monthlyCalories = 0;

        Calendar cal = Calendar.getInstance();
        int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        for (CookHistory history : allHistory) {
            totalCalories += history.getCalories();

            cal.setTimeInMillis(history.getCookedDate());

            // This week
            if (cal.get(Calendar.WEEK_OF_YEAR) == currentWeek &&
                    cal.get(Calendar.YEAR) == currentYear) {
                weeklyCalories += history.getCalories();
            }

            // This month
            if (cal.get(Calendar.MONTH) == currentMonth &&
                    cal.get(Calendar.YEAR) == currentYear) {
                monthlyCalories += history.getCalories();
            }
        }

        // Update summary cards
        binding.totalMealsTextView.setText(String.valueOf(totalMeals));
        binding.totalCaloriesTextView.setText(String.valueOf(totalCalories));
        binding.weeklyCaloriesTextView.setText(String.valueOf(weeklyCalories));
        binding.monthlyCaloriesTextView.setText(String.valueOf(monthlyCalories));
    }
}