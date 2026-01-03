package com.example.mealrecmmenderandroid.activities.consumer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.mealrecmmenderandroid.adapters.CookHistoryAdapter;
import com.example.mealrecmmenderandroid.databinding.ActivityCookHistoryBinding;
import com.example.mealrecmmenderandroid.models.CookHistory;
import com.example.mealrecmmenderandroid.helpers.FirebaseHelper;
import com.example.mealrecmmenderandroid.helpers.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CookHistoryActivity extends AppCompatActivity {

    private ActivityCookHistoryBinding binding;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private CookHistoryAdapter adapter;
    private List<CookHistory> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCookHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = new SessionManager(this);
        historyList = new ArrayList<>();

        setupToolbar();
        setupRecyclerView();
        loadCookHistory();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Cook History");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new CookHistoryAdapter(this, historyList,
                new CookHistoryAdapter.OnHistoryClickListener() {
                    @Override
                    public void onHistoryClick(CookHistory cookHistory) {
                        // Show details when history item is clicked
                        Toast.makeText(CookHistoryActivity.this,
                                "Cooked: " + cookHistory.getRecipeName(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onViewRecipeClick(CookHistory cookHistory) {
                        // View recipe details when button is clicked
                        Intent intent = new Intent(CookHistoryActivity.this,
                                RecipeDetailActivity.class);
                        intent.putExtra("recipe_id", cookHistory.getRecipeId());
                        startActivity(intent);
                    }
                });

        binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.historyRecyclerView.setAdapter(adapter);
    }

    private void loadCookHistory() {
        binding.progressBar.setVisibility(View.VISIBLE);

        String userId = sessionManager.getUserId();
        Query query = firebaseHelper.getUserCookHistoryRef(userId)
                .orderByChild("cookedDate");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();

                for (DataSnapshot historySnapshot : snapshot.getChildren()) {
                    CookHistory history = historySnapshot.getValue(CookHistory.class);
                    if (history != null) {
                        historyList.add(history);
                    }
                }

                // Sort by most recent first
                Collections.reverse(historyList);

                if (historyList.isEmpty()) {
                    binding.emptyStateLayout.setVisibility(View.VISIBLE);
                    binding.historyRecyclerView.setVisibility(View.GONE);
                } else {
                    binding.emptyStateLayout.setVisibility(View.GONE);
                    binding.historyRecyclerView.setVisibility(View.VISIBLE);

                    // Update statistics
                    updateStatistics();
                }

                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(CookHistoryActivity.this,
                        "Error loading history: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatistics() {
        int totalMeals = historyList.size();
        int totalCalories = 0;
        double averageRating = 0;

        for (CookHistory history : historyList) {
            totalCalories += history.getCalories();
            averageRating += history.getUserRating();
        }

        if (totalMeals > 0) {
            averageRating /= totalMeals;
        }

        binding.totalMealsTextView.setText(String.valueOf(totalMeals));
        binding.totalCaloriesTextView.setText(String.valueOf(totalCalories));
        binding.averageRatingTextView.setText(String.format(Locale.getDefault(), "%.1f", averageRating));
    }
}