package com.healthymeal.recommender.activities.provider;

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
import com.healthymeal.recommender.R;
import com.healthymeal.recommender.adapters.ProviderRecipeAdapter;
import com.healthymeal.recommender.databinding.ActivityProviderDashboardBinding;
import com.healthymeal.recommender.models.Award;
import com.healthymeal.recommender.models.Recipe;
import com.healthymeal.recommender.models.User;
import com.healthymeal.recommender.utils.DateHelper;
import com.healthymeal.recommender.utils.FirebaseHelper;
import com.healthymeal.recommender.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ProviderDashboardActivity extends AppCompatActivity {

    private ActivityProviderDashboardBinding binding;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private ProviderRecipeAdapter adapter;
    private List<Recipe> myRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProviderDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = new SessionManager(this);
        myRecipes = new ArrayList<>();

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadProviderStats();
        loadMyRecipes();
        checkAwards();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Provider Dashboard");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new ProviderRecipeAdapter(this, myRecipes,
                new ProviderRecipeAdapter.OnRecipeActionListener() {
                    @Override
                    public void onEditClick(Recipe recipe) {
                        Intent intent = new Intent(ProviderDashboardActivity.this,
                                EditRecipeActivity.class);
                        intent.putExtra("recipe_id", recipe.getRecipeId());
                        startActivity(intent);
                    }

                    @Override
                    public void onDeleteClick(Recipe recipe) {
                        deleteRecipe(recipe);
                    }

                    @Override
                    public void onViewClick(Recipe recipe) {
                        // View recipe details
                    }
                });

        binding.recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recipesRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.addRecipeButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AddRecipeActivity.class));
        });
    }

    private void loadProviderStats() {
        String providerId = sessionManager.getUserId();

        firebaseHelper.getUserRef(providerId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User provider = snapshot.getValue(User.class);
                            if (provider != null) {
                                binding.totalRecipesTextView.setText(
                                        String.valueOf(provider.getTotalRecipesProvided()));
                                binding.averageRatingTextView.setText(
                                        String.format("%.1f", provider.getAverageRating()));
                                binding.totalRatingsTextView.setText(
                                        String.valueOf(provider.getRatingCount()));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadMyRecipes() {
        binding.progressBar.setVisibility(View.VISIBLE);

        String providerId = sessionManager.getUserId();
        Query query = firebaseHelper.getRecipesRef()
                .orderByChild("providerId")
                .equalTo(providerId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myRecipes.clear();

                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        myRecipes.add(recipe);
                    }
                }

                if (myRecipes.isEmpty()) {
                    binding.emptyStateLayout.setVisibility(View.VISIBLE);
                    binding.recipesRecyclerView.setVisibility(View.GONE);
                } else {
                    binding.emptyStateLayout.setVisibility(View.GONE);
                    binding.recipesRecyclerView.setVisibility(View.VISIBLE);
                }

                adapter.updateRecipes(myRecipes);
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ProviderDashboardActivity.this,
                        "Error loading recipes: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAwards() {
        String providerId = sessionManager.getUserId();
        int currentYear = DateHelper.getCurrentYear();

        firebaseHelper.getYearAwardsRef(currentYear)
                .orderByChild("providerId")
                .equalTo(providerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot awardSnapshot : snapshot.getChildren()) {
                                Award award = awardSnapshot.getValue(Award.class);
                                if (award != null) {
                                    showAwardBanner(award);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void showAwardBanner(Award award) {
        binding.awardBannerCard.setVisibility(View.VISIBLE);
        binding.awardRankTextView.setText(award.getRankDisplay());
        binding.awardPrizeTextView.setText(award.getPrizeAmount());
        binding.awardYearTextView.setText("Award " + award.getYear());
    }

    private void deleteRecipe(Recipe recipe) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Recipe")
                .setMessage("Are you sure you want to delete this recipe?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    firebaseHelper.getRecipeRef(recipe.getRecipeId())
                            .removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Recipe deleted", Toast.LENGTH_SHORT).show();

                                // Update provider recipe count
                                updateProviderRecipeCount(-1);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateProviderRecipeCount(int change) {
        String providerId = sessionManager.getUserId();

        firebaseHelper.getUserRef(providerId)
                .child("totalRecipesProvided")
                .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                    @NonNull
                    @Override
                    public com.google.firebase.database.Transaction.Result doTransaction(
                            @NonNull com.google.firebase.database.MutableData mutableData) {
                        Integer currentValue = mutableData.getValue(Integer.class);
                        if (currentValue == null) {
                            mutableData.setValue(change > 0 ? 1 : 0);
                        } else {
                            mutableData.setValue(currentValue + change);
                        }
                        return com.google.firebase.database.Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {}
                });
    }
}